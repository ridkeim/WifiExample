package ru.ridkeim.wifiexample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class ScanActivity : AppCompatActivity() {
    private lateinit var radarView : RadarView
    private val maxSignalLevel = 100
    private val wifiManager by lazy{
        applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    }
    private val channelsFrequency: List<Int> = listOf(
        0,
        2412,
        2417,
        2422,
        2427,
        2432,
        2437,
        2442,
        2447,
        2452,
        2457,
        2462,
        2467,
        2472,
        2484
    )

    private val accessPoints = mutableListOf<AccessPoint>()

    fun getChannelFromFrequency(frequency: Int): Int {
        return channelsFrequency.indexOf(frequency)
    }

    fun getSecurity(capabilities: String): Int {
        return if (capabilities.contains("[WPA")) 2 else if (capabilities.contains("[WEP")) 1 else 0
    }

    fun getWPS(capabilities: String): Boolean {
        return capabilities.contains("[WPS]")
    }
    private val scanReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val success = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) ?: false
            } else {
                true
            }
            if(success){
                val scanResults = wifiManager.scanResults
                accessPoints.clear()
                for (scanResult in scanResults) {
                    @Suppress("DEPRECATION") val accessPoint = AccessPoint(
                        scanResult.SSID,
                        scanResult.BSSID,
                        getChannelFromFrequency(scanResult.frequency),
                        WifiManager.calculateSignalLevel(scanResult.level, maxSignalLevel),
                        getSecurity(scanResult.capabilities),
                        getWPS(scanResult.capabilities)
                    )
                    accessPoints.add(accessPoint)
                }
                radarView.setData(accessPoints)
            } else {
                Toast.makeText(context,"Something goes wrong",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        radarView = findViewById(R.id.radarView)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(scanReceiver)
    }

    @Suppress("DEPRECATION")
    override fun onResume() {
        super.onResume()
        registerReceiver(scanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()
    }

}