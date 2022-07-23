package com.rcappstudio.adip.ui.registrationportal

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.model.AidsDoc
import com.rcappstudio.adip.data.model.LatLng
import com.rcappstudio.adip.data.model.RequestStatus
import com.rcappstudio.adip.data.model.VerificationApplied
import com.rcappstudio.adip.databinding.ActivityUploadAidsRegistrationDetailsBinding
import com.rcappstudio.adip.databinding.ActivityUploadProfileBinding
import com.rcappstudio.adip.databinding.ConfirmationDialogBinding
import com.rcappstudio.adip.databinding.ImagepreviewDialogBinding
import com.rcappstudio.adip.utils.Constants
import com.rcappstudio.adip.utils.LoadingDialog

class UploadAidsRegistrationDetailsActivity : AppCompatActivity() {

    private lateinit var isdialog: AlertDialog
    private lateinit var binding: ActivityUploadAidsRegistrationDetailsBinding
    private lateinit var dBinding : ImagepreviewDialogBinding

    private  var disabilityImageUri : Uri ?= null
    private  var passportSizeImageUri: Uri ?= null
    private  var incomeCertificateImageUri : Uri ?= null
    private  var identityProofImageUri  :Uri ?= null
    private  var addressProofImageUri : Uri ?= null

    private lateinit var disabilityImageUrl : String
    private lateinit var passportSizeImageUrl: String
    private lateinit var incomeCertificateImageUrl : String
    private lateinit var identityProofImageUrl  :String
    private lateinit var addressProofImageUrl : String

    private lateinit var pref : SharedPreferences

    private var IMAGE_NO : Int = 0

    private lateinit var loadingDialog: LoadingDialog

    private lateinit var databaseReference : DatabaseReference

