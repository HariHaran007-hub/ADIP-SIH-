package com.rcappstudio.adip.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.nl.translate.Translator
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.model.CampingModel
import com.rcappstudio.adip.data.model.NgoData
import com.rcappstudio.adip.utils.Constants
import org.w3c.dom.Text

class AgenciesAdapter (
    val context : Context,
    val ngoList : List<NgoData>,
    private var translator : Translator
        ) : RecyclerView.Adapter<AgenciesAdapter.ViewHolder>(){

    class ViewHolder(view : View)  : RecyclerView.ViewHolder(view){
        val agencyName = view.findViewById<TextView>(R.id.agencyName)
        val aidsList = view.findViewById<TextView>(R.id.aidsList)
        val googleMapButton = view.findViewById<ImageView>(R.id.googleMapButton)
        val cardView = view.findViewById<MaterialCardView>(R.id.ngoCardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_agency, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ngoIdData = ngoList[position]
        if(ngoIdData.aidsReceived!!){
            holder.cardView.strokeColor = ContextCompat.getColor(context, R.color.green)
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.greenLight))
        } else{
            holder.cardView.strokeColor = ContextCompat.getColor(context, R.color.red)
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLight))
        }
        FirebaseDatabase.getInstance().getReference("${Constants.CAMPING}/${ngoIdData.ngoId}")
            .get().addOnSuccessListener { snapshot->
                if (snapshot.exists()){
                  val ngo = snapshot.getValue(CampingModel::class.java)
                    translator.translate("Agency name: "+ ngo!!.campingName.toString()).addOnSuccessListener {
                        holder.agencyName.text = it
                    }
                  holder.googleMapButton.setOnClickListener {
                      if(ngo.location != null){
                          val uri =
                              "http://maps.google.com/maps?daddr=${ngo.location!!.lat},${ngo.location.lng}" + "(" + "Nearest implementation agency" + ")"
                          val gmmIntentUri = Uri.parse(uri)
                          val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                          mapIntent.setPackage("com.google.android.apps.maps")
                          context.startActivity(mapIntent)
                      } else{
                          Toast.makeText(context, "Location will be updated soon", Toast.LENGTH_LONG).show()
                      }
                  }
                } else{
                    Toast.makeText(context, "Error occured", Toast.LENGTH_LONG).show()
                }
            }
        var count = 0
        var aidsText = ""
        for(aid in ngoIdData.aidsList!!){
            if(count == 0){
                translator.translate("Aids allocated: $aid").addOnSuccessListener {
                    aidsText += it+", "
                    holder.aidsList.text  =aidsText
                    count++
                }
            } else {
                translator.translate(aid).addOnSuccessListener {
                    aidsText += it+", "
                    holder.aidsList.text  =aidsText
                }
            }

        }

    }

    override fun getItemCount(): Int {
        return ngoList.size
    }
}