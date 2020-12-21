package ru.ridkeim.wifiexample

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val wifiManager by lazy{
        applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    }
    private lateinit var infoTextView: TextView
    private val wifiStateReceiver = object : BroadcastReceiver(){
        @SuppressLint("SetTextI18n")
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        infoTextView = findViewById(R.id.textView)
        findViewById<Button>(R.id.button).setOnClickListener {
            when (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )){
                 PackageManager.PERMISSION_GRANTED -> {
                    startScanActivity()
                 }
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            PERMISSION_REQUEST_CODE
                        )
                    }
                }
            }
        }
    }

    private fun getDeviceCurrentIPAddress(): String {
        val connectionInfo = wifiManager.connectionInfo
        val ip = connectionInfo.ipAddress
        return String.format("%d.%d.%d.%d", ip and 0xff, ip shr 8 and 0xff,
                ip shr 16 and 0xff, ip shr 24 and 0xff)
    }

    private fun startScanActivity(){
        if(!wifiManager.isWifiEnabled){
            AlertDialog.Builder(this)
                    .setTitle("Wifi выключен!")
                    .setMessage("Для работы сканера включите Wifi")
                    .setPositiveButton("Ок"){ dialog, _ ->
                        dialog.cancel()
                    }
                    .create()
                    .show()
        }else{
            startActivity(Intent(this,ScanActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(wifiStateReceiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(wifiStateReceiver)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    startScanActivity()
                } else {
                    Log.d(TAG,"")
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 42
        private val TAG = MainActivity::class.qualifiedName
    }
}