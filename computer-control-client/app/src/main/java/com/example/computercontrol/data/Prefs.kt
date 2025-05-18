package com.example.remotepccontroller.data

import android.content.Context
import androidx.core.content.edit

class Prefs(context: Context) {
    private val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var serverIp: String
        get() = sharedPref.getString("server_ip", "") ?: ""
        set(value) = sharedPref.edit { putString("server_ip", value) }
}