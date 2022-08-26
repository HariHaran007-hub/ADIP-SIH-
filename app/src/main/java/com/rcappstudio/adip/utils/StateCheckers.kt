package com.rcappstudio.adip.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

val snakeRegex = " [a-zA-Z]".toRegex()

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
fun getDateTime(s: Long): String? {
    return try {
        val sdf = android.icu.text.SimpleDateFormat("dd/MM/yyyy")
        val netDate = Date(s)
        sdf.format(netDate)
    } catch (e: Exception) {
        e.toString()
    }
}
fun String.snakeToLowerCamelCase(): String {
    return snakeRegex.replace(this) {
        it.value.replace(" ","")
            .toUpperCase()
    }
}

 fun getAge(dobString: String): Int {
    var date: Date? = null
    val sdf = SimpleDateFormat("dd-MM-yyyy")
    try {
        date = sdf.parse(dobString)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    if (date == null) return 0
    val dob = Calendar.getInstance()
    val today = Calendar.getInstance()
    dob.time = date
    val year = dob[Calendar.YEAR]
    val month = dob[Calendar.MONTH]
    val day = dob[Calendar.DAY_OF_MONTH]
    dob[year, month + 1] = day
    var age = today[Calendar.YEAR] - dob[Calendar.YEAR]
    if (today[Calendar.DAY_OF_YEAR] < dob[Calendar.DAY_OF_YEAR]) {
        age--
    }
    return age
}