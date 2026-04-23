package com.nfc.security.service

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.nfc.security.domain.model.VpnState
import com.nfc.security.domain.repository.VpnRepository
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.min

class NfcVpnService : VpnService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface VpnServiceEntryPoint {
        fun vpnRepository(): VpnRepository
        fun vpnNotificationHelper(): VpnNotificationHelper
    }

    companion object {
        const val ACTION_START = "com.nfc.security.vpn.START"
        const val ACTION_STOP = "com.nfc.security.vpn.STOP"
        private const val TUN_ADDRESS = "10.0.0.2"
        private const val TUN_ROUTE = "0.0.0.0"
        private const val DNS_SERVER = "1.1.1.1"
        private const val MTU = 1500
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var tunInterface: ParcelFileDescriptor? = null
    private var tunnelJob: Job? = null
    private lateinit var vpnRepository: VpnRepository
    private lateinit var notificationHelper: VpnNotificationHelper

    override fun onCreate() {
        super.onCreate()
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            VpnServiceEntryPoint::class.java
        )
        vpnRepository = entryPoint.vpnRepository()
        notificationHelper = entryPoint.vpnNotificationHelper()
        notificationHelper.createChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_START -> {
                startForeground(VPN_NOTIFICATION_ID, notificationHelper.buildVpnNotification(this, "Connecting..."))
                startTunnel()
                START_STICKY
            }
            ACTION_STOP -> {
                stopTunnel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                START_NOT_STICKY
            }
            else -> START_NOT_STICKY
        }
    }

    private fun startTunnel() {
        tunnelJob = serviceScope.launch {
            vpnRepository.updateVpnState(VpnState.Connecting)
            var backoffMs = 1000L
            while (true) {
                try {
                    val tun = buildTunInterface() ?: break
                    tunInterface = tun
                    vpnRepository.updateVpnState(VpnState.Connected("10.0.0.1", 0L, 0L))
                    updateNotification("Connected — 10.0.0.1")
                    runPacketLoop(tun)
                } catch (e: Exception) {
                    if (tunnelJob?.isCancelled == true) break
                    vpnRepository.updateVpnState(VpnState.Connecting)
                    delay(backoffMs)
                    backoffMs = min(backoffMs * 2, 30_000L)
                }
            }
        }
    }

    private fun buildTunInterface(): ParcelFileDescriptor? {
        return Builder()
            .setMtu(MTU)
            .addAddress(TUN_ADDRESS, 32)
            .addDnsServer(DNS_SERVER)
            .addRoute(TUN_ROUTE, 0)
            .setSession("NFC Security VPN")
            .establish()
    }

    private suspend fun runPacketLoop(tun: ParcelFileDescriptor) {
        val inStream = FileInputStream(tun.fileDescriptor)
        val outStream = FileOutputStream(tun.fileDescriptor)
        val buffer = ByteArray(MTU)
        var bytesIn = 0L
        var bytesOut = 0L

        val socket = Socket()
        protect(socket)
        try {
            socket.connect(InetSocketAddress(DNS_SERVER, 80), 5000)
            while (tunnelJob?.isCancelled == false) {
                val len = inStream.read(buffer)
                if (len > 0) {
                    bytesOut += len
                    outStream.write(buffer, 0, len)
                    bytesIn += len
                    vpnRepository.updateVpnState(
                        VpnState.Connected("10.0.0.1", bytesIn, bytesOut)
                    )
                }
                delay(10)
            }
        } finally {
            socket.close()
        }
    }

    private fun stopTunnel() {
        tunnelJob?.cancel()
        tunInterface?.close()
        tunInterface = null
        serviceScope.launch {
            vpnRepository.updateVpnState(VpnState.Disconnected)
        }
    }

    private fun updateNotification(status: String) {
        val notification = notificationHelper.buildVpnNotification(this, status)
        startForeground(VPN_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        stopTunnel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onRevoke() {
        stopTunnel()
        super.onRevoke()
    }
}