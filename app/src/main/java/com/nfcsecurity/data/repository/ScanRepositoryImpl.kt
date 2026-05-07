package com.nfcsecurity.data.repository

import android.content.pm.ApplicationInfo
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import com.nfcsecurity.data.local.MalwareBlocklist
import com.nfcsecurity.data.local.ScanResultLocalDataSource
import com.nfcsecurity.domain.model.MalwareHit
import com.nfcsecurity.domain.model.MalwareHit.Severity
import com.nfcsecurity.domain.model.ScanReport
import com.nfcsecurity.domain.repository.ScanRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanResultLocalDataSource: ScanResultLocalDataSource
) : ScanRepository {

    override suspend fun performScan(): ScanReport = withContext(Dispatchers.IO) {
        val hits = mutableListOf<MalwareHit>()

        // ── 1. Installed-package analysis ────────────────────────────────────
        val packages = context.packageManager.getInstalledPackages(
            PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS
        )

        packages.forEach { pkg ->
            val pkgName   = pkg.packageName
            val appInfo   = pkg.applicationInfo ?: return@forEach
            val isSystem  = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            // 1a. Known-malware package name
            if (pkgName in MalwareBlocklist.KNOWN_MALICIOUS_PACKAGES) {
                hits += MalwareHit(
                    packageNameOrPath = pkgName,
                    hitType  = MalwareHit.HitType.KNOWN_MALWARE_PACKAGE,
                    detail   = "Package name matches known malware: $pkgName",
                    severity = Severity.CRITICAL
                )
            }

            // 1b. Dangerous permission combinations (stalkerware / spyware / trojan patterns)
            val declaredPerms = pkg.requestedPermissions?.toSet() ?: emptySet()
            for ((comboPerms, reason, isHigh) in MalwareBlocklist.DANGEROUS_PERMISSION_COMBOS) {
                if (declaredPerms.containsAll(comboPerms) && !isSystem) {
                    hits += MalwareHit(
                        packageNameOrPath = pkgName,
                        hitType  = MalwareHit.HitType.DANGEROUS_PERMISSIONS,
                        detail   = "$pkgName — $reason",
                        severity = if (isHigh) Severity.HIGH else Severity.MEDIUM
                    )
                    break  // one hit per package is enough
                }
            }

            // 1c. Accessibility service abuse: requests BIND_ACCESSIBILITY_SERVICE
            if (!isSystem &&
                "android.permission.BIND_ACCESSIBILITY_SERVICE" in declaredPerms
            ) {
                hits += MalwareHit(
                    packageNameOrPath = pkgName,
                    hitType  = MalwareHit.HitType.ACCESSIBILITY_ABUSE,
                    detail   = "$pkgName requests accessibility service binding (common in keyloggers / overlay trojans)",
                    severity = Severity.HIGH
                )
            }

            // 1d. Device-admin abuse: requests BIND_DEVICE_ADMIN
            if (!isSystem &&
                "android.permission.BIND_DEVICE_ADMIN" in declaredPerms
            ) {
                hits += MalwareHit(
                    packageNameOrPath = pkgName,
                    hitType  = MalwareHit.HitType.DEVICE_ADMIN_ABUSE,
                    detail   = "$pkgName requests device administrator binding (prevents uninstall)",
                    severity = Severity.HIGH
                )
            }

            // 1e. Debuggable flag on a release/side-loaded app (potential tampered APK)
            if (!isSystem && (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                hits += MalwareHit(
                    packageNameOrPath = pkgName,
                    hitType  = MalwareHit.HitType.DEBUGGABLE_APP,
                    detail   = "$pkgName is marked debuggable — may be a modified/repackaged APK",
                    severity = Severity.LOW
                )
            }

            // 1f. SHA-256 hash of the installed APK against known-malware hash set
            //     (skip system apps and large APKs >50 MB to keep scan time reasonable)
            if (!isSystem) {
                val apkFile = File(appInfo.publicSourceDir)
                if (apkFile.exists() && apkFile.length() in 1..(50 * 1024 * 1024L)) {
                    val hash = sha256(apkFile)
                    if (hash != null && hash in MalwareBlocklist.KNOWN_MALWARE_HASHES) {
                        hits += MalwareHit(
                            packageNameOrPath = pkgName,
                            hitType  = MalwareHit.HitType.SIDELOADED_APK_HASH,
                            detail   = "$pkgName APK SHA-256 matches known malware signature ($hash)",
                            severity = Severity.CRITICAL
                        )
                    }
                }
            }
        }

        // ── 2. File-system scan of the Downloads directory ───────────────────
        var scannedFiles = 0
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (downloadsDir.exists() && downloadsDir.canRead()) {
            downloadsDir.walkTopDown().maxDepth(3).forEach { file ->
                if (!file.isFile) return@forEach
                scannedFiles++
                val ext = ".${file.extension.lowercase()}"

                when {
                    ext == ".apk" -> {
                        // Hash-check side-loaded APKs in Downloads
                        if (file.length() in 1..(50 * 1024 * 1024L)) {
                            val hash = sha256(file)
                            if (hash != null && hash in MalwareBlocklist.KNOWN_MALWARE_HASHES) {
                                hits += MalwareHit(
                                    packageNameOrPath = file.absolutePath,
                                    hitType  = MalwareHit.HitType.SIDELOADED_APK_HASH,
                                    detail   = "APK hash matches known malware — do not install: ${file.name}",
                                    severity = Severity.CRITICAL
                                )
                            } else {
                                hits += MalwareHit(
                                    packageNameOrPath = file.absolutePath,
                                    hitType  = MalwareHit.HitType.SUSPICIOUS_EXTENSION,
                                    detail   = "Unverified APK in Downloads — confirm source before installing: ${file.name}",
                                    severity = Severity.MEDIUM
                                )
                            }
                        }
                    }
                    ext in MalwareBlocklist.SUSPICIOUS_EXTENSIONS -> {
                        hits += MalwareHit(
                            packageNameOrPath = file.absolutePath,
                            hitType  = MalwareHit.HitType.SUSPICIOUS_EXTENSION,
                            detail   = "Suspicious file type '$ext' found in Downloads: ${file.name}",
                            severity = Severity.MEDIUM
                        )
                    }
                }
            }
        }

        // ── 3. Risk score weighted by severity ───────────────────────────────
        val riskScore = minOf(100, hits.sumOf { hit ->
            when (hit.severity) {
                Severity.CRITICAL -> 35
                Severity.HIGH     -> 20
                Severity.MEDIUM   -> 10
                Severity.LOW      ->  5
            }
        })

        val report = ScanReport(
            hits              = hits,
            scannedApps       = packages.size,
            scannedFiles      = scannedFiles,
            riskScore         = riskScore,
            scanCompletedAt   = System.currentTimeMillis()
        )
        scanResultLocalDataSource.saveReport(report)
        report
    }

    override suspend fun clearCaches(): Long = withContext(Dispatchers.IO) {
        var cleared = 0L
        val cacheDir = context.cacheDir
        if (cacheDir.exists()) {
            cleared += cacheDir.walkTopDown().sumOf { it.length() }
            cacheDir.deleteRecursively()
        }
        context.externalCacheDir?.takeIf { it.exists() }?.let { dir ->
            cleared += dir.walkTopDown().sumOf { it.length() }
            dir.deleteRecursively()
        }
        cleared
    }

    override fun getLastReport(): Flow<ScanReport?> = flow {
        emit(scanResultLocalDataSource.getLastReport())
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun sha256(file: File): String? = try {
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered(8192).use { stream ->
            val buf = ByteArray(8192)
            var n: Int
            while (stream.read(buf).also { n = it } != -1) md.update(buf, 0, n)
        }
        md.digest().joinToString("") { "%02x".format(it) }
    } catch (_: Exception) {
        null
    }
}
