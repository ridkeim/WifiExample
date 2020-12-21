package ru.ridkeim.wifiexample

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ScanActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private val wifiManager by lazy{
        applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    }

    private val scanReceiver = object : BroadcastReceiver(){
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            val scanResults = wifiManager.scanResults
            val success = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED,false) ?: false
            } else {
                true
            }
            if(success){
                var bestSignal: ScanResult? = null
                textView.text = "Scan result: ${scanResults.size} points"

                for (scanResult in scanResults){
                    if(bestSignal == null || WifiManager.compareSignalLevel(bestSignal.level, scanResult.level) < 0){
                        bestSignal = scanResult
                    }
                    textView.append("\nSSID: ${scanResult.SSID}")
                    textView.append("\nLevel: ${scanResult.level} dBm")
                    textView.append("\nFrequency: ${scanResult.frequency} MHz")
                    textView.append("\nCapabilities: ${scanResult.capabilities}")
                }
                textView.append("\nBest signal: ${bestSignal?.SSID ?: ""}")
            } else {
                textView.text = "Something goes wrong"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        textView = findViewById(R.id.textView)
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