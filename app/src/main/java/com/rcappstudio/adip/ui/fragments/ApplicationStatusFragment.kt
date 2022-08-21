package com.rcappstudio.adip.ui.fragments

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.rcappstudio.adip.adapter.RequestStatusAdapter
import com.rcappstudio.adip.data.model.*
import com.rcappstudio.adip.databinding.FragmentApplicationStatusBinding
import com.rcappstudio.adip.utils.Constants
import com.rcappstudio.adip.utils.LoadingDialog
import java.util.*

class ApplicationStatusFragment : Fragment() {

    private lateinit var binding: FragmentApplicationStatusBinding

    private lateinit var databaseReference: DatabaseReference

    private lateinit var userPath: String
    private lateinit var pref: SharedPreferences
    private lateinit var state : String
    private lateinit var district : String
    private lateinit var userId: String
    private lateinit var loadingDialog: LoadingDialog

    private lateinit var translator : Translator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentApplicationStatusBinding.inflate(layoutInflater)
        binding.customToolbar.toolbar.title = "Status bar"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareModel()
        loadingDialog = LoadingDialog(requireActivity(), "Loading application status....")
        //loadingDialog.startLoading()
        initDataModule()
    }

    private fun initDataModule() {
        pref = requireContext().getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE)
        userPath = pref.getString(Constants.USER_PROFILE_PATH, null)!!
        state = pref.getString(Constants.STATE , null)!!
        district = pref.getString(Constants.DISTRICT, null)!!
        userId = FirebaseAuth.getInstance().uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference(userPath)
        loadDatabase()
    }

    private fun loadDatabase(){

        val requestStatusList = mutableListOf<RequestStatus>()
        FirebaseDatabase.getInstance()
            .getReference("$userPath/${Constants.REQUEST_STATUS}")
            .get().addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    binding.rvRequestStatus.visibility = View.VISIBLE
                    binding.lottieFile.visibility = View.GONE
                    binding.tvNoStatus.visibility = View.GONE

                    for(c in snapshot.children){
                        val value = c.getValue(RequestStatus::class.java)
                        requestStatusList.add(value!!)
                    }

                    initRecyclerView(requestStatusList)
                } else{
                    //TODO: Show no application status
                    binding.rvRequestStatus.visibility = View.GONE
                    binding.lottieFile.visibility = View.VISIBLE
                    binding.tvNoStatus.visibility = View.VISIBLE
                }
            }

    }

    private fun initRecyclerView(requestStatusList : MutableList<RequestStatus>){
        binding.rvRequestStatus.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, true)
        binding.rvRequestStatus.adapter = RequestStatusAdapter(requireContext(), requestStatusList, translator)
    }


    private fun prepareModel(){
        val sharedPreferences = requireContext().getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE)
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
            translator.translate(binding.customToolbar.toolbar.title.toString()).addOnSuccessListener {
                binding.customToolbar.toolbar.title = it
            }
            translator.translate(binding.tvNoStatus.text.toString()).addOnSuccessListener {
                binding.tvNoStatus.text = it
            }
        }
    }


}