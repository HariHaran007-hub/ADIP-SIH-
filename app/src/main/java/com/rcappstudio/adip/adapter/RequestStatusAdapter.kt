package com.rcappstudio.adip.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.mlkit.nl.translate.Translator
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.model.RequestStatus
import com.rcappstudio.adip.utils.getDateTime

class RequestStatusAdapter(
    private val context: Context,
    private var requestStatusList : MutableList<RequestStatus>,
    private var translator : Translator
) : RecyclerView.Adapter<RequestStatusAdapter.ViewHolder>() {

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view)  {
        val tvApplicationStatus = view.findViewById<TextView>(R.id.tvApplicationStatus)
        val tvAppliedOn = view.findViewById<TextView>(R.id.tvAppliedOn)
        val tvAidsApplies = view.findViewById<TextView>(R.id.tvAidsApplied)
        val statusCardView = view.findViewById<MaterialCardView>(R.id.statusCardView)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val campAllocated = view.findViewById<TextView>(R.id.campAllocated)
        val rvcamps = view.findViewById<RecyclerView>(R.id.rvCamps)
        val rootCardView = view.findViewById<MaterialCardView>(R.id.rootCardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_application, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val requestStatus = requestStatusList[position]

        if(requestStatus.verified){
            holder.rootCardView.strokeColor = ContextCompat.getColor(context, R.color.greenLight)
            holder.rootCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.greenLightLight))
        } else{
            holder.rootCardView.strokeColor = ContextCompat.getColor(context, R.color.redLight)
            holder.rootCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLightLight))
        }
        translator.translate("Application: ${position+1}").addOnSuccessListener {
            holder.tvApplicationStatus.text = it
        }

        translator.translate("Submitted on: ${getDateTime(requestStatus.appliedOnTimeStamp!!)}").addOnSuccessListener {
            holder.tvAppliedOn.text = it
        }
        var aidsTextData = ""
        var count = 1
        for(aid in requestStatus.aidsList!!){
            if(count == 1){
                translator.translate("Aids applied for: $count)$aid").addOnSuccessListener {
                    aidsTextData = aidsTextData + "\n" +
                            "\t\t\t\t\t\t$it"
                    holder.tvAidsApplies.text = aidsTextData
                    count++
                }
            } else {
                translator.translate(aid).addOnSuccessListener {
                    aidsTextData = aidsTextData + "\n\t\t\t\t\t\t$count)$it"
                    holder.tvAidsApplies.text = aidsTextData
                    count++
                }
            }

        }


        if(!requestStatus.verified){
            translator.translate("Application not yet verified").addOnSuccessListener {
                holder.tvStatus.text = it
            }
            holder.statusCardView.strokeColor = ContextCompat.getColor(context, R.color.red)
            holder.statusCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLight))
        } else if(requestStatus.notAppropriate){
            translator.translate("Application rejected apply with new application").addOnSuccessListener {
                holder.tvStatus.text = it
            }
            holder.statusCardView.strokeColor = ContextCompat.getColor(context, R.color.red)
            holder.statusCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLight))
        }
        else{
            holder.statusCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.greenLight))
            holder.statusCardView.strokeColor = ContextCompat.getColor(context, R.color.green)
            translator.translate("Application verified successfully").addOnSuccessListener {
                holder.tvStatus.text = it
            }
            holder.rvcamps.visibility = View.VISIBLE
            holder.campAllocated.visibility = View.VISIBLE
            translator.translate(holder.campAllocated.text.toString()).addOnSuccessListener {
                holder.campAllocated.text = it
            }
            holder.rvcamps.layoutManager = LinearLayoutManager(context)
            holder.rvcamps.adapter = AgenciesAdapter(context, requestStatus.ngoList!!.values.toMutableList(), translator)
            holder.statusCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.greenLight))
        }
    }

    override fun getItemCount(): Int {
        return requestStatusList.size
    }
}