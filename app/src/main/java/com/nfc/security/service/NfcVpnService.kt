package com.nfc.security.service

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.nfc.security.data.local.MalwareBlocklist
import com.nfc.security.domain.model.VpnState
import com.nfc.security.domain.repository.VpnRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import kotlin.math.min

/**
 * DNS-filtering VPN that intercepts all DNS queries from the device, blocks queries to domains
 * in the malware/stalkerware blocklist, and forwards safe queries to an upstream resolver.
 *
 * Architecture:
 *  - VPN tun address 10.8.0.1/24; virtual DNS at 10.8.0.2 (set as system DNS by the VPN).
 *  - The OS routes queries for 10.8.0.2 through the TUN interface.
 *  - We parse each IPv4/UDP/DNS packet, inspect the queried domain, and either:
 *      a) Return NXDOMAIN immediately (malicious domain), or
 *      b) Forward to 1.1.1.1:53 via a protected socket and relay the upstream response.
 *  - Non-DNS traffic is not routed through the TUN (no 0.0.0.0/0 catch-all route), so
 *    device connectivity is fully preserved.
 */
class NfcVpnService : VpnService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface VpnServiceEntryPoint {
        fun vpnRepository(): VpnRepository
        fun vpnNotificationHelper(): VpnNotificationHelper
    }

    companion object {
        const val ACTION_START = "com.nfc.security.vpn.START"
        const val ACTION_STOP  = "com.nfc.security.vpn.STOP"
        private const val TUN_ADDRESS   = "10.8.0.1"
        private const val TUN_SUBNET    = "10.8.0.0"
        private const val VIRTUAL_DNS   = "10.8.0.2"
        private const val UPSTREAM_DNS  = "1.1.1.1"
        private const val DNS_PORT      = 53
        private const val UDP_PROTO     = 17
        private const val MTU           = 1500
        private const val DNS_TIMEOUT_MS = 3000
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var tunInterface: ParcelFileDescriptor? = null
    private var tunnelJob: Job? = null
    private lateinit var vpnRepository: VpnRepository
    private lateinit var notificationHelper: VpnNotificationHelper

    @Volatile private var bytesIn  = 0L
    @Volatile private var bytesOut = 0L
    @Volatile private var blockedDomains = 0L

    override fun onCreate() {
        super.onCreate()
        val ep = EntryPointAccessors.fromApplication(applicationContext, VpnServiceEntryPoint::class.java)
        vpnRepository    = ep.vpnRepository()
        notificationHelper = ep.vpnNotificationHelper()
        notificationHelper.createChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_START -> {
                startForeground(VPN_NOTIFICATION_ID, notificationHelper.buildVpnNotification(this, "Connecting…"))
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

    // ── Tunnel lifecycle ─────────────────────────────────────────────────────

    private fun startTunnel() {
        tunnelJob = serviceScope.launch {
            vpnRepository.updateVpnState(VpnState.Connecting)
            var backoffMs = 1_000L
            while (isActive) {
                try {
                    val tun = buildTunInterface() ?: break
                    tunInterface = tun
                    vpnRepository.updateVpnState(VpnState.Connected(UPSTREAM_DNS, 0L, 0L))
                    updateNotification("DNS filtering active — $UPSTREAM_DNS")
                    runPacketLoop(tun)          // blocks until tun fd is closed
                } catch (e: Exception) {
                    if (!isActive) break
                    vpnRepository.updateVpnState(VpnState.Connecting)
                    kotlinx.coroutines.delay(backoffMs)
                    backoffMs = min(backoffMs * 2, 30_000L)
                }
            }
        }
    }

    private fun buildTunInterface(): ParcelFileDescriptor? =
        Builder()
            .setMtu(MTU)
            .addAddress(TUN_ADDRESS, 24)
            // Route only the VPN subnet — non-DNS traffic uses the regular network path.
            .addRoute(TUN_SUBNET, 24)
            // Override system DNS so all resolver queries land on our virtual DNS IP.
            .addDnsServer(VIRTUAL_DNS)
            .setSession("NFC Security VPN")
            .establish()

    private fun stopTunnel() {
        tunnelJob?.cancel()
        tunInterface?.close()   // unblocks the blocking read() in runPacketLoop
        tunInterface = null
        serviceScope.launch { vpnRepository.updateVpnState(VpnState.Disconnected) }
    }

    private fun updateNotification(status: String) {
        startForeground(VPN_NOTIFICATION_ID, notificationHelper.buildVpnNotification(this, status))
    }

    override fun onDestroy() { stopTunnel(); serviceScope.cancel(); super.onDestroy() }
    override fun onRevoke()  { stopTunnel(); super.onRevoke() }

    // ── Packet loop ──────────────────────────────────────────────────────────

    /**
     * Blocking loop: reads raw IPv4 packets from the TUN fd, handles DNS queries,
     * and writes responses back into the TUN fd so the OS delivers them to the app.
     * Exits when the TUN fd is closed (IOException) or the coroutine is cancelled.
     */
    private suspend fun runPacketLoop(tun: ParcelFileDescriptor) {
        val inStream  = FileInputStream(tun.fileDescriptor)
        val outStream = FileOutputStream(tun.fileDescriptor)
        val buffer    = ByteArray(MTU)

        // Single protected UDP socket reused for all upstream DNS forwarding.
        val dnsSocket = DatagramSocket().also { protect(it); it.soTimeout = DNS_TIMEOUT_MS }

        try {
            while (tunnelJob?.isActive == true) {
                val len = inStream.read(buffer)
                if (len < 20) continue

                bytesIn += len
                val response = processIpPacket(buffer, len, dnsSocket) ?: continue
                outStream.write(response)
                bytesOut += response.size
                vpnRepository.updateVpnState(VpnState.Connected(UPSTREAM_DNS, bytesIn, bytesOut))
            }
        } finally {
            dnsSocket.close()
        }
    }

    // ── Packet parsing ───────────────────────────────────────────────────────

    private fun processIpPacket(packet: ByteArray, len: Int, dnsSocket: DatagramSocket): ByteArray? {
        // Only handle IPv4
        if ((packet[0].toInt() ushr 4) and 0xF != 4) return null
        val ihl = (packet[0].toInt() and 0xF) * 4
        if (ihl < 20 || ihl + 8 > len) return null
        // Only handle UDP
        if (packet[9].toInt() and 0xFF != UDP_PROTO) return null
        // Only handle DNS (destination port 53)
        val dstPort = ((packet[ihl + 2].toInt() and 0xFF) shl 8) or (packet[ihl + 3].toInt() and 0xFF)
        if (dstPort != DNS_PORT) return null

        return handleDnsQuery(packet, len, ihl, dnsSocket)
    }

    private fun handleDnsQuery(ipPacket: ByteArray, len: Int, ihl: Int, dnsSocket: DatagramSocket): ByteArray? {
        val udpStart = ihl + 8
        if (udpStart >= len) return null
        val dnsPayload = ipPacket.copyOfRange(udpStart, len)
        if (dnsPayload.size < 12) return null

        val clientIp   = ipPacket.copyOfRange(12, 16)
        val clientPort = ((ipPacket[ihl].toInt() and 0xFF) shl 8) or (ipPacket[ihl + 1].toInt() and 0xFF)

        val domain = parseDnsDomain(dnsPayload)
        if (domain != null && MalwareBlocklist.isMaliciousDomain(domain)) {
            blockedDomains++
            updateNotification("Blocked: $domain (total $blockedDomains)")
            notificationHelper.showSecurityAlert(
                this,
                "Malicious domain blocked",
                "DNS query for \"$domain\" was blocked by NFC Security VPN."
            )
            return buildNxdomainResponse(clientIp, clientPort, dnsPayload)
        }

        return forwardDns(clientIp, clientPort, dnsPayload, dnsSocket)
    }

    /**
     * Walks the QNAME labels in a DNS query payload and returns the fully-qualified domain name.
     * DNS payload layout: 12-byte header, then QNAME (length-prefixed labels, terminated by 0x00).
     */
    private fun parseDnsDomain(dns: ByteArray): String? {
        if (dns.size < 13) return null
        val sb  = StringBuilder()
        var pos = 12   // skip 12-byte DNS header
        while (pos < dns.size) {
            val labelLen = dns[pos].toInt() and 0xFF
            if (labelLen == 0) break
            if (pos + labelLen >= dns.size) return null
            if (sb.isNotEmpty()) sb.append('.')
            sb.append(String(dns, pos + 1, labelLen, Charsets.US_ASCII))
            pos += labelLen + 1
        }
        return sb.toString().lowercase().ifEmpty { null }
    }

    /**
     * Builds an NXDOMAIN response for the given DNS query.
     * Flags: QR=1 RD=1 RA=1 RCODE=3 (NXDOMAIN). Answer/NS/AR counts = 0.
     */
    private fun buildNxdomainResponse(clientIp: ByteArray, clientPort: Int, dnsQuery: ByteArray): ByteArray {
        val resp = dnsQuery.copyOf()
        resp[2] = 0x81.toByte()  // QR=1, OPCODE=0, AA=0, TC=0, RD=1
        resp[3] = 0x83.toByte()  // RA=1, Z=0, RCODE=3
        resp[6] = 0; resp[7]  = 0  // ANCOUNT = 0
        resp[8] = 0; resp[9]  = 0  // NSCOUNT = 0
        resp[10] = 0; resp[11] = 0  // ARCOUNT = 0
        return buildIpUdpPacket(
            srcIp   = ipToBytes(VIRTUAL_DNS),
            dstIp   = clientIp,
            srcPort = DNS_PORT,
            dstPort = clientPort,
            payload = resp
        )
    }

    /**
     * Forwards [dnsPayload] to the upstream resolver (1.1.1.1:53) via a protected socket,
     * waits for the response, and wraps it in an IP/UDP packet addressed back to the client.
     */
    private fun forwardDns(
        clientIp: ByteArray,
        clientPort: Int,
        dnsPayload: ByteArray,
        socket: DatagramSocket
    ): ByteArray? {
        return try {
            val upstream = InetAddress.getByName(UPSTREAM_DNS)
            socket.send(DatagramPacket(dnsPayload, dnsPayload.size, upstream, DNS_PORT))
            val buf = ByteArray(512)
            val rp  = DatagramPacket(buf, buf.size)
            socket.receive(rp)
            buildIpUdpPacket(
                srcIp   = ipToBytes(VIRTUAL_DNS),
                dstIp   = clientIp,
                srcPort = DNS_PORT,
                dstPort = clientPort,
                payload = buf.copyOf(rp.length)
            )
        } catch (_: SocketTimeoutException) {
            null   // upstream didn't answer in time; client's resolver will retry
        } catch (_: Exception) {
            null
        }
    }

    // ── Packet construction ──────────────────────────────────────────────────

    /**
     * Builds a minimal IPv4/UDP datagram carrying [payload].
     * IP checksum is computed; UDP checksum is left as 0 (optional for IPv4).
     */
    private fun buildIpUdpPacket(
        srcIp: ByteArray,
        dstIp: ByteArray,
        srcPort: Int,
        dstPort: Int,
        payload: ByteArray
    ): ByteArray {
        val udpLen = 8 + payload.size
        val ipLen  = 20 + udpLen
        val pkt    = ByteArray(ipLen)

        // IPv4 header
        pkt[0]  = 0x45.toByte()                            // version=4, IHL=5 (20 bytes)
        pkt[2]  = (ipLen ushr 8).toByte()
        pkt[3]  = (ipLen and 0xFF).toByte()
        pkt[6]  = 0x40                                      // DF flag
        pkt[8]  = 64                                        // TTL
        pkt[9]  = UDP_PROTO.toByte()
        srcIp.copyInto(pkt, 12)
        dstIp.copyInto(pkt, 16)
        val cs  = ipv4Checksum(pkt, 0, 20)
        pkt[10] = (cs ushr 8).toByte()
        pkt[11] = (cs and 0xFF).toByte()

        // UDP header
        pkt[20] = (srcPort ushr 8).toByte(); pkt[21] = (srcPort and 0xFF).toByte()
        pkt[22] = (dstPort ushr 8).toByte(); pkt[23] = (dstPort and 0xFF).toByte()
        pkt[24] = (udpLen ushr 8).toByte();  pkt[25] = (udpLen and 0xFF).toByte()
        // [26..27] UDP checksum = 0 (disabled, valid for IPv4)

        payload.copyInto(pkt, 28)
        return pkt
    }

    /** One's-complement 16-bit checksum over [len] bytes starting at [off]. */
    private fun ipv4Checksum(data: ByteArray, off: Int, len: Int): Int {
        var sum = 0
        var i   = off
        while (i < off + len - 1) {
            sum += ((data[i].toInt() and 0xFF) shl 8) or (data[i + 1].toInt() and 0xFF)
            i   += 2
        }
        while (sum ushr 16 != 0) sum = (sum and 0xFFFF) + (sum ushr 16)
        return sum.inv() and 0xFFFF
    }

    private fun ipToBytes(ip: String): ByteArray = InetAddress.getByName(ip).address
}
