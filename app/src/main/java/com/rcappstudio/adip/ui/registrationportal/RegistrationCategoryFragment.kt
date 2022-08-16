package com.rcappstudio.adip.ui.registrationportal

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import com.androidbuts.multispinnerfilter.MultiSpinnerListener
import com.androidbuts.multispinnerfilter.MultiSpinnerSearch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.rcappstudio.adip.data.model.NgoData
import com.rcappstudio.adip.data.model.RequestStatus
import com.rcappstudio.adip.data.model.VerificationApplied
import com.rcappstudio.adip.databinding.ConfirmationDialogBinding
import com.rcappstudio.adip.databinding.FragmentRegistrationCategoryBinding
import com.rcappstudio.adip.utils.Constants
import com.rcappstudio.adip.utils.LoadingDialog
import com.rcappstudio.adip.utils.snakeToLowerCamelCase
import java.util.*

class RegistrationCategoryFragment : Fragment() {

    private lateinit var fBinding: ConfirmationDialogBinding

    private var aidsCount = 0
    private var validCount = 0
    private lateinit var binding: FragmentRegistrationCategoryBinding

    private lateinit var loadingDialog: LoadingDialog
    private lateinit var userPath : String

    private lateinit var userId : String
    private lateinit var state : String
    private lateinit var district : String

    private lateinit var orthopedicDisabilityList: MutableList<KeyPairBoolData>
    private lateinit var visualDisabilityList: MutableList<KeyPairBoolData>
    private lateinit var hearingDisabilityList: MutableList<KeyPairBoolData>
    private lateinit var multipleDisabilityList: MutableList<KeyPairBoolData>

    private lateinit var listOfAidsSelected: MutableList<String>
    private  var incomeCertificateUri : Uri? = null

    val orthoDisability = "Orthopedic disability"
    val hearingDisability = "Hearing disability"

    private val getImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        if(it != null){
            binding.ivIncomeCertificate.visibility = View.VISIBLE
            binding.ivIncomeCertificate.setImageURI(it)
            incomeCertificateUri = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRegistrationCategoryBinding.inflate(layoutInflater)
        binding.customToolBar.toolbar.title = "Registration portal"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity(), "Loading portal info....")
        //loadingDialog.startLoading()
        Log.d("aidsList", "onViewCreated: ${"Assistive and alarm devices".snakeToLowerCamelCase()} ")
        val sharedPreferences = requireContext().getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE)
        userPath = sharedPreferences.getString(Constants.USER_PROFILE_PATH, null)!!
        userId = FirebaseAuth.getInstance().uid!!
        state = sharedPreferences.getString(Constants.STATE, null)!!
        district = sharedPreferences.getString(Constants.DISTRICT , null)!!
        fetchList()
        fetchAlreadyAppliedAids()

        binding.btnContinue.setOnClickListener {
            validateAidsList()
        }

