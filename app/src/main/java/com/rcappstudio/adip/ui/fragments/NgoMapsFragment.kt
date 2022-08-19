package com.rcappstudio.adip.ui.fragments

import android.content.Context
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.model.CampingModel
import com.rcappstudio.adip.databinding.FragmentHomeBinding
import com.rcappstudio.adip.databinding.FragmentNgoMapsBinding
import com.rcappstudio.adip.utils.Constants

class NgoMapsFragment : Fragment() {

    private lateinit var translator: Translator
    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentNgoMapsBinding

    private val callback = OnMapReadyCallback { googleMap ->
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        map = googleMap
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNgoMapsBinding.inflate(layoutInflater)
        //binding.customToolBar.toolbar.title = "Camp locations"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        prepareModel()
        fetchCampingLocations()
    }


    private fun fetchCampingLocations(){
        FirebaseDatabase.getInstance().getReference(Constants.CAMPING)
            .get().addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    for(loc in snapshot.children){
                        val camp = loc.getValue(CampingModel::class.java)
                        if(camp!!.location != null){
                            val location = camp!!.location
                            val latLng = LatLng(location?.lat!!.toDouble(), location.lng!!.toDouble())
                            map!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                            map!!.addMarker(MarkerOptions().position(latLng)
                                .icon(
                                    BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_RED)))
                        }
                    }
                }
            }
    }
    private fun prepareModel(){
        val sharedPreferences = requireContext().getSharedPreferences(Constants.SHARED_PREF_FILE,
            Context.MODE_PRIVATE
        )
            .getString(Constants.LANGUAGE, null)
        if(sharedPreferences != null){
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(sharedPreferences)
                .build()
            translator = Translation.getClient(options)

            translator.downloadModelIfNeeded().addOnSuccessListener {
                translator.translate(binding.campLoc.locTv.text.toString()).addOnSuccessListener {
                    binding.campLoc.locTv.text = it
                }
            }.addOnFailureListener {

            }
        }
    }
}