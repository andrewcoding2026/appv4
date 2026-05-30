package com.NFC.SecureShield.data.local

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import com.NFC.SecureShield.domain.model.SecurityCheckResult
import com.NFC.SecureShield.domain.model.SecurityCheckResult.Severity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityCheckDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyguardManager: KeyguardManager
) {

    /**
     * Check 1 — Root Detection (static heuristic, bypass-susceptible).
     *
     * Enumerates well-known su binary paths and inspects Build.TAGS for "test-keys".
     * Limitation: advanced root management frameworks (e.g. Magisk with Zygisk + DenyList)
     * hide su binaries and patch Build.TAGS at the system call level, rendering this check
     * ineffective against a determined attacker. A production implementation must treat this
     * as a best-effort signal only and rely on the Google Play Integrity API (check 7,
     * checkPlayIntegrity) for authoritative device-integrity attestation.
     */
    suspend fun checkIsRooted(): SecurityCheckResult = withContext(Dispatchers.IO) {
        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su-backup",
            "/system/xbin/mu"
        )
        val suFound = suPaths.any { File(it).exists() }
        val testKeys = Build.TAGS?.contains("test-keys") == true
        val passed = !suFound && !testKeys
        SecurityCheckResult(
            checkName = "Root Detection",
            passed = passed,
            severity = Severity.CRITICAL,
            detail = when {
                suFound -> "su binary found on device"
                testKeys -> "Device running test-keys build"
                else -> "No root indicators detected"
            }
        )
    }

    suspend fun checkDeveloperOptions(): SecurityCheckResult = withContext(Dispatchers.IO) {
        val enabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) != 0
        SecurityCheckResult(
            checkName = "Developer Options",
            passed = !enabled,
            severity = Severity.MEDIUM,
            detail = if (enabled) "Developer options are enabled" else "Developer options are disabled"
        )
    }

    suspend fun checkUsbDebugging(): SecurityCheckResult = withContext(Dispatchers.IO) {
        val enabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            0
        ) != 0
        SecurityCheckResult(
            checkName = "USB Debugging",
            passed = !enabled,
            severity = Severity.HIGH,
            detail = if (enabled) "USB debugging (ADB) is enabled" else "USB debugging is disabled"
        )
    }

    suspend fun checkScreenLock(): SecurityCheckResult = withContext(Dispatchers.IO) {
        val secure = keyguardManager.isDeviceSecure
        SecurityCheckResult(
            checkName = "Screen Lock",
            passed = secure,
            severity = Severity.HIGH,
            detail = if (secure) "Device has a secure screen lock" else "No secure screen lock configured"
        )
    }

    suspend fun checkBiometricAvailability(): SecurityCheckResult = withContext(Dispatchers.IO) {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        val passed = canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
        SecurityCheckResult(
            checkName = "Biometric Authentication",
            passed = passed,
            severity = Severity.LOW,
            detail = when (canAuthenticate) {
                BiometricManager.BIOMETRIC_SUCCESS -> "Strong biometric authentication available"
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric hardware on device"
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric hardware unavailable"
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometrics enrolled"
                else -> "Biometric authentication not available"
            }
        )
    }

    suspend fun checkUnknownSourceApps(): SecurityCheckResult = withContext(Dispatchers.IO) {
        val unknownSourceEnabled = context.packageManager.canRequestPackageInstalls()
        SecurityCheckResult(
            checkName = "Unknown Sources",
            passed = !unknownSourceEnabled,
            severity = Severity.HIGH,
            detail = if (unknownSourceEnabled) "Installation from unknown sources is permitted" else "Unknown source installation is blocked"
        )
    }

    suspend fun checkGooglePlayProtect(): SecurityCheckResult = withContext(Dispatchers.IO) {
        val hasPlayStore = context.packageManager.getLaunchIntentForPackage("com.android.vending") != null
        SecurityCheckResult(
            checkName = "Google Play Protect",
            passed = hasPlayStore,
            severity = Severity.MEDIUM,
            detail = if (hasPlayStore) "Google Play Store present (Play Protect available)" else "Google Play Store not found"
        )
    }

    /**
     * Check 7 — Google Play Integrity API (NOT YET IMPLEMENTED).
     *
     * Intended to call IntegrityManagerFactory.create(context).requestIntegrityToken() and
     * decode the verdict to verify: (a) the app binary has not been tampered with, (b) the
     * device passes the MEETS_BASIC_INTEGRITY / MEETS_DEVICE_INTEGRITY verdict, and (c) the
     * licence was obtained via Google Play.
     *
     * The dependency (com.google.android.play:integrity:1.6.0) is declared in
     * libs.versions.toml and included in app/build.gradle.kts. Integration is deferred
     * because the API requires a server-side nonce generation endpoint and a Google Cloud
     * project with the Play Integrity API enabled — infrastructure outside the scope of
     * the current thesis prototype. This stub keeps the check enumerable and explicitly
     * marks it as an open development item so the absence is visible at runtime and in
     * code review rather than silently missing.
     */
    @Suppress("FunctionOnlyReturningConstant")
    suspend fun checkPlayIntegrity(): SecurityCheckResult = withContext(Dispatchers.IO) {
        SecurityCheckResult(
            checkName = "Play Integrity",
            passed = false,
            severity = Severity.HIGH,
            detail = "Play Integrity API not yet integrated — verdict unavailable (open development item)"
        )
    }
}
