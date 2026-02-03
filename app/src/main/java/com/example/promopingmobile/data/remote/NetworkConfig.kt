package com.example.promopingmobile.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.example.promopingmobile.BuildConfig

class NetworkConfig(private val context: Context) {
    private val ssidToBaseUrl: Map<String, String> = mapOf(
        // TODO: add your SSID -> base URL mapping
        // "MinhaRedeWifi" to "http://192.168.1.72:3000"
    )

    fun currentBaseUrl(): String {
        val ssid = currentSsid() ?: return BuildConfig.API_BASE_URL
        return ssidToBaseUrl[ssid] ?: BuildConfig.API_BASE_URL
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
