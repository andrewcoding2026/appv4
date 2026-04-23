package com.nfc.security.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import com.nfc.security.data.local.MalwareBlocklist
import com.nfc.security.data.local.ScanResultLocalDataSource
import com.nfc.security.domain.model.MalwareHit
import com.nfc.security.domain.model.ScanReport
import com.nfc.security.domain.repository.ScanRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanResultLocalDataSource: ScanResultLocalDataSource
) : ScanRepository {

    override suspend fun performScan(): ScanReport = withContext(Dispatchers.IO) {
        val hits = mutableListOf<MalwareHit>()

        val installedPackages = context.packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        installedPackages.forEach { pkg ->
            if (pkg.packageName in MalwareBlocklist.KNOWN_MALICIOUS_PACKAGES) {
                hits.add(
                    MalwareHit(
                        packageNameOrPath = pkg.packageName,
                        hitType = MalwareHit.HitType.KNOWN_MALWARE_PACKAGE,
                        detail = "Known malicious package: ${pkg.packageName}"
                    )
                )
            }
        }

        var scannedFiles = 0
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (downloadsDir.exists() && downloadsDir.canRead()) {
            downloadsDir.walkTopDown().maxDepth(3).forEach { file ->
                if (file.isFile) {
                    scannedFiles++
                    val ext = ".${file.extension.lowercase()}"
                    if (ext in MalwareBlocklist.SUSPICIOUS_EXTENSIONS && ext != ".apk") {
                        hits.add(
                            MalwareHit(
                                packageNameOrPath = file.absolutePath,
                                hitType = MalwareHit.HitType.SUSPICIOUS_EXTENSION,
                                detail = "Suspicious file type '$ext' in Downloads"
                            )
                        )
                    }
                }
            }
        }

        val apksInDownloads = downloadsDir.takeIf { it.exists() && it.canRead() }
            ?.walkTopDown()?.maxDepth(2)
            ?.filter { it.isFile && it.extension.lowercase() == "apk" }
            ?.toList() ?: emptyList()
        apksInDownloads.forEach { apkFile ->
            hits.add(
                MalwareHit(
                    packageNameOrPath = apkFile.absolutePath,
                    hitType = MalwareHit.HitType.SUSPICIOUS_EXTENSION,
                    detail = "APK file found in Downloads — verify source before installing"
                )
            )
        }

        val riskScore = minOf(100, hits.size * 15)
        val report = ScanReport(
            hits = hits,
            scannedApps = installedPackages.size,
            scannedFiles = scannedFiles,
            riskScore = riskScore,
            scanCompletedAt = System.currentTimeMillis()
        )
        scanResultLocalDataSource.saveReport(report)
        report
    }

    override suspend fun clearCaches(): Long = withContext(Dispatchers.IO) {
        var cleared = 0L
        val cacheDir = context.cacheDir
        if (cacheDir.exists()) {
            val before = cacheDir.walkTopDown().sumOf { it.length() }
            cacheDir.deleteRecursively()
            cleared += before
        }
        val externalCache = context.externalCacheDir
        if (externalCache?.exists() == true) {
            val before = externalCache.walkTopDown().sumOf { it.length() }
            externalCache.deleteRecursively()
            cleared += before
        }
        cleared
    }

    override fun getLastReport(): Flow<ScanReport?> = flow {
        emit(scanResultLocalDataSource.getLastReport())
    }
}
