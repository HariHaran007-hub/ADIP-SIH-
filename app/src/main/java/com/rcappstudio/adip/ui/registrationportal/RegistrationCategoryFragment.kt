package com.rcappstudio.adip.ui.registrationportal

import android.Manifest
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.rcappstudio.adip.data.model.AidsDoc
import com.rcappstudio.adip.data.model.RequestStatus
import com.rcappstudio.adip.data.model.UserModel
import com.rcappstudio.adip.data.model.VerificationApplied
import com.rcappstudio.adip.databinding.ConfirmationDialogBinding
import com.rcappstudio.adip.databinding.FragmentRegistrationCategoryBinding
import com.rcappstudio.adip.utils.Constants
import com.rcappstudio.adip.utils.LoadingDialog
import com.rcappstudio.adip.utils.getAge
import com.rcappstudio.adip.utils.snakeToLowerCamelCase
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


class RegistrationCategoryFragment : Fragment() , TextToSpeech.OnInitListener{

    private lateinit var fBinding: ConfirmationDialogBinding
    val english = "en"
    val tamil = "ta"
    val hindi = "hi"
    private var voiceUrl = "https://translate.google.com/translate_tts?ie=UTF-&&client=tw-ob&tl=${tamil}&q="

    private var tts : TextToSpeech ?= null

    private var aidsCount = 0
    private var validCount = 0
    private lateinit var binding: FragmentRegistrationCategoryBinding

    private lateinit var loadingDialog: LoadingDialog
    private lateinit var userPath: String

    private lateinit var userId: String
    private lateinit var state: String
    private lateinit var district: String

    private lateinit var orthopedicDisabilityList: MutableList<String>
    private lateinit var visualDisabilityList: MutableList<String>
    private lateinit var hearingDisabilityList: MutableList<String>
    private lateinit var multipleDisabilityList: MutableList<String>

    private lateinit var translator : Translator

    private lateinit var listOfAidsSelected: MutableList<String>

    private lateinit var incomeCertificateUri: Uri
    private lateinit var incomeCertificateUrl : String
    private lateinit var aadhaarCardUri : Uri
    private lateinit var aadhaarCardUrl : String
    private lateinit var disabilityCertificateUrl : String
    private lateinit var disabilityCertificateUri : Uri

    private lateinit var  aidsList : MutableList<String>
    private lateinit var dob : String

    var orthoDisability = false
    var hearingDisability = false
    var visualDisability = false
    var mentallyAndMultipleDisability = false

    val orthopedicDisabilityConst = "Orthopedic disability"
    val hearingDisabilityConst = "Hearing disability"
    val visualSisabilityConst = "Visual disability"
    val mentallyAndMultipleDisabilityConst = "Mentally and multiple disability"



    var IMG_NODE =0

