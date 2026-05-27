package com.nfcsecurity.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.nfcsecurity.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

const val VPN_CHANNEL_ID = "vpn_channel"
const val SECURITY_CHANNEL_ID = "security_channel"
const val VPN_NOTIFICATION_ID = 1001
const val SECURITY_NOTIFICATION_ID = 1002

@Singleton
class VpnNotificationHelper @Inject constructor(
    private val notificationManager: NotificationManager
) {

    fun createChannels() {
        val vpnChannel = NotificationChannel(
            VPN_CHANNEL_ID,
            "VPN Status",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "NFC Secure Shield VPN connection status" }

        val securityChannel = NotificationChannel(
            SECURITY_CHANNEL_ID,
            "Security Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Device security warnings" }

        notificationManager.createNotificationChannels(listOf(vpnChannel, securityChannel))
    }

    fun buildVpnNotification(context: Context, statusText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(context, VPN_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("NFC Secure Shield VPN")
            .setContentText(statusText)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun showSecurityAlert(context: Context, title: String, message: String) {
        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, SECURITY_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager.notify(SECURITY_NOTIFICATION_ID, notification)
    }
}
