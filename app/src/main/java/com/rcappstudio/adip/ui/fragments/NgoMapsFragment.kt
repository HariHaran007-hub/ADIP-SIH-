package com.rcappstudio.adip.ui.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
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

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>


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
        initBottomSheet()
    }

    private fun initBottomSheet(){
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.root)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
//                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED;
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

        })
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
                                .snippet(Gson().toJson(camp))
                                .icon(
                                    BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_RED)))
                        }
                    }
                    clickListener()
                }
            }
    }

    private fun clickListener(){
        map.setOnMarkerClickListener { campString ->
            if(campString.snippet != null){
                val camp: CampingModel = Gson().fromJson(campString.snippet , CampingModel::class.java)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                showBottomSheet(camp)
            }
            true
        }
    }

    private fun showBottomSheet(camp : CampingModel){
        Log.d("woha", "showBottomSheet: ")
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        translator.translate("Agency name: "+camp.campingName!!).addOnSuccessListener {
            binding.bottomSheet.agencyName.text = it
        }

        if(camp.address.toString() != null){
            translator.translate("Address: "+ camp.address!!).addOnSuccessListener {
                binding.bottomSheet.agencyAddress.text = it
            }
        }

        if(camp.mobileNo!!.isNotEmpty()){
            translator.translate("Mobile no: "+ camp.mobileNo!!).addOnSuccessListener {
                binding.bottomSheet.agencyNumber.text = it
            }
        }


        translator.translate(binding.bottomSheet.tvOpenGoogleMap.text.toString()).addOnSuccessListener {
            binding.bottomSheet.tvOpenGoogleMap.text = it
        }

        translator.translate(binding.bottomSheet.tvCall.text.toString()).addOnSuccessListener{
            binding.bottomSheet.tvCall.text = it
        }

        binding.bottomSheet.dialForCall.setOnClickListener {
            if(camp.mobileNo.isNotEmpty()){
                val intent = Intent(Intent.ACTION_DIAL)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.data = Uri.parse("tel:${camp.mobileNo}")
                startActivity(intent)
            }
        }

        binding.bottomSheet.openLoc.setOnClickListener {
            if (camp.location != null) {
                val uri =
                    "http://maps.google.com/maps?daddr=${camp.location!!.lat},${camp.location.lng}" + "(" + "Nearest implementation agency" + ")"
                val gmmIntentUri = Uri.parse(uri)
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                mapIntent.setPackage("com.google.android.apps.maps")
                ContextCompat.startActivity(requireContext(), mapIntent, null)

            } else {
                Toast.makeText(context, "Location will be updated soon", Toast.LENGTH_LONG).show()
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