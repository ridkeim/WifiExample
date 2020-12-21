package ru.ridkeim.wifiexample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val wifiManager by lazy{
        applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    }
    private lateinit var infoTextView: TextView
    private val wifiStateReceiver = WifiStateReceiver()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        infoTextView = findViewById(R.id.textView)

    }

    private fun getDeviceCurrentIPAddress(): String {
        val connectionInfo = wifiManager.connectionInfo
        val ip = connectionInfo.ipAddress
        return String.format("%d.%d.%d.%d", ip and 0xff, ip shr 8 and 0xff,
                ip shr 16 and 0xff, ip shr 24 and 0xff)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(wifiStateReceiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(wifiStateReceiver)
    }

    inner class WifiStateReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val extraWifiState = intent!!.getIntExtra(
                    WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN)

            when (extraWifiState) {
                WifiManager.WIFI_STATE_DISABLED -> infoTextView.text = "WIFI недоступен"
                WifiManager.WIFI_STATE_DISABLING -> infoTextView.text = "WIFI отключается"
                WifiManager.WIFI_STATE_ENABLED -> infoTextView.text = "WIFI доступен"
                WifiManager.WIFI_STATE_ENABLING -> infoTextView.text = "WIFI включается"
                WifiManager.WIFI_STATE_UNKNOWN -> infoTextView.text = "WIFI: неизвестное состояние"
            }
            infoTextView.append("\n${getDeviceCurrentIPAddress()}")
        }
    }

}