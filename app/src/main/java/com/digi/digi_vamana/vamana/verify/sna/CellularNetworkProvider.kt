package com.digi.digi_vamana.vamana.verify.sna

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Real Android connectivity APIs backing the Silent Network Authentication
 * (SNA) demo — no simulation here, unlike the SIM-binding state the demo
 * lets you toggle. Detects which transport is genuinely active (cellular vs
 * Wi-Fi) and can request a Network explicitly bound to TRANSPORT_CELLULAR.
 * If the device truly has no active cellular data session,
 * [requestCellularNetwork] genuinely returns null.
 */
class CellularNetworkProvider(context: Context) {

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var activeCallback: ConnectivityManager.NetworkCallback? = null

    /** "CELLULAR", "WIFI", "OTHER", or "NONE" — the transport actually in use right now. */
    fun detectActiveTransport(): String {
        val network = connectivityManager.activeNetwork ?: return "NONE"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "NONE"
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            else -> "OTHER"
        }
    }

    suspend fun requestCellularNetwork(timeoutMs: Long = 4000): Network? =
        suspendCancellableCoroutine { continuation ->
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    if (continuation.isActive) continuation.resumeWith(Result.success(network))
                }

                override fun onUnavailable() {
                    if (continuation.isActive) continuation.resumeWith(Result.success(null))
                }
            }
            activeCallback = callback
            connectivityManager.requestNetwork(request, callback)

            continuation.invokeOnCancellation { release() }

            Handler(Looper.getMainLooper()).postDelayed({
                if (continuation.isActive) continuation.resumeWith(Result.success(null))
            }, timeoutMs)
        }

    /** Call once the cellular-bound request is done, so the radio isn't held open. */
    fun release() {
        activeCallback?.let { runCatching { connectivityManager.unregisterNetworkCallback(it) } }
        activeCallback = null
    }
}
