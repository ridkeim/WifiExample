package ru.ridkeim.wifiexample

data class AccessPoint(
    val ssid: String,
    val bssid: String,
    val channel: Int,
    val level: Int,
    val security: Int,
    val wps: Boolean
)