    private val getImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        if (it != null) {
//            Log.d("dataEvaluation", ": $IMG_NODE")
            if (IMG_NODE == 2) {
                binding.ivAadhaarCard.visibility = View.VISIBLE
                binding.ivAadhaarCard.setImageURI(it)
                aadhaarCardUri = it
            } else if (IMG_NODE == 1) {
                binding.ivIncomeCertificate.visibility = View.VISIBLE
                binding.ivIncomeCertificate.setImageURI(it)
                incomeCertificateUri = it
            } else if (IMG_NODE == 3) {

                binding.ivDisabilityCertificate.visibility = View.VISIBLE
                binding.ivDisabilityCertificate.setImageURI(it)
                disabilityCertificateUri = it
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRegistrationCategoryBinding.inflate(layoutInflater)
        binding.customToolBar.toolbar.title = "Application portal"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        aidsList = mutableListOf()
        loadingDialog = LoadingDialog(requireActivity(), "Loading portal info....")
        tts = TextToSpeech(requireContext(), this)
        //loadingDialog.startLoading()
        Log.d(
            "aidsList",
            "onViewCreated: ${"Assistive and alarm devices".snakeToLowerCamelCase()} "
        )
        val sharedPreferences =
            requireContext().getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE)
        userPath = sharedPreferences.getString(Constants.USER_PROFILE_PATH, null)!!
        userId = FirebaseAuth.getInstance().uid!!
        state = sharedPreferences.getString(Constants.STATE, null)!!
        district = sharedPreferences.getString(Constants.DISTRICT, null)!!
        prepareModel()
        fetchList()
        clickListener()

        FirebaseDatabase.getInstance().getReference(userPath).get()
            .addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    val user = snapshot.getValue(UserModel::class.java)
                    dob = user!!.dateOfBirth.toString()
                    fetchUserData()
                }

            }
    }
    private fun clickListener(){
        binding.addIncomeCertificate.setOnClickListener {
            IMG_NODE = 1
            permissionChecker()
        }

        binding.addAadhaarCard.setOnClickListener {
            IMG_NODE = 2
            permissionChecker()
        }

        binding.addDisabilityCertificate.setOnClickListener {
            IMG_NODE = 3
            permissionChecker()
        }

        binding.btnContinue.setOnClickListener {
            if(disabilityCertificateUri != null && incomeCertificateUri != null && aadhaarCardUri != null)
                uploadFilesToFirebaseStorage()
            else
                Toast.makeText(requireContext(), "Please select all documents to upload" , Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchUserData(){
        val timeStamps = mutableListOf<Long>()
        FirebaseDatabase.getInstance().getReference("${userPath}/${Constants.REQUEST_STATUS}").get()
            .addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    for(s in snapshot.children){
                        val requestStatus = s.getValue(RequestStatus::class.java)
                        if(!requestStatus!!.documentVerified!!){
                            binding.tvStatus2.visibility =  View.VISIBLE
                            binding.tvStatus.visibility = View.GONE
                            binding.llLayout.visibility = View.GONE
                            binding.lottieFile.visibility = View.VISIBLE
                        } else{
                            binding.tvStatus2.visibility =  View.GONE
                            binding.tvStatus.visibility = View.VISIBLE
                            binding.llLayout.visibility = View.GONE
                            binding.lottieFile.visibility = View.VISIBLE
                        }

                        if(requestStatus.notAppropriate){
                            binding.tvStatus.visibility = View.GONE
                            binding.tvStatus2.visibility = View.GONE
                            binding.lottieFile.visibility = View.GONE
                            binding.llLayout.visibility = View.VISIBLE
                        }
                            timeStamps.add(s.key!!.toLong())
                    }

                    validation(timeStamps)

                } else {
                    binding.llLayout.visibility = View.VISIBLE
                }
            }
    }

    private fun validation(timeStamps : MutableList<Long>){
        timeStamps.sort()
        val daysHashMap = HashMap<Long , Long>()
        for(t in timeStamps){
            Log.d("tagData", "validation: t-> $t")
            val diffTime = Calendar.getInstance().timeInMillis - t
            val diffInDays: Long = TimeUnit.MILLISECONDS.toDays(diffTime)
            daysHashMap[t] = diffInDays
        }
//        val diffInMillisecTopLevel: Long = Calendar.getInstance().timeInMillis - timeStamps[0]

        if(getAge(dob) <=  12){
            var workTimeStamp : Long = 0
            daysHashMap.toSortedMap()
            for(i in daysHashMap){
                Log.d("tagData", "validation: ${i.value}")
                if(i.value > 365){

                } else{
                    workTimeStamp = i.key
                    Log.d("tagData", "validation: workTimeStamp->$workTimeStamp")
                    break
                }
            }

            val diffInMillisec: Long = Calendar.getInstance().timeInMillis - workTimeStamp

            val diffInDays: Long = TimeUnit.MILLISECONDS.toDays(diffInMillisec)

            val remainingDays = (365 - diffInDays) - 1
            Log.d("dataSet", "validation: $diffInDays")
            if(diffInDays <= 365){
//                binding.llLayout.visibility = View.GONE
                binding.tvStatus.text = "Time remaining for  another application ${remainingDays} days."
            } else {
                binding.llLayout.visibility = View.VISIBLE
//                binding.lottieFile.visibility = View.GONE
                binding.tvStatus.visibility = View.GONE
            }

            Log.d("tagData", "validation: $diffInDays")

        } else {

            var workTimeStamp : Long = 0
            daysHashMap.toSortedMap()
            for(i in daysHashMap){
                Log.d("tagData", "validation: ${i.value}")
                if(i.value > 1095){

                } else{
                    workTimeStamp = i.key
                    Log.d("tagData", "validation: workTimeStamp ${workTimeStamp}")
                    break
                }
            }

            val diffInMillisec: Long = Calendar.getInstance().timeInMillis - workTimeStamp

            val diffInDays: Long = TimeUnit.MILLISECONDS.toDays(diffInMillisec)
            Log.d("tagData", "validation: $diffInDays")

            val remainingDays = (1095 - diffInDays) - 1
            Log.d("dataSet", "validation: $diffInDays")
            if(diffInDays <= 1095){
//                binding.llLayout.visibility = View.GONE
                binding.tvStatus.text = "Time remaining for  another application ${remainingDays} days."
            } else {
                binding.llLayout.visibility = View.VISIBLE
//                binding.lottieFile.visibility = View.GONE
                binding.tvStatus.visibility = View.GONE
            }

            Log.d("tagData", "validation: $diffInDays")
        }

    }



    private fun fetchList() {
//        loadingDialog.startLoading()
        //TODO: Add network optimization in future
        listOfAidsSelected = mutableListOf()
//        orthopedicDisabiltyList = mutableListOf()
//        orthopedicDisabilityList.add(KeyPairBoolData("Tricycle", false))
//        orthopedicDisabilityList.add(KeyPairBoolData("Wheel chair(adult and child)", false))
//        orthopedicDisabilityList.add(KeyPairBoolData("Walking stick", false))
//        orthopedicDisabilityList.add(KeyPairBoolData("Rollator", false))
//        orthopedicDisabilityList.add(KeyPairBoolData("Quadripod", false))
//        orthopedicDisabilityList.add(KeyPairBoolData("Tetrapod", false))
//        orthopedicDisabilityList.add(KeyPairBoolData("Auxiliary crutches", false))
//        orthopedicDisabilityList.add(KeyPairBoolData("Elbow crutches", false))
//        orthopedicDisabilityList.add(KeyPairBoolData("CP chair", false))
//        orthopedicDisabilityList.add(KeyPairBoolData("Corner chair", false))


        orthopedicDisabilityList = mutableListOf()
        orthopedicDisabilityList.add("Tricycle")
        orthopedicDisabilityList.add("Wheel_chair(adult_and_child)")
        orthopedicDisabilityList.add("Walking_stick")
        orthopedicDisabilityList.add("Rollator")
        orthopedicDisabilityList.add("Quadripod")
        orthopedicDisabilityList.add("Tetrapod")
        orthopedicDisabilityList.add("Auxiliary_crutches")
        orthopedicDisabilityList.add("Elbow_crutches")
        orthopedicDisabilityList.add("CP_chair")
        orthopedicDisabilityList.add("Corner_chair")


        visualDisabilityList = mutableListOf()
//        visualDisabilityList.add(
//            KeyPairBoolData(
//                "Accessible mobile phones, Laptop, Braille note taker , Brallier (school going students)",
//                false
//            )
//        )

        visualDisabilityList.add(
                "Accessible_mobile_phones,_Laptop,_Braille_note_taker_,_Brallier_(school_going_students)",
        )


//        visualDisabilityList.add(KeyPairBoolData("Learning equipment", false))
//        visualDisabilityList.add(KeyPairBoolData("Communication equipment", false))
//        visualDisabilityList.add(
//            KeyPairBoolData(
//                "Braille attachment for telephone for deafblind persons",
//                false
//            )
//        )


        visualDisabilityList.add("Learning_equipment")
        visualDisabilityList.add("Communication_equipment")
        visualDisabilityList.add(
                "Braille_attachment_for_telephone_for_deafblind_persons"
        )

        visualDisabilityList.add("Low_vision_Aids")
        visualDisabilityList.add(
                "Special_mobility_aids(for_muscular_dystrophy_and_cerebral_palsy_person)"

        )

        hearingDisabilityList = mutableListOf()
//        hearingDisabilityList.add(KeyPairBoolData("Hearing aids", false))
//        hearingDisabilityList.add(KeyPairBoolData("Educational kits", false))
//        hearingDisabilityList.add(KeyPairBoolData("Assistive and alarm devices", false))
//        hearingDisabilityList.add(KeyPairBoolData("Cochlear implant", false))

        hearingDisabilityList.add("Hearing_aids")
        hearingDisabilityList.add("Educational_kits")
        hearingDisabilityList.add("Assistive_and_alarm_devices")
        hearingDisabilityList.add("Cochlear_implant")

        multipleDisabilityList = mutableListOf()
//        multipleDisabilityList.add(KeyPairBoolData("Teaching learning material kit", false))
        multipleDisabilityList.add("Teaching_learning_material_kit")

        extractCategoryFromDatabase()

    }

//    private fun fetchAlreadyAppliedAids() {
//
//
//        val alreadyAppliedList = mutableListOf<String>()
//        FirebaseDatabase.getInstance().getReference("$userPath/${Constants.AIDS_APPLIED}").get()
//            .addOnSuccessListener { snapshot ->
//                if (snapshot.exists()) {
//                    for (s in snapshot.children) {
//                        aidsCount++
//                        alreadyAppliedList.add(s.key!!)
//                    }
//                    validCount = 5 - aidsCount//TODO: Increase maximum logic
//                    if (validCount <= 0) {
//                        //TODO : show portal closed
//                        binding.multipleItemSelectionSpinner.visibility = View.GONE
//                        binding.btnContinue.visibility = View.GONE
//                        binding.selectTv.visibility = View.GONE
//                        binding.lottieFile.visibility = View.VISIBLE
//                        binding.tvStatus.visibility = View.VISIBLE
//                        Log.d("portal", "fetchAlreadyAppliedAids: portalCLosed!!")
//                        loadingDialog.isDismiss()
//                        return@addOnSuccessListener
//                    }
//                    extractCategoryFromDatabase(alreadyAppliedList)
//                    loadingDialog.isDismiss()
//                } else {
//                    validCount = 5 //TODO: Set maximum limit
//                    loadingDialog.isDismiss()
//                    extractCategoryFromDatabase(alreadyAppliedList)
//                }
//            }
//    }

    private fun extractCategoryFromDatabase() {
        FirebaseDatabase.getInstance().getReference("$userPath/disabilityCategory")
            .get().addOnSuccessListener {
                if (it.exists()) {
                    val category = it.value.toString()

//                    categoryConditionChecker(appliedAidsList, category.split(',').toMutableList())
                    categoryConditionChecker( category.split(',').toMutableList())
                }
            }

    }

    private fun categoryConditionChecker(
//        appliedList: MutableList<String>,
        categoryList: MutableList<String>
    ) {

        //TODO : Get data from shared pref to check the categories of users
        for (i in categoryList) {
            Log.d("valuewData", "categoryConditionChecker: $i")
            when (i.trim()) {
                orthopedicDisabilityConst -> {
                    orthoDisability = true
                }
                visualSisabilityConst -> visualDisability = true
                hearingDisabilityConst -> hearingDisability = true
                mentallyAndMultipleDisabilityConst -> mentallyAndMultipleDisability = true
            }
        }

        //TODO: Set multi item spinner and automate it

        if (hearingDisability && orthoDisability) {
            aidsList.addAll(hearingDisabilityList + orthopedicDisabilityList)
//            val filteredAidsList = mutableListOf<KeyPairBoolData>()
//            for (aidKeyBoolPair in list) {
//                if (!appliedList.contains(aidKeyBoolPair.name.snakeToLowerCamelCase())) {
//                    filteredAidsList.add(aidKeyBoolPair)
//                }
//            }
//            setMultipleSpinner(filteredAidsList)
        } else if (hearingDisability && visualDisability) {

            aidsList.addAll(hearingDisabilityList + visualDisabilityList)
//            val filteredAidsList = mutableListOf<KeyPairBoolData>()
//            for (aidKeyBoolPair in list) {
//                if (!appliedList.contains(aidKeyBoolPair.name.snakeToLowerCamelCase())) {
//                    filteredAidsList.add(aidKeyBoolPair)
//                }
//            }
//            setMultipleSpinner(filteredAidsList)

        } else if (hearingDisability && mentallyAndMultipleDisability) {
            aidsList.addAll(hearingDisabilityList + multipleDisabilityList)
//            val filteredAidsList = mutableListOf<KeyPairBoolData>()
//            for (aidKeyBoolPair in list) {
//                if (!appliedList.contains(aidKeyBoolPair.name.snakeToLowerCamelCase())) {
//                    filteredAidsList.add(aidKeyBoolPair)
//                }
//            }
//            setMultipleSpinner(filteredAidsList)

        } else if (orthoDisability && visualDisability) {

            aidsList.addAll( orthopedicDisabilityList + visualDisabilityList)
//            val filteredAidsList = mutableListOf<KeyPairBoolData>()
//            for (aidKeyBoolPair in list) {
//                if (!appliedList.contains(aidKeyBoolPair.name.snakeToLowerCamelCase())) {
//                    filteredAidsList.add(aidKeyBoolPair)
//                }
//            }
//            setMultipleSpinner(filteredAidsList)


        } else if (orthoDisability && mentallyAndMultipleDisability) {
            aidsList.addAll(orthopedicDisabilityList + multipleDisabilityList)
//            val filteredAidsList = mutableListOf<KeyPairBoolData>()
//            for (aidKeyBoolPair in list) {
//                if (!appliedList.contains(aidKeyBoolPair.name.snakeToLowerCamelCase())) {
//                    filteredAidsList.add(aidKeyBoolPair)
//                }
//            }
//            setMultipleSpinner(filteredAidsList)


        } else if (mentallyAndMultipleDisability && visualDisability) {
            aidsList.addAll(multipleDisabilityList + visualDisabilityList)
//            val filteredAidsList = mutableListOf<KeyPairBoolData>()
//            for (aidKeyBoolPair in list) {
//                if (!appliedList.contains(aidKeyBoolPair.name.snakeToLowerCamelCase())) {
//                    filteredAidsList.add(aidKeyBoolPair)
//                }
//            }
//            setMultipleSpinner(filteredAidsList)

        } else if (mentallyAndMultipleDisability && visualDisability && orthoDisability) {
            aidsList.addAll( multipleDisabilityList + visualDisabilityList + orthopedicDisabilityList)
//            val filteredAidsList = mutableListOf<KeyPairBoolData>()
//            for (aidKeyBoolPair in list) {
//                if (!appliedList.contains(aidKeyBoolPair.name.snakeToLowerCamelCase())) {
//                    filteredAidsList.add(aidKeyBoolPair)
//                }
//            }
//            setMultipleSpinner(filteredAidsList)

        } else if (orthoDisability && mentallyAndMultipleDisability && hearingDisability) {
            aidsList.addAll(orthopedicDisabilityList + multipleDisabilityList + hearingDisabilityList)
//            val filteredAidsList = mutableListOf<KeyPairBoolData>()
//            for (aidKeyBoolPair in list) {
//                if (!appliedList.contains(aidKeyBoolPair.name.snakeToLowerCamelCase())) {
//                    filteredAidsList.add(aidKeyBoolPair)
//                }
//            }
//            setMultipleSpinner(filteredAidsList)

        } else if (orthoDisability && visualDisability && hearingDisability) {
            aidsList.addAll(orthopedicDisabilityList + visualDisabilityList + hearingDisabilityList)
//            val filteredAidsList = mutableListOf<KeyPairBoolData>()
//            for (aidKeyBoolPair in list) {
//                if (!appliedList.contains(aidKeyBoolPair.name.snakeToLowerCamelCase())) {
//                    filteredAidsList.add(aidKeyBoolPair)
//                }
//            }
//            setMultipleSpinner(filteredAidsList)

        } else if (mentallyAndMultipleDisability) {
            aidsList.addAll( multipleDisabilityList)
//            val filteredAidsList = mutableListOf<KeyPairBoolData>()
//            for (aidKeyBoolPair in list) {
//                if (!appliedList.contains(aidKeyBoolPair.name.snakeToLowerCamelCase())) {
//                    filteredAidsList.add(aidKeyBoolPair)
//                }
//            }
//            setMultipleSpinner(filteredAidsList)


        } else if (hearingDisability) {
            aidsList.addAll( hearingDisabilityList)
//            val filteredAidsList = mutableListOf<KeyPairBoolData>()
//            for (aidKeyBoolPair in list) {
//                if (!appliedList.contains(aidKeyBoolPair.name.snakeToLowerCamelCase())) {
//                    filteredAidsList.add(aidKeyBoolPair)
//                }
//            }
//            setMultipleSpinner(filteredAidsList)

        } else if (orthoDisability) {
            aidsList.addAll(orthopedicDisabilityList)
//            val filteredAidsList = mutableListOf<KeyPairBoolData>()
//            for (aidKeyBoolPair in list) {
//                if (!appliedList.contains(aidKeyBoolPair.name.snakeToLowerCamelCase())) {
//                    filteredAidsList.add(aidKeyBoolPair)
//                }
//            }
//            setMultipleSpinner(filteredAidsList)

        } else if (visualDisability) {
            aidsList.addAll(visualDisabilityList)
//            val filteredAidsList = mutableListOf<KeyPairBoolData>()
//            for (aidKeyBoolPair in list) {
//                if (!appliedList.contains(aidKeyBoolPair.name.snakeToLowerCamelCase())) {
//                    filteredAidsList.add(aidKeyBoolPair)
//                }
//            }
//            setMultipleSpinner(filteredAidsList)
        }
    }
/*
    private fun setMultipleSpinner(filteredAidsList: MutableList<KeyPairBoolData>) {
        if(filteredAidsList.isEmpty()){
            binding.multipleItemSelectionSpinner.visibility = View.GONE
            binding.btnContinue.visibility = View.GONE
            binding.selectTv.visibility = View.GONE
            binding.lottieFile.visibility = View.VISIBLE
            binding.tvStatus.visibility = View.VISIBLE
        }
        binding.multipleItemSelectionSpinner.setLimit(validCount, object : MultiSpinnerListener,
            MultiSpinnerSearch.LimitExceedListener {
            override fun onItemsSelected(selectedItems: MutableList<KeyPairBoolData>?) {
                Log.d("llll", "onLimitListener: ${selectedItems!!.size}")
            }

            override fun onLimitListener(data: KeyPairBoolData?) {
                Toast.makeText(requireContext(), "Limit has exceeded", Toast.LENGTH_LONG).show()
            }

        })
        binding.multipleItemSelectionSpinner.setItems(
            filteredAidsList
        ) { items ->
            for (i in 0 until items!!.size) {
                if (items[i].isSelected) {
                    Log.i(
                        "multiData",
                        i.toString() + " : " + items[i].name + " : " + items[i]
                            .isSelected
                    )
                    listOfAidsSelected.add(items[i].name)
                }
            }
        }

        for (i in filteredAidsList) {
            Log.d("aidsList", "setMultipleSpinner: ${i.name.toString()}")
        }
    }*/

//    private fun validateAidsList() {
//
//        if (listOfAidsSelected.size in 0..validCount) {
//            uploadIncomeCertificateView()
//        } else {
//            listOfAidsSelected.clear()
//            binding.multipleItemSelectionSpinner.setClearText("")
//            Toast.makeText(requireContext(), "Limit has exceeded", Toast.LENGTH_LONG).show()
//        }
//    }

   /* private fun uploadIncomeCertificateView() {
        binding.llIvCertificate.visibility = View.VISIBLE
        binding.btnContinue.visibility = View.GONE
        binding.btnUploadData.visibility = View.VISIBLE

        binding.addIncomeCertificate.setOnClickListener {
            permissionChecker()
        }

        binding.btnUploadData.setOnClickListener {
            loadingDialog.startLoading()
            uploadToDatabase()
        }
    }*/

    private fun uploadFilesToFirebaseStorage(){

        loadingDialog.startLoading()
        val imageUploadReference = FirebaseStorage.getInstance()
            .getReference("/userFiles/${FirebaseAuth.getInstance().uid!!}")

        for (i in 1..3){
            when(i){
                1->{
                    imageUploadReference.child(Constants.DISABILITY_CERTIFICATE)
                        .putFile(disabilityCertificateUri!!).addOnSuccessListener {
                            it.storage.downloadUrl.addOnSuccessListener {d->
                                disabilityCertificateUrl = d.toString().trim()
                            }
                        }
                }

                2->{
                    imageUploadReference.child(Constants.IDENTITY_PROOF)
                        .putFile(aadhaarCardUri!!).addOnSuccessListener {
                            it.storage.downloadUrl.addOnSuccessListener {d->
                                aadhaarCardUrl = d.toString().trim()
                            }
                        }
                }

                3->{
                    imageUploadReference.child(Constants.INCOME_CERTIFICATE)
                        .putFile(incomeCertificateUri!!).addOnSuccessListener {
                            it.storage.downloadUrl.addOnSuccessListener {d->
                                incomeCertificateUrl = d.toString().trim()
                                updateRequestStatus()
                            }
                        }
                }


            }
        }
    }

    private fun initRequestStatus(timeStamp : Long){
        val aidsDoc = AidsDoc(
            incomeTaxCertificate = incomeCertificateUrl,
            identityProofUrl = aadhaarCardUrl,
            disabilityCertificateURL = disabilityCertificateUrl
        )
        FirebaseDatabase.getInstance()
            .getReference("${Constants.VERIFICATION_APPLIED}/$state/$district")
            .child(userId).setValue(VerificationApplied(userId, state, district))
        FirebaseDatabase.getInstance()
            .getReference("$userPath/${Constants.REQUEST_STATUS}/$timeStamp")
            .setValue(
                RequestStatus(
                    documentVerified = false,
                    notAppropriate = false,
                    message = "Yet to verified",
                    aidsReceived = false,
                    appliedOnTimeStamp = timeStamp,
                    aidsList = aidsList,
                    doctorVerification = false,
                    aidsDocs = aidsDoc
                )
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    //TODO : Remove loading dialog
                    Snackbar.make(binding.root, "Files uploaded successfully", Snackbar.LENGTH_LONG)
                        .show()
                    loadingDialog.isDismiss()
                    requireActivity().onBackPressed()
                }
            }

    }

    private fun uploadToDatabase() {
        var incomeCertificateUrl = ""
        if (incomeCertificateUri != null) {
            for (aid in listOfAidsSelected) {
                FirebaseDatabase.getInstance()
                    .getReference("$userPath/${Constants.AIDS_APPLIED}/${aid.snakeToLowerCamelCase()}")
                    .setValue(false)
            }
            FirebaseStorage.getInstance()
                .getReference("/userFiles/${FirebaseAuth.getInstance().uid!!}/${Constants.INCOME_CERTIFICATE}")
                .putFile(incomeCertificateUri!!).addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener { d ->
                        incomeCertificateUrl = d.toString().trim()
                        updateRequestStatus()
                    }
                }
        }
    }

    private fun updateRequestStatus() {

        //TODO: Update request status
        val timeStamp: Long = System.currentTimeMillis()
        initRequestStatus(timeStamp)

    }

    private fun updateRequestStatus2(incomeCertificateUrl: String, timeStamp: Long) {
        val aidsDoc = AidsDoc(
            incomeTaxCertificate = incomeCertificateUrl,
            identityProofUrl = aadhaarCardUrl,
            disabilityCertificateURL = disabilityCertificateUrl
        )
        FirebaseDatabase.getInstance()
            .getReference("${Constants.VERIFICATION_APPLIED}/$state/$district")
            .child(userId).setValue(VerificationApplied(userId, state, district))
        FirebaseDatabase.getInstance()
            .getReference("$userPath/${Constants.REQUEST_STATUS}/$timeStamp")
            .setValue(
                RequestStatus(
                    documentVerified = false,
                    notAppropriate = false,
                    message = "Yet to verified",
                    aidsReceived = false,
                    appliedOnTimeStamp = timeStamp,
                    aidsList = listOfAidsSelected,
                    doctorVerification = false,
                    aidsDocs = aidsDoc
                )
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    //TODO : Remove loading dialog
                    Snackbar.make(binding.root, "Files uploaded successfully", Snackbar.LENGTH_LONG)
                        .show()
                    loadingDialog.isDismiss()
                    requireActivity().onBackPressed()
                }
            }
    }


    private fun permissionChecker() {
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    getImage.launch("image/*")
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Toast.makeText(
                        requireContext(),
                        "You have denied!! gallery permissions",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
    }


    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(requireContext()).setMessage("Please enable the required permissions")
            .setPositiveButton("GO TO SETTINGS")
            { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", requireContext().packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel")
            { dialog, _ ->
                dialog.dismiss()
            }.show()
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
            if(sharedPreferences == TranslateLanguage.TAMIL)
                voiceUrl = "https://translate.google.com/translate_tts?ie=UTF-&&client=tw-ob&tl=${tamil}&q="
            else if(sharedPreferences == TranslateLanguage.HINDI)
                voiceUrl = "https://translate.google.com/translate_tts?ie=UTF-&&client=tw-ob&tl=${hindi}&q="

            translator.downloadModelIfNeeded().addOnSuccessListener {
                translateLanguage()
            }.addOnFailureListener {

            }
            translator.translate(binding.customToolBar.toolbar.title.toString()).addOnSuccessListener {

                binding.customToolBar.toolbar.title = it
            }
        }
    }

    private fun translateLanguage(){

        translator.translate(binding.tvRegistration.text.toString()).addOnSuccessListener {
            binding.tvRegistration.text = it
        }

        binding.tvRegistration.setOnLongClickListener {
                val mp = MediaPlayer()
                mp.setDataSource(voiceUrl + binding.tvRegistration.text.toString())
                mp.prepare()
                mp.start()
                true
        }
        translator.translate(binding.selectTv.text.toString()).addOnSuccessListener {
            binding.selectTv.text = it
        }

        binding.selectTv.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.selectTv.text.toString())
            mp.prepare()
            mp.start()
            true
        }


        translator.translate(binding.btnContinue.text.toString()).addOnSuccessListener {
            binding.btnContinue.text = it
        }


        binding.btnContinue.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.btnContinue.text.toString())
            mp.prepare()
            mp.start()
            true
        }
        translator.translate(binding.btnUploadData.text.toString()).addOnSuccessListener {
            binding.btnContinue.text = it
        }
        binding.btnUploadData.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.btnUploadData.text.toString())
            mp.prepare()
            mp.start()
            true
        }


