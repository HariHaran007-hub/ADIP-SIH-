package com.rcappstudio.adip.notifications


data class PushNotification(
    val data: NotificationData,
    val to: String
)