    private val getImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        if(it != null)
            setImageUri(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadAidsRegistrationDetailsBinding.inflate(layoutInflater)
        loadingDialog = LoadingDialog(this, "Uploading files it may take a while...")
        pref = applicationContext.getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE)
        setContentView(binding.root)
        clickListener()
    }

    private fun clickListener(){

        binding.addDisabilityCertificate.setOnClickListener {
            IMAGE_NO = 1
            permissionChecker()
        }

        binding.addPassportSizePhoto.setOnClickListener {
            IMAGE_NO = 2
            permissionChecker()
        }

        binding.addIncomeCertificate.setOnClickListener {
            IMAGE_NO = 3
            permissionChecker()
        }

        binding.addIdentityProof.setOnClickListener {
            IMAGE_NO = 4
            permissionChecker()
        }

        binding.addAddressProof.setOnClickListener {
            IMAGE_NO = 5
            permissionChecker()
        }

        binding.uploadFilesForVerification.setOnClickListener{
            if(
            disabilityImageUri == null ||
            passportSizeImageUri == null ||
            incomeCertificateImageUri == null||
            identityProofImageUri == null ||
            addressProofImageUri == null
            ){
                Snackbar.make(binding.root, "Please select the required files", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            } else{
                uploadFilesToFirebaseStorage()
            }
        }

        binding.disabilityCertificate.setOnClickListener {
            imagePreviewDialog(disabilityImageUri)
        }

        binding.passportSizePhoto.setOnClickListener {
            imagePreviewDialog(passportSizeImageUri)
        }

        binding.ivAddressProof.setOnClickListener {
            imagePreviewDialog(addressProofImageUri)
        }

        binding.ivIdentityProof.setOnClickListener {
            imagePreviewDialog(identityProofImageUri)
        }

        binding.ivIncomeCertificate.setOnClickListener {
            imagePreviewDialog(incomeCertificateImageUri)
        }

    }

    private fun permissionChecker() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    getImage.launch("image/*")
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Toast.makeText(
                        applicationContext, "You have denied!! gallery permissions",
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
        AlertDialog.Builder(this).setMessage("Please enable the required permissions")
            .setPositiveButton("GO TO SETTINGS")
            { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
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

    private fun imagePreviewDialog(imageUri : Uri?){

        val dialogView = layoutInflater.inflate(R.layout.imagepreview_dialog, null)
        val dialogImageView = dialogView.findViewById<ImageView>(R.id.dialogImageView)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        dialogImageView.setImageURI(imageUri)
        builder.setCancelable(true)
        isdialog = builder.create()
        isdialog.show()

    }

    private fun uploadFilesToFirebaseStorage(){
        loadingDialog.startLoading()
        val imageUploadReference = FirebaseStorage.getInstance()
            .getReference("/userFiles/${FirebaseAuth.getInstance().uid!!}")

        for (i in 1..5){
            when(i){
                1->{
                    imageUploadReference.child(Constants.DISABILITY_CERTIFICATE)
                        .putFile(disabilityImageUri!!).addOnSuccessListener {
                            it.storage.downloadUrl.addOnSuccessListener {d->
                               disabilityImageUrl = d.toString().trim()
                            }
                        }
                }

                2->{
                    imageUploadReference.child(Constants.PASSPORT_SIZE_PHOTO)
                        .putFile(passportSizeImageUri!!).addOnSuccessListener {
                            it.storage.downloadUrl.addOnSuccessListener {d->
                                passportSizeImageUrl = d.toString().trim()
                            }
                        }
                }

                3->{
                    imageUploadReference.child(Constants.INCOME_CERTIFICATE)
                        .putFile(incomeCertificateImageUri!!).addOnSuccessListener {
                            it.storage.downloadUrl.addOnSuccessListener {d->
                                incomeCertificateImageUrl = d.toString().trim()
                            }
                        }
                }

                4->{
                    imageUploadReference.child(Constants.IDENTITY_PROOF)
                        .putFile(identityProofImageUri!!).addOnSuccessListener {
                            it.storage.downloadUrl.addOnSuccessListener {d->
                                identityProofImageUrl = d.toString().trim()
                            }
                        }
                }
                5->{
                    imageUploadReference.child(Constants.ADDRESS_PROOF)
                        .putFile(addressProofImageUri!!).addOnSuccessListener {
                            it.storage.downloadUrl.addOnSuccessListener {d->
                                addressProofImageUrl = d.toString().trim()
                                uploadImageUrlToDatabase()
                            }
                        }
                }
            }
        }
    }

    private fun uploadImageUrlToDatabase(){
        val pref = applicationContext.getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE)


        databaseReference =   FirebaseDatabase.getInstance()
            .getReference(pref.getString(Constants.USER_PROFILE_PATH, null)!!)

        val aidsDoc = AidsDoc(
           disabilityImageUrl,
           passportSizeImageUrl,
           incomeCertificateImageUrl,
           identityProofImageUrl,
           addressProofImageUrl
        )
        databaseReference
            .child("/aidsVerificationDocs")
            .setValue(aidsDoc).addOnSuccessListener {
                createRequestStatus()
            }
    }

    private fun createRequestStatus(){

        val requestStatus = RequestStatus(false, false , "Yet to verify the documents")
        requestStatus.message = "Yet to verify the documents"

        databaseReference.child("/${Constants.REQUEST_STATUS}")
            .setValue(requestStatus)
            .addOnSuccessListener {
                notifyAdminPanel()
                uploadDummyLocation()
                Snackbar.make(binding.root, "File uploaded successfully!!", Snackbar.LENGTH_LONG)
                    .show()
            }
    }

    private fun notifyAdminPanel(){

        val state = pref.getString(Constants.STATE, null)
        val district = pref.getString(Constants.DISTRICT, null)
        FirebaseDatabase.getInstance().getReference("${Constants.VERIFICATION_APPLIED}/$state/$district/${FirebaseAuth.getInstance().uid!!}")
            .setValue(VerificationApplied(FirebaseAuth.getInstance().uid!!,state!!,district!!))

        changeAlreadyApplied()
    }

    private fun changeAlreadyApplied(){
        val userProfilePath = pref.getString(Constants.USER_PROFILE_PATH, null)
        FirebaseDatabase.getInstance().getReference("$userProfilePath/alreadyApplied")
            .setValue(true).addOnSuccessListener {
                binding.uploadFilesForVerification.isEnabled = false
                binding.uploadFilesForVerification.visibility = View.GONE
                loadingDialog.isDismiss()
            }
    }

    private fun uploadDummyLocation(){
        databaseReference.child("${Constants.REQUEST_STATUS}/latLng")
            .setValue(LatLng("1.2", "1.2", "Dindigul collectrate"))
        LatLng("1.2", "1.2", "Dindigul collectrate")
    }

    private fun setImageUri(it: Uri?){
        when(IMAGE_NO){
            1->{
                disabilityImageUri = it!!
                binding.disabilityCertificate.visibility = View.VISIBLE
                binding.disabilityCertificate.setImageURI(it)
            }
            2->{
                passportSizeImageUri = it!!
                binding.passportSizePhoto.visibility = View.VISIBLE
                binding.passportSizePhoto.setImageURI(it)
            }
            3->{
                incomeCertificateImageUri = it!!
                binding.ivIncomeCertificate.visibility = View.VISIBLE
                binding.ivIncomeCertificate.setImageURI(it)
            }

            4->{
                identityProofImageUri = it!!
                binding.ivIdentityProof.visibility = View.VISIBLE
                binding.ivIdentityProof.setImageURI(it)
            }
            5->{
                addressProofImageUri = it!!
                binding.ivAddressProof.visibility = View.VISIBLE
                binding.ivAddressProof.setImageURI(it)
            }
        }
    }

    private fun reloadDatabase(){

    }
}