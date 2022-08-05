package com.rcappstudio.adip.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

fun isConnected(context : Context): Boolean {
    val cm = context
        .getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
        ?: return false
    /* NetworkInfo is deprecated in API 29 so we have to check separately for higher API Levels */return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val network = cm.activeNetwork ?: return false
        val networkCapabilities = cm.getNetworkCapabilities(network) ?: return false
        val isInternetSuspended =
            !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)
        (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                && !isInternetSuspended)
    } else {
        val networkInfo = cm.activeNetworkInfo
        networkInfo != null && networkInfo.isConnected
    }
}

fun timeStampToHrs(timeStamp : Long) : String{

    return SimpleDateFormat("h:mma").format(Date(timeStamp ))
}