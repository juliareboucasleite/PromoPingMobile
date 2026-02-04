package com.example.promopingmobile.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.util.Log
import com.example.promopingmobile.BuildConfig

class NetworkConfig(private val context: Context) {
    private val ssidToBaseUrl: Map<String, String> = mapOf(
        // "reboucas" to "http://192.168.1.72:3000",
        "Apocalypse" to "http://localhost:3000",
    )

    fun currentBaseUrl(): String {
        val ssid = currentSsid()
        val baseUrl = ssid?.let { ssidToBaseUrl[it] } ?: BuildConfig.API_BASE_URL
        Log.d("NetworkConfig", "ssid=$ssid baseUrl=$baseUrl")
        return baseUrl
    }

    private fun currentSsid(): String? {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return null
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null
        if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo ?: return null
        val ssid = info.ssid ?: return null
        if (ssid == "<unknown ssid>") return null
        return ssid.trim('"')
    }
}
