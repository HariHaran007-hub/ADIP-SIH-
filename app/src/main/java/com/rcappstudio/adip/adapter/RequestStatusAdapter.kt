package com.rcappstudio.adip.adapter


import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.collection.arraySetOf
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anton46.stepsview.StepsView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.collection.LLRBNode
import com.google.mlkit.nl.translate.Translator
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.model.RequestStatus
import com.rcappstudio.adip.utils.getDateTime
import java.util.ArrayList


class RequestStatusAdapter(
    private val context: Context,
    private var requestStatusList : MutableList<RequestStatus>,
    private var translator : Translator,
    val voiceUrl : String,
    val disabilityCategory : String
) : RecyclerView.Adapter<RequestStatusAdapter.ViewHolder>() {

    private var current_position = 0

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view)  {
        val tvApplicationStatus = view.findViewById<TextView>(R.id.tvApplicationStatus)
        val tvAppliedOn = view.findViewById<TextView>(R.id.tvAppliedOn)
        val tvAidsApplies = view.findViewById<TextView>(R.id.tvAidsApplied)
        val statusCardView = view.findViewById<MaterialCardView>(R.id.statusCardView)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val campAllocated = view.findViewById<TextView>(R.id.campAllocated)
        val rvcamps = view.findViewById<RecyclerView>(R.id.rvCamps)
        val rootCardView = view.findViewById<MaterialCardView>(R.id.rootCardView)
        val header = view.findViewById<TextView>(R.id.tvAidsAppliedHeader)
        val stepView = view.findViewById<StepsView>(R.id.stepsView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_application, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val requestStatus = requestStatusList[position]
        val descriptionData = arraySetOf("Document uploaded","Camp allotment","Aids received")




        holder.stepView.setLabels(arrayOf("Docs uploaded","Verification","Allotment"))
            .barColorIndicator = Color.LTGRAY

        holder.stepView.setProgressColorIndicator(context.resources.getColor(R.color.pink))
            .setLabelColorIndicator(context.resources.getColor(R.color.pink))
            .setCompletedPosition(0)
            .drawView()


        if(requestStatus.doctorVerification!!){
            holder.stepView.setCompletedPosition(1)
        }

        if(requestStatus.documentVerified!!){
            holder.stepView.setCompletedPosition(2)
        }

//        holder.stepView.setCompletedPosition(2)


        if(requestStatus.documentVerified!!){
            holder.rootCardView.strokeColor = ContextCompat.getColor(context, R.color.greenLight)
            holder.rootCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.greenLightLight))
        } else{
            holder.rootCardView.strokeColor = ContextCompat.getColor(context, R.color.redLight)
            holder.rootCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLightLight))
        }
        translator.translate("Application: ${position+1}").addOnSuccessListener {
            holder.tvApplicationStatus.text = " " + it
        }

        holder.tvApplicationStatus.setOnLongClickListener {
            playAudio(holder.tvApplicationStatus.text.toString())
            true
        }

        translator.translate(holder.header.text.toString()).addOnSuccessListener {
            holder.header.text = it
        }
        holder.header.setOnLongClickListener{
            playAudio(holder.header.text.toString())
            true
        }

        translator.translate("Submitted on: ${getDateTime(requestStatus.appliedOnTimeStamp!!)}").addOnSuccessListener {
            holder.tvAppliedOn.text = it
        }

        holder.tvAppliedOn.setOnLongClickListener {
            playAudio(holder.tvAppliedOn.text.toString())
            true
        }
        var aidsTextData = "\t\t\t\t\t\t"
        var count = 0
//        for(aid in requestStatus.aidsList!!){
//            count++
//            if(count == 1){
//                translator.translate("$count)$aid").addOnSuccessListener {
//                    aidsTextData = aidsTextData + "\t\t\t\t\t\t$it"
//                    holder.tvAidsApplies.text = aidsTextData
//                }
//                holder.tvAidsApplies.setOnLongClickListener {
//                    playAudio(holder.tvAidsApplies.text.toString())
//                    true
//                }
//            } else {
//                translator.translate(aid).addOnSuccessListener {
//                    aidsTextData = aidsTextData + "\n\t\t\t\t\t\t$count)$it"
//                    holder.tvAidsApplies.text = aidsTextData
//                    count++
//                }
//                holder.tvAidsApplies.setOnLongClickListener {
//                    playAudio(holder.tvAidsApplies.text.toString())
//                    true
//                }
//            }
//        }

        holder.tvAidsApplies.text = "Disability Category: ${disabilityCategory}"


        if(requestStatus.notAppropriate){
            translator.translate("Application rejected apply with new application. Reason: ${requestStatus.message.toString()}").addOnSuccessListener {
                holder.tvStatus.text = it
            }
            holder.tvStatus.setOnLongClickListener {
                playAudio(holder.tvStatus.text.toString())
                true
            }
            holder.statusCardView.strokeColor = ContextCompat.getColor(context, R.color.red)
            holder.statusCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLight))
        } else{
            if(!requestStatus.documentVerified!! && !requestStatus.doctorVerification!!){
                translator.translate("Application not yet verified").addOnSuccessListener {
                    holder.tvStatus.text = it
                }
                holder.tvStatus.setOnLongClickListener {
                    playAudio(holder.tvStatus.text.toString())
                    true
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

                holder.tvStatus.setOnLongClickListener {
                    playAudio(holder.tvStatus.text.toString())
                    true
                }
                holder.rvcamps.visibility = View.VISIBLE
                holder.campAllocated.visibility = View.VISIBLE
                holder.campAllocated.setOnLongClickListener {
                    playAudio(holder.campAllocated.text.toString())
                    true
                }
                translator.translate(holder.campAllocated.text.toString()).addOnSuccessListener {
                    holder.campAllocated.text = it
                }
                holder.campAllocated.setOnLongClickListener {
                    playAudio(holder.campAllocated.text.toString())
                    true
                }
                if(requestStatus.ngoList!!.isNotEmpty()){
                    holder.rvcamps.layoutManager = LinearLayoutManager(context)
                    holder.rvcamps.adapter = AgenciesAdapter(context, requestStatus.ngoList!!.values.toMutableList(), translator, voiceUrl)
                    holder.statusCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.greenLight))
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return requestStatusList.size
    }

    private fun playAudio(text : String) {
        val mp = MediaPlayer()
        mp.setDataSource(voiceUrl + text)
        mp.prepare()
        mp.start()

    }

}