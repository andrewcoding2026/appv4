package com.nfc.security.data.local

import com.nfc.security.domain.model.MalwareHit
import com.nfc.security.domain.model.ScanReport
import javax.inject.Inject
import javax.inject.Singleton

private const val KEY_LAST_SCAN = "last_scan_report"

@Singleton
class ScanResultLocalDataSource @Inject constructor(
    private val prefs: EncryptedPreferencesDataSource
) {

    fun saveReport(report: ScanReport) {
        val json = buildString {
            append("{")
            append("\"scannedApps\":${report.scannedApps},")
            append("\"scannedFiles\":${report.scannedFiles},")
            append("\"riskScore\":${report.riskScore},")
            append("\"scanCompletedAt\":${report.scanCompletedAt},")
            append("\"hits\":[")
            report.hits.forEachIndexed { i, hit ->
                if (i > 0) append(",")
                append("{")
                append("\"packageNameOrPath\":\"${hit.packageNameOrPath.replace("\"", "\\\"")}\",")
                append("\"hitType\":\"${hit.hitType.name}\",")
                append("\"detail\":\"${hit.detail.replace("\"", "\\\"")}\"")
                append("}")
            }
            append("]}")
        }
        prefs.putString(KEY_LAST_SCAN, json)
    }

    fun getLastReport(): ScanReport? {
        val json = prefs.getString(KEY_LAST_SCAN) ?: return null
        return try {
            parseReport(json)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseReport(json: String): ScanReport {
        fun extractLong(key: String): Long {
            val pattern = "\"$key\":(\\d+)".toRegex()
            return pattern.find(json)?.groupValues?.get(1)?.toLong() ?: 0L
        }
        fun extractInt(key: String): Int {
            val pattern = "\"$key\":(\\d+)".toRegex()
            return pattern.find(json)?.groupValues?.get(1)?.toInt() ?: 0
        }
        val hitsRegex = "\\{\"packageNameOrPath\":\"([^\"]*)\",\"hitType\":\"([^\"]*)\",\"detail\":\"([^\"]*)\"\\}".toRegex()
        val hits = hitsRegex.findAll(json).map { match ->
            MalwareHit(
                packageNameOrPath = match.groupValues[1],
                hitType = MalwareHit.HitType.valueOf(match.groupValues[2]),
                detail = match.groupValues[3]
            )
        }.toList()
        return ScanReport(
            hits = hits,
            scannedApps = extractInt("scannedApps"),
            scannedFiles = extractInt("scannedFiles"),
            riskScore = extractInt("riskScore"),
            scanCompletedAt = extractLong("scanCompletedAt")
        )
    }
}
