package com.rcappstudio.adip.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.nl.translate.Translator
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.model.CampingModel

class CampDetailsAdapter(
    val context : Context,
    val campList : MutableList<CampingModel>,
    val translator: Translator
) : RecyclerView.Adapter<CampDetailsAdapter.ViewHolder>(){
    class ViewHolder(view : View)  : RecyclerView.ViewHolder(view){

        val agencyName = view.findViewById<TextView>(R.id.agencyName)
        val address = view.findViewById<TextView>(R.id.agencyAddress)
        val openMap = view.findViewById<CardView>(R.id.openLoc)
        val dialcall = view.findViewById<CardView>(R.id.dialForCall)
        val agencyNumber = view.findViewById<TextView>(R.id.agencyNumber)

        val tvOpenMap = view.findViewById<TextView>(R.id.tvOpenGoogleMap)
        val tvCall= view.findViewById<TextView>(R.id.tvCall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.camp_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val camp = campList[position]
        translator.translate("Agency name: " + camp.campingName).addOnSuccessListener {
            holder.agencyName.text = it
        }
        if (camp.address != null) {
            translator.translate("Address: " + camp.address).addOnSuccessListener {
                holder.address.text = it
            }
        }
        if (camp.mobileNo != null) {
            translator.translate("Mobile number: " + camp.mobileNo).addOnSuccessListener {
                holder.agencyNumber.text = it
            }
        }

        translator.translate(holder.tvCall.text.toString()).addOnSuccessListener {
            holder.tvCall.text = it
        }

        translator.translate(holder.tvOpenMap.text.toString()).addOnSuccessListener {
            holder.tvOpenMap.text = it
        }

        holder.openMap.setOnClickListener {
            if (camp.location != null) {
                val uri =
                    "http://maps.google.com/maps?daddr=${camp.location!!.lat},${camp.location.lng}" + "(" + "Nearest implementation agency" + ")"
                val gmmIntentUri = Uri.parse(uri)
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(context , mapIntent , null)

            } else {
                Toast.makeText(context, "Location will be updated soon", Toast.LENGTH_LONG).show()
            }
        }

        holder.dialcall.setOnClickListener {

            if(camp.mobileNo != null){
                val intent = Intent(Intent.ACTION_DIAL)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.data = Uri.parse("tel:${camp.mobileNo}")
                context.startActivity(intent)
            } else{
                Toast.makeText(context, "Number will be updated soon", Toast.LENGTH_LONG).show()
            }
        }
    }
    override fun getItemCount(): Int {
        return campList.size
    }
}