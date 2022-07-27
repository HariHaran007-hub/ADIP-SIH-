package com.rcappstudio.adip.utils

import android.app.Activity
import android.app.AlertDialog
import android.widget.TextView
import com.rcappstudio.adip.R

class NoInternetDialog(val mActivity : Activity) {

    private lateinit var isdialog : AlertDialog
    private lateinit var textView : TextView

    fun startLoading(){
        val inflater = mActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_dialog, null)
        val textView = dialogView.findViewById<TextView>(R.id.textView)
        textView.text = "No internet connection"
        val builder = AlertDialog.Builder(mActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        isdialog = builder.create()
        isdialog.show()
    }

    fun isDismiss(){
        isdialog.dismiss()
    }


}