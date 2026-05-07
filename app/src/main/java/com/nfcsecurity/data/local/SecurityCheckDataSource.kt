package com.nfcsecurity.data.local

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import com.nfcsecurity.domain.model.SecurityCheckResult
import com.nfcsecurity.domain.model.SecurityCheckResult.Severity
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
}
