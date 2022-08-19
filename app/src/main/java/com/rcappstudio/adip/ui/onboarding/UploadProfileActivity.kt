package com.rcappstudio.adip.ui.onboarding

import android.Manifest
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle

import android.provider.Settings

import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.rcappstudio.adip.data.model.*
import com.rcappstudio.adip.databinding.ActivityUploadProfileBinding
import com.rcappstudio.adip.ui.activity.MainActivity
import com.rcappstudio.adip.utils.Constants.Companion.DISTRICT
import com.rcappstudio.adip.utils.Constants.Companion.PROFILE_IMAGE
import com.rcappstudio.adip.utils.Constants.Companion.SHARED_PREF_FILE
import com.rcappstudio.adip.utils.Constants.Companion.STATE
import com.rcappstudio.adip.utils.Constants.Companion.UDID_NO_LIST
import com.rcappstudio.adip.utils.Constants.Companion.USER
import com.rcappstudio.adip.utils.Constants.Companion.USER_ID_LIST
import com.rcappstudio.adip.utils.Constants.Companion.USER_PROFILE_PATH
import com.rcappstudio.adip.utils.LoadingDialog


class UploadProfileActivity : AppCompatActivity() {

    private lateinit var name: String
    private lateinit var udidNo: String
    private lateinit var dateOfBirth: String
    private lateinit var state: String
    private lateinit var district: String
    private lateinit var mobileNo: String
    private lateinit var disabilityCategory : String


    private var imageURI: Uri? = null

    private lateinit var loadingDialog : LoadingDialog

    private lateinit var binding: ActivityUploadProfileBinding

    private val getImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        imageURI = it!!
        binding.profileImage.setImageURI(it)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadProfileBinding.inflate(layoutInflater)
        supportActionBar!!.hide()
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this, "Uploading please wait....")


        getIntentData()
        clickListener()
    }

    private fun getIntentData() {
        name = intent.getStringExtra(DetailsCollectingActivity.NAME)!!
        udidNo = intent.getStringExtra(DetailsCollectingActivity.UDID_NUMBER)!!
        dateOfBirth = intent.getStringExtra(DetailsCollectingActivity.DATE_OF_BIRTH)!!
        state = intent.getStringExtra(DetailsCollectingActivity.STATE)!!
        district = intent.getStringExtra(DetailsCollectingActivity.DISTRICT)!!
        mobileNo = intent.getStringExtra(DetailsCollectingActivity.MOBILE_NO)!!
        disabilityCategory = intent.getStringExtra("disabilityCategory")!!
    }

    private fun clickListener() {
        binding.uploadImage.setOnClickListener {
            permissionChecker()
        }

        binding.btnContinue.setOnClickListener {
            if(imageURI != null)
                storeToDatabase()
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


    private fun storeToDatabase() {
        loadingDialog.startLoading()
        if (imageURI != null) {
            val imageUploadReference = FirebaseStorage.getInstance()
                .getReference("/userFiles/${FirebaseAuth.getInstance().uid!!}")
            imageUploadReference.child(PROFILE_IMAGE).putFile(imageURI!!)
                .addOnSuccessListener { it ->
                    it.storage.downloadUrl.addOnSuccessListener {
                        saveUserDetails(it.toString().trim())
                    }
                }
        }
    }

    private fun saveUserDetails(imageUrl: String) {

        saveDataToSharedPreference(state, district, FirebaseAuth.getInstance().uid.toString())



        val pref = applicationContext.getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE)
        val userProfilePath = pref.getString(USER_PROFILE_PATH, null)

        val userModel = UserModel(
            name,
            mobileNo,
            udidNo,
            dateOfBirth,
            state,
            district,
//            false,
            imageUrl,
            disabilityCategory
        )

        val userIdList = UserIdList(state, district)

        FirebaseDatabase.getInstance()
            .getReference("${USER_ID_LIST}/${FirebaseAuth.getInstance().uid!!}")
            .setValue(userIdList)

        val udidReferencData = UdidReferenceModel(FirebaseAuth.getInstance().uid!! ,state, district)

        FirebaseDatabase.getInstance().getReference(UDID_NO_LIST).child(udidNo).setValue(udidReferencData)

        FirebaseDatabase.getInstance().getReference(userProfilePath.toString()).setValue(userModel)
            .addOnSuccessListener {
                loadingDialog.isDismiss()
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }
    }


    private fun saveDataToSharedPreference(
        state: String, district: String, uid: String
    ) {

        val userPath = "$USER/$state/$district/$uid"

        val pref = applicationContext.getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE)
        pref.edit().apply {
            putString(STATE, state)
            putString(DISTRICT, district)
            putString(USER_PROFILE_PATH, userPath)
        }.apply()

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
}