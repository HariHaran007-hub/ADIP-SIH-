package com.rcappstudio.adip.ui.news

import java.sql.Timestamp

data class NewsModel(
    val headLines: String ?= null,
    val contentDescription: String ?= null,
    val imageUrl: String ?= null,
    val timeStamp: Long ?= null
)