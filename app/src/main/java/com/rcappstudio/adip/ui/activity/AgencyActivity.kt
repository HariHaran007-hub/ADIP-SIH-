package com.rcappstudio.adip.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.rcappstudio.adip.adapter.CampDetailsAdapter
import com.rcappstudio.adip.data.model.CampingModel
import com.rcappstudio.adip.databinding.ActivityAgencyBinding
import com.rcappstudio.adip.utils.Constants

class AgencyActivity : AppCompatActivity() {
    private lateinit var translator: Translator
    private lateinit var binding : ActivityAgencyBinding
    private lateinit var agencyList: MutableList<CampingModel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgencyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        agencyList = mutableListOf()
        prepareModel()
        fetchAgencyDetails()
    }

    private fun fetchAgencyDetails(){
        FirebaseDatabase.getInstance().getReference(Constants.CAMPING).get()
            .addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    for(s in snapshot.children){
                        val camp = s.getValue(CampingModel::class.java)
                        agencyList.add(camp!!)
                    }
                    setRecyclerView(agencyList)
                }

            }
    }

    private fun prepareModel(){
        val sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE)
            .getString(Constants.LANGUAGE, null)
        if(sharedPreferences != null){
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(sharedPreferences)
                .build()
            translator = Translation.getClient(options)

            translator.downloadModelIfNeeded().addOnSuccessListener {

            }.addOnFailureListener {

            }
        }
    }


    private fun setRecyclerView(agencyList : MutableList<CampingModel>){
        binding.rvAgency.layoutManager =LinearLayoutManager(this)
        binding.rvAgency.adapter = CampDetailsAdapter(applicationContext, agencyList, translator)
    }
}