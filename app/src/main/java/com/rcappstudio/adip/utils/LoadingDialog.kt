package com.rcappstudio.adip.utils

import android.app.Activity
import android.app.AlertDialog
import android.widget.TextView
import com.rcappstudio.adip.R

class LoadingDialog(val mActivity : Activity , val message : String?) {

    private lateinit var isdialog : AlertDialog
    private lateinit var textView : TextView

    fun startLoading(){
        val inflater = mActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_dialog, null)
        val textView = dialogView.findViewById<TextView>(R.id.textView)
        textView.text = message
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