//        FirebaseDatabase.getInstance().getReference("$userPath/requestStatus/1660545428574/ngoList/-N8lgOhH1sxsPj__D8Xl")
//            .setValue(NgoData(mutableListOf("Educational kits", "Tricycle"), false , "-N8lgOhH1sxsPj__D8Xl" ))

    }

    private fun fetchList() {
        loadingDialog.startLoading()
        //TODO: Add network optimization in future
        listOfAidsSelected = mutableListOf()
        orthopedicDisabilityList = mutableListOf()
        orthopedicDisabilityList.add(KeyPairBoolData("Tricycle", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Wheel chair(adult and child)", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Walking stick", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Rollator", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Quadripod", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Tetrapod", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Auxiliary crutches", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Elbow crutches", false))
        orthopedicDisabilityList.add(KeyPairBoolData("CP chair", false))
        orthopedicDisabilityList.add(KeyPairBoolData("Corner chair", false))

        visualDisabilityList = mutableListOf()
        visualDisabilityList.add(
            KeyPairBoolData(
                "Accessible mobile phones, Laptop, Braille note taker , Brallier (school going students)",
                false
            )
        )
        visualDisabilityList.add(KeyPairBoolData("Learning equipment", false))
        visualDisabilityList.add(KeyPairBoolData("Communication equipment", false))
        visualDisabilityList.add(
            KeyPairBoolData(
                "Braille attachment for telephone for deafblind persons",
                false
            )
        )
        visualDisabilityList.add(KeyPairBoolData("Low vision Aids", false))
        visualDisabilityList.add(
            KeyPairBoolData(
                "Special mobility aids(for muscular dystrophy and cerebral palsy person)",
                false
            )
        )

        hearingDisabilityList = mutableListOf()
        hearingDisabilityList.add(KeyPairBoolData("Hearing aids", false))
        hearingDisabilityList.add(KeyPairBoolData("Educational kits", false))
        hearingDisabilityList.add(KeyPairBoolData("Assistive and alarm devices", false))
        hearingDisabilityList.add(KeyPairBoolData("Cochlear implant", false))

        multipleDisabilityList = mutableListOf()
        multipleDisabilityList.add(KeyPairBoolData("Teaching learning material kit", false))

    }

    private fun fetchAlreadyAppliedAids(){


        val alreadyAppliedList = mutableListOf<String>()
        FirebaseDatabase.getInstance().getReference("$userPath/${Constants.AIDS_APPLIED}").get()
            .addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    for(s in snapshot.children){
                        aidsCount++
                        alreadyAppliedList.add(s.key!!)
                    }
                    validCount = 5 - aidsCount//TODO: Increase maximum logic
                    if(validCount <= 0){
                        //TODO : show portal closed
                        binding.multipleItemSelectionSpinner.visibility = View.GONE
                        binding.btnContinue.visibility = View.GONE
                        binding.selectTv.visibility = View.GONE
                        binding.lottieFile.visibility = View.VISIBLE
                        binding.tvStatus.visibility = View.VISIBLE
                        Log.d("portal", "fetchAlreadyAppliedAids: portalCLosed!!")
                        loadingDialog.isDismiss()
                        return@addOnSuccessListener
                    }
                    categoryConditionChecker(alreadyAppliedList)
                    loadingDialog.isDismiss()
                } else{
                    validCount = 5
                    loadingDialog.isDismiss()
                    categoryConditionChecker(alreadyAppliedList)
                }
            }
    }

    private fun categoryConditionChecker(appliedList : MutableList<String>) {

        //TODO : Get data from shared pref to check the categories of users

        if (orthoDisability == "Orthopedic disability" && hearingDisability == "Hearing disability") {
            val list = hearingDisabilityList + orthopedicDisabilityList
            val filteredAidsList = mutableListOf<KeyPairBoolData>()
            for(aidKeyBoolPair in list){
                if(!appliedList.contains(aidKeyBoolPair.name.snakeToLowerCamelCase())){
                    filteredAidsList.add(aidKeyBoolPair)
                }
            }
            setMultipleSpinner(filteredAidsList)
        }
        else if(orthoDisability == "Orthopedic disability"){
            val list = orthopedicDisabilityList
            val filteredAidsList = mutableListOf<KeyPairBoolData>()
            for(aidKeyBoolPair in list){
                if(!appliedList.contains(aidKeyBoolPair.name.snakeToLowerCamelCase())){
                    filteredAidsList.add(aidKeyBoolPair)
                }
            }
            setMultipleSpinner(filteredAidsList)
        }
    }

    private fun setMultipleSpinner(filteredAidsList : MutableList<KeyPairBoolData>){
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

        for(i in filteredAidsList){
            Log.d("aidsList", "setMultipleSpinner: ${i.name.toString()}")
        }
    }

    private fun validateAidsList(){

        if(listOfAidsSelected.size in 0..validCount){
            uploadIncomeCertificateView()
        }
        else {
            listOfAidsSelected.clear()
            binding.multipleItemSelectionSpinner.setClearText("")
            Toast.makeText(requireContext(), "Limit has exceeded", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadIncomeCertificateView(){
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
    }

    private fun uploadToDatabase(){
        var incomeCertificateUrl = ""
        if(incomeCertificateUri != null){

            for(aid in listOfAidsSelected){
                FirebaseDatabase.getInstance().getReference("$userPath/${Constants.AIDS_APPLIED}/${aid.snakeToLowerCamelCase()}")
                    .setValue(false)
            }
            FirebaseStorage.getInstance()
                .getReference("/userFiles/${FirebaseAuth.getInstance().uid!!}/${Constants.INCOME_CERTIFICATE}")
                .putFile(incomeCertificateUri!!).addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener {d->
                        incomeCertificateUrl = d.toString().trim()
                        updateRequestStatus(incomeCertificateUrl)
                    }
                }
        }
    }

    private fun updateRequestStatus(incomeCertificateUrl : String){

        //TODO: Update request status
        val timeStamp : Long = System.currentTimeMillis()
        updateRequestStatus2(incomeCertificateUrl, timeStamp)

    }

    private fun updateRequestStatus2(incomeCertificateUrl: String , timeStamp : Long){
        FirebaseDatabase.getInstance()
            .getReference("${Constants.VERIFICATION_APPLIED}/$state/$district")
            .child(userId).setValue(VerificationApplied(userId, state, district))
        FirebaseDatabase.getInstance().getReference("$userPath/${Constants.REQUEST_STATUS}/$timeStamp")
            .setValue(RequestStatus(
                verified = false,
                notAppropriate = false,
                message = "Yet to verified",
                aidsReceived = false,
                appliedOnTimeStamp = timeStamp,
                aidsList = listOfAidsSelected,
                incomeCertificate = incomeCertificateUrl
            )).addOnCompleteListener {
                if(it.isSuccessful){
                    //TODO : Remove loading dialog
                    loadingDialog.isDismiss()
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
                        requireContext(), "You have denied!! gallery permissions",
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



}