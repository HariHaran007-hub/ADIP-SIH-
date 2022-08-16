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
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.model.RequestStatus
import com.rcappstudio.adip.utils.getDateTime

class RequestStatusAdapter(
    private val context: Context,
    private var requestStatusList : MutableList<RequestStatus>
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
        holder.tvApplicationStatus.text = "Application ${position+1}"
        holder.tvAppliedOn.text = "Applied on: ${getDateTime(requestStatus.appliedOnTimeStamp!!)}"
        var aidsTextData = "Aids applied for:"
        var count = 0
        for(aid in requestStatus.aidsList!!){
            count++
            aidsTextData = aidsTextData + "\n\t\t\t\t\t\t$count) $aid"
        }
        holder.tvAidsApplies.text = aidsTextData

        if(!requestStatus.verified){
            holder.tvStatus.text = "Application not yet verified"
            holder.statusCardView.setStrokeColor(ContextCompat.getColor(context, R.color.red))
            holder.statusCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLight))
        } else if(requestStatus.notAppropriate){
            holder.tvStatus.text = "Application rejected apply with new application"
            holder.statusCardView.setStrokeColor(ContextCompat.getColor(context, R.color.red))
            holder.statusCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLight))
        }
        else{
            holder.statusCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.greenLight))
            holder.statusCardView.setStrokeColor(ContextCompat.getColor(context, R.color.green))
            holder.tvStatus.text = "Application verified successfully"
            holder.rvcamps.visibility = View.VISIBLE
            holder.campAllocated.visibility = View.VISIBLE
            holder.rvcamps.layoutManager = LinearLayoutManager(context)
            holder.rvcamps.adapter = AgenciesAdapter(context, requestStatus.ngoList!!.values.toMutableList())
            holder.statusCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.greenLight))
        }
    }

    override fun getItemCount(): Int {
        return requestStatusList.size
    }
}