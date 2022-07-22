package com.rcappstudio.adip.data

interface ClickListener {

    fun buttonClick()

    fun buttonClick(position: Int, type: String)

    fun buttonClick(position: Int)

}