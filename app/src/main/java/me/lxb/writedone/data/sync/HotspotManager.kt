package me.lxb.writedone.data.sync

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

enum class Role { HOST, CLIENT, UNKNOWN }

data class HotspotState(
    val role: Role = Role.UNKNOWN,
    val gatewayAddress: InetAddress? = null,
    val localHotspotIp: InetAddress? = null,
    val isConnectedToHotspot: Boolean = false,
    val lastError: String = "",
)

@Singleton
class HotspotManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "HotspotManager"
        private const val SYNC_PORT = 48766
        private const val CONNECT_TIMEOUT_MS = 2000L
    }

    private val _state = MutableStateFlow(HotspotState())
    val state: StateFlow<HotspotState> = _state.asStateFlow()

    fun getGatewayAddress(): InetAddress? {
        val wifi = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return null
        val dhcp = wifi.dhcpInfo ?: return null
        if (dhcp.gateway == 0) return null
        val addr = intToInetAddress(dhcp.gateway)
        Log.d(TAG, "getGatewayAddress: fast path -> ${addr.hostAddress}")
        return addr
    }

    fun getLocalIpAddress(): InetAddress? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        return addr
                    }
                }
            }
            null
        } catch (_: Exception) { null }
    }

    fun startServerSocket(): ServerSocket? {
        return try {
            ServerSocket().apply {
                setReuseAddress(true)
                bind(java.net.InetSocketAddress(SYNC_PORT))
                soTimeout = 10000
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start server socket", e)
            _state.value = _state.value.copy(lastError = "服务器启动失败: ${e.message}")
            null
        }
    }

    suspend fun detectRole(): Pair<Role, Socket?> = withContext(Dispatchers.IO) {
        Log.d(TAG, "detectRole: starting")

        // Fast path: DHCP gateway
        val gateway = getGatewayAddress()
        if (gateway != null) {
            Log.d(TAG, "detectRole: fast path gateway=$gateway:$SYNC_PORT, trying...")
            try {
                val sock = Socket(gateway, SYNC_PORT)
                Log.d(TAG, "detectRole: fast path connected => CLIENT")
                _state.value = _state.value.copy(
                    role = Role.CLIENT,
                    gatewayAddress = gateway,
                    isConnectedToHotspot = true,
                    lastError = "",
                )
                return@withContext Pair(Role.CLIENT, sock)
            } catch (e: Exception) {
                Log.d(TAG, "detectRole: fast path failed (${e.message}), fallback to scan")
            }
        } else {
            Log.d(TAG, "detectRole: no DHCP gateway, fallback to subnet scan")
        }

        // Fallback: scan subnet for HOST on SYNC_PORT
        Log.d(TAG, "detectRole: scanning subnet for port $SYNC_PORT...")
        val hostSocket = scanForHost()
        if (hostSocket != null) {
            Log.d(TAG, "detectRole: scan found host at ${hostSocket.inetAddress.hostAddress} => CLIENT")
            _state.value = _state.value.copy(
                role = Role.CLIENT,
                gatewayAddress = hostSocket.inetAddress,
                isConnectedToHotspot = true,
                lastError = "",
            )
            return@withContext Pair(Role.CLIENT, hostSocket)
        }

        // Nothing found → assume HOST
        val localIp = getLocalIpAddress()
        Log.d(TAG, "detectRole: scan found nothing, assuming HOST, local IP=$localIp")
        _state.value = _state.value.copy(
            role = Role.HOST,
            gatewayAddress = null,
            localHotspotIp = localIp,
            isConnectedToHotspot = false,
            lastError = "",
        )
        Pair(Role.HOST, null)
    }

    private suspend fun scanForHost(): Socket? = coroutineScope {
        val localIp = getLocalIpAddress()
        val subnetBases = computeSubnetBases(localIp)
        val excludeOctet = localIp?.hostAddress?.substringAfterLast('.')?.toIntOrNull()

        val allIps = subnetBases.flatMap { base ->
            (1..254).filter { it != excludeOctet }.map { "$base.$it" }
        }.distinct().shuffled()

        Log.d(TAG, "scanForHost: scanning ${allIps.size} IPs (exclude ${localIp?.hostAddress})")

        val result = CompletableDeferred<Socket?>()
        val jobs = mutableListOf<Job>()

        for (ip in allIps) {
            val job = launch(Dispatchers.IO) {
                try {
                    val sock = Socket()
                    sock.connect(InetSocketAddress(ip, SYNC_PORT), 300)
                    result.complete(sock)
                } catch (_: Exception) {}
            }
            jobs.add(job)
        }

        val socket = withTimeoutOrNull(CONNECT_TIMEOUT_MS) { result.await() }
        jobs.forEach { it.cancel() }

        if (socket != null) {
            Log.d(TAG, "scanForHost: found host at ${socket.inetAddress.hostAddress}")
        } else {
            Log.d(TAG, "scanForHost: no host found")
        }
        socket
    }

    private fun computeSubnetBases(localIp: InetAddress?): List<String> {
        if (localIp != null) {
            val octets = localIp.hostAddress.split('.')
            if (octets.size == 4) return listOf("${octets[0]}.${octets[1]}.${octets[2]}")
        }
        return listOf("192.168.43", "192.168.0", "192.168.1", "192.168.137")
    }

    fun reset() {
        _state.value = HotspotState()
    }

    private fun intToInetAddress(host: Int): InetAddress {
        return InetAddress.getByAddress(
            byteArrayOf(
                (host and 0xFF).toByte(),
                ((host shr 8) and 0xFF).toByte(),
                ((host shr 16) and 0xFF).toByte(),
                ((host shr 24) and 0xFF).toByte(),
            )
        )
    }
}