//        translator.translate(binding.btnUploadData.text.toString()).addOnSuccessListener {
//            binding.btnContinue.text = it
//        }

//        translator.translate(binding.btnUploadData.text.toString()).addOnSuccessListener {
//            binding.btnContinue.text = it
//        }

        translator.translate(binding.tvIncomeCertificate.text.toString()).addOnSuccessListener {
            binding.tvIncomeCertificate.text = it
        }

        binding.tvIncomeCertificate.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.tvIncomeCertificate.text.toString())
            mp.prepare()
            mp.start()
            true
        }

        translator.translate(binding.tvIncomeCertificatePara.text.toString()).addOnSuccessListener {
            binding.tvIncomeCertificatePara.text = it
        }

        binding.tvIncomeCertificatePara.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.tvIncomeCertificatePara.text.toString())
            mp.prepare()
            mp.start()
            true
        }

        translator.translate(binding.addIncomeCertificate.text.toString()).addOnSuccessListener {
            binding.addIncomeCertificate.text = it
        }

        binding.addIncomeCertificate.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.addIncomeCertificate.text.toString())
            mp.prepare()
            mp.start()
            true
        }

        translator.translate(binding.tvStatus.text.toString()).addOnSuccessListener {
            binding.tvStatus.text = it
        }

        binding.tvStatus.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.tvStatus.text.toString())
            mp.prepare()
            mp.start()
            true
        }

        translator.translate(binding.btnUploadData.text.toString()).addOnSuccessListener {
            binding.btnContinue.text = it
        }

        binding.btnUploadData.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.btnUploadData.text.toString())
            mp.prepare()
            mp.start()
            true
        }
        translator.translate(binding.multipleItemSelectionSpinner.hintText.toString()).addOnSuccessListener {
            binding.multipleItemSelectionSpinner.hintText = it
        }

        binding.multipleItemSelectionSpinner.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.multipleItemSelectionSpinner.hintText.toString())
            mp.prepare()
            mp.start()
            true
        }

    }

    override fun onInit(status: Int) {




        if (status === TextToSpeech.SUCCESS) {

            val result: Int = tts!!.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts!!.language = Locale.forLanguageTag("hin")
                } else {
                    //  Toast.makeText(mContext, result + "Language is not supported", Toast.LENGTH_SHORT).show();
                    Log.e("Text2SpeechWidget", result.toString() + "Language is not supported")
                }
                Log.e("Text2SpeechWidget", "$result is not supported")
            }

//            if ("en".toLowerCase().contains("en")) {
//                val result: Int = tts.setLanguage(Locale("en", "IN"))
//                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                    //Toast.makeText(mContext, result + " is not supported", Toast.LENGTH_SHORT).show();
//                    Log.e("Text2SpeechWidget", "$result is not supported")
//                }
//            } else {
//
//            }
//        }
    }
}
}