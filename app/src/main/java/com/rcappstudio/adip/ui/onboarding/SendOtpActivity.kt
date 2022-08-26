package com.rcappstudio.adip.ui.onboarding

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

import com.rcappstudio.adip.databinding.ActivitySendOtpBinding
import com.rcappstudio.adip.utils.LoadingDialog
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.FileDescriptor
import java.io.IOException
import java.io.StringReader
import java.lang.Exception
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


class SendOtpActivity : AppCompatActivity() {

    private lateinit var udidDataList: MutableList<String>

    val REQUEST_CAMERA_CODE = 100
    private lateinit var binding : ActivitySendOtpBinding
    private lateinit var loadingDialog : LoadingDialog
    private lateinit var mobileNumber : String
    private lateinit var udidNumber : String
    private lateinit var percentageOfDisability : String

    private lateinit var visualDisability : String
    private lateinit var orthopedicDisability : String
    private lateinit var hearingDisability : String
    private lateinit var mentalDisability : String

    private lateinit var gender : String
    private lateinit var category: String
    private lateinit var dob : String



    val orthopedicDisabilityConst = "Orthopedic disability"
    val hearingDisabilityConst = "Hearing disability"
    val visualSisabilityConst = "Visual disability"
    val mentallyAndMultipleDisabilityConst = "Mentally and multiple disability"

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent: Uri = result.uriContent!!
            binding.ivUdidCard.setImageURI(uriContent)
            imageProcess(uriContent)
        } else {
            // an error occurred
            val exception = result.error
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()
        udidDataList= mutableListOf()

        loadingDialog = LoadingDialog(this, "Sending OTP to +91 ${binding.inputMobNo.text.toString().trim()}")
        binding.addUdidCard.setOnClickListener {
            permissionChecker()
        }


        binding.btnsend.setOnClickListener{
            val percentile = percentageOfDisability.split('%')[0].toInt()

            if(udidNumber.isNotEmpty() && percentile >= 40){
                fetchXmlData()
            }
        }

        binding.notHaveUdid.setOnClickListener {
//            startActivity(Intent(applicationContext , WebViewActivity::class.java))
            openBrowser()
        }
    }

    private fun openBrowser(){
        val url = "https://www.swavlambancard.gov.in/pwd/application"
        val intent = Intent(Intent.ACTION_VIEW , Uri.parse(url))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.setPackage("com.android.chrome")
        try {
            applicationContext.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            // Chrome browser presumably not installed so allow user to choose instead
            intent.setPackage(null)
            applicationContext.startActivity(intent)
        }
    }

    private fun fetchXmlData(){
        FirebaseDatabase.getInstance().getReference("udidData")
            .get().addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    for(string in snapshot.children){
                        val document = convertStringToXmlDocument(string.value as String)
                        val udid =  document!!.documentElement.getElementsByTagName("Person").item(0).attributes.getNamedItem("uid").nodeValue
                        Log.d("tag", "fetchXmlData: $udid")
                        val mobileNo = document!!.documentElement.getElementsByTagName("Person").item(0).attributes.getNamedItem("phone").nodeValue
                        if(udidNumber == udid.toString() ){
                            val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                                .setPhoneNumber("+91"+mobileNo)       // Phone number to verify
                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                .setActivity(this)                 // Activity (for callback binding)
                                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                .build()
                            mobileNumber = mobileNo
                            category = document!!.documentElement.getElementsByTagName("Person").item(0).attributes.getNamedItem("category").nodeValue
                            gender = document!!.documentElement.getElementsByTagName("Person").item(0).attributes.getNamedItem("gender").nodeValue
                            dob = document!!.documentElement.getElementsByTagName("Person").item(0).attributes.getNamedItem("dob").nodeValue
                            udidNumber = udid
                            loadingDialog.startLoading()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                            break
                        }
                    }
                }
            }
    }

    private fun permissionChecker() {
        Dexter.withContext(this)
            .withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    cropImage.launch(options { setGuidelines(CropImageView.Guidelines.ON)
                        setCropShape(CropImageView.CropShape.RECTANGLE_HORIZONTAL_ONLY)
                        setAspectRatio(4,3)
                        setAutoZoomEnabled(true)
                        setFixAspectRatio(true)
                        setScaleType(CropImageView.ScaleType.CENTER_CROP)
                    })
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    showRationalDialogForPermissions()
                    Toast.makeText(
                        applicationContext, "You have denied!! camera permissions",
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


    private var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            loadingDialog.isDismiss()
        }

        override fun onVerificationFailed(e: FirebaseException) {

            if (e is FirebaseAuthInvalidCredentialsException) {
                loadingDialog.isDismiss()
                Toast.makeText(this@SendOtpActivity, e.message, Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                loadingDialog.isDismiss()
                Toast.makeText(this@SendOtpActivity, e.message, Toast.LENGTH_SHORT).show()
            }

            // Show a message and update the UI
            loadingDialog.isDismiss()
            Toast.makeText(this@SendOtpActivity, e.message, Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            loadingDialog.isDismiss()
            val intent = Intent(this@SendOtpActivity, OtpVerification::class.java)
            intent.putExtra("mobile",mobileNumber)
            intent.putExtra("backendotp",verificationId)
            intent.putExtra("udidNumber", udidNumber)
            intent.putExtra("percentageOfDisability", percentageOfDisability)
            intent.putExtra("disabilityCategory", binding.tvDisability.text.toString())
            intent.putExtra("gender", gender)
            intent.putExtra("category" , category)
            intent.putExtra("dob", dob)
            startActivity(intent)
            finish()
            Toast.makeText(this@SendOtpActivity, "Code sent!!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertStringToXmlDocument(xmlString : String) : Document?{
        val factory = DocumentBuilderFactory.newInstance()

        var builder : DocumentBuilder? = null

        try {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            return builder.parse(InputSource(StringReader(xmlString)))
        } catch ( e : Exception) {
            e.printStackTrace();
        }
        return null;
    }

    private fun imageProcess(uri: Uri?) {

        udidDataList.clear()
        var bitmap: Bitmap? = null
        try {

            val parcelFileDescriptor =
              contentResolver.openFileDescriptor(uri!!, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.d("bitmap", "imageProcess: $bitmap")
        val textRecognizer = TextRecognizer.Builder(this).build()

        if (!textRecognizer.isOperational) {
            Toast.makeText(this, "Some error occured", Toast.LENGTH_LONG).show()
        } else {
            val frame = Frame.Builder().setBitmap(bitmap).build()

            val textBlockSparseArray = textRecognizer.detect(frame)
            var s = "s"
            for (i in 0 until textBlockSparseArray.size()) {
                val textBlock = textBlockSparseArray.valueAt(i)
//                Log.d("imageProcessing", "imageProcess: ${textBlock.value}")

                s = s + " " + textBlock.value.trim().replace("\n", " ")
            }
            Log.d("imageProcessing", "imageProcess: ${s}")
            udidDataList.addAll(s.lowercase().trim().split(" "))

            Log.d("imageProcessing", "imageProcess: $udidDataList")
            conditionValidation()
            for (str in udidDataList) {
                val charArray = str.contains("%")
                val isUdidNo = isLetters(str.chunked(2)[0])
                if (charArray) {
                    val disabilityPercentage = str.chunked(3)[0]
                    percentageOfDisability = disabilityPercentage
                    binding.previewTextPercentage.text = "Percentage of disability: " + disabilityPercentage + "\n"
                    Log.d("percent", "imageProcess: $disabilityPercentage")
                }

                if (str.length >= 17 && isUdidNo) {
                    Log.d("dataProcessing", "imageProcess: ${str.uppercase().trim()}")
                    udidNumber = str.uppercase().trim()
                    binding.previewTextUDIDNo.text = "UDID no: " + str.uppercase().trim() + "\n"
                }
            }
        }
    }
    private fun conditionValidation(){
        if(udidDataList.contains("hearing") && udidDataList.contains("orthopedic")){
            hearingDisability = hearingDisabilityConst
            orthopedicDisability = orthopedicDisabilityConst
            binding.tvDisability.text = "$hearingDisability , $orthopedicDisability"
            Log.d("tagData", "conditionValidation: Hearing Disability Orthopedic Disability")
        }
        else if(udidDataList.contains("hearing") && udidDataList.contains("visual")){
            hearingDisability = hearingDisabilityConst
            visualDisability = visualSisabilityConst
            binding.tvDisability.text = "$hearingDisability , $visualDisability"
            Log.d("tagData", "conditionValidation: Hearing Disability Visually Disability")
        }
        else  if(udidDataList.contains("hearing") && udidDataList.contains("mentally")){
            hearingDisability = hearingDisabilityConst
            mentalDisability = mentallyAndMultipleDisabilityConst
            binding.tvDisability.text = "$hearingDisability , $mentalDisability"
            Log.d("tagData", "conditionValidation: Hearing Disability Mental Disability")
        }
        else if(udidDataList.contains("orthopedic") && udidDataList.contains("visual")){
            orthopedicDisability = orthopedicDisabilityConst
            visualDisability = visualSisabilityConst
            binding.tvDisability.text = "$orthopedicDisability , $visualDisability"
            Log.d("tagData", "conditionValidation: Orthopedic Disability Visually Disability")
        }
        else if(udidDataList.contains("orthopedic") && udidDataList.contains("mentally")){
            orthopedicDisability = orthopedicDisabilityConst
            mentalDisability = mentallyAndMultipleDisabilityConst
            binding.tvDisability.text = "$orthopedicDisability , $mentalDisability"
            Log.d("tagData", "conditionValidation: Orthopedic Disability Mentally Disability")
        }
        else if(udidDataList.contains("mentally") && udidDataList.contains("visual")){
            mentalDisability = mentallyAndMultipleDisabilityConst
            visualDisability = visualSisabilityConst
            binding.tvDisability.text = "$orthopedicDisability , $visualDisability"

            Log.d("tagData", "conditionValidation: Visual Disability Mentally Disability")
        }
        else if(udidDataList.contains("mentally") && udidDataList.contains("visual") && udidDataList.contains("orthopedic")){
            mentalDisability = mentallyAndMultipleDisabilityConst
            visualDisability = visualSisabilityConst
            orthopedicDisability = orthopedicDisabilityConst
            binding.tvDisability.text = "$orthopedicDisability , $visualDisability , $mentalDisability"
            Log.d("tagData", "conditionValidation: Visual Disability Mentally Disability Orthopedic")
        }
        else if(udidDataList.contains("orthopedic") && udidDataList.contains("mentally") && udidDataList.contains("hearing")){
            orthopedicDisability = orthopedicDisabilityConst
            mentalDisability = mentallyAndMultipleDisabilityConst
            hearingDisability = hearingDisabilityConst
            binding.tvDisability.text = "$orthopedicDisability , $hearingDisability , $mentalDisability"
            Log.d("tagData", "conditionValidation: Visual Disability Mentally Disability Orthopedic")
        }
        else if(udidDataList.contains("orthopedic") && udidDataList.contains("visual") && udidDataList.contains("hearing")){
            orthopedicDisability = orthopedicDisabilityConst
            visualDisability  = visualSisabilityConst
            hearingDisability = hearingDisabilityConst
            binding.tvDisability.text = "$orthopedicDisability , $visualDisability , $hearingDisability"
            Log.d("tagData", "conditionValidation: Visual Disability Orthopedic Disability Hearing disability")
        }
        else if(udidDataList.contains("mentally")){
            mentalDisability = mentallyAndMultipleDisabilityConst
            binding.tvDisability.text = "$mentalDisability"
            Log.d("tagData", "conditionValidation: Mentally Disability ")
        }
        else if(udidDataList.contains("hearing")){
            hearingDisability = hearingDisabilityConst
            binding.tvDisability.text = "$hearingDisability"
            Log.d("tagData", "conditionValidation: Hearing Disability ")
        }
        else if(udidDataList.contains("orthopedic")){
            orthopedicDisability = orthopedicDisabilityConst
            binding.tvDisability.text = "$orthopedicDisability"
            Log.d("tagData", "conditionValidation: Orthopedic Disability ")
        } else if(udidDataList.contains("visual")){
            visualDisability = visualSisabilityConst
            binding.tvDisability.text = "$visualDisability"
            Log.d("tagData", "conditionValidation: Visual Disability ")
        }
    }

    private fun isLetters(string: String): Boolean {
        return string.none { it !in 'A'..'Z' && it !in 'a'..'z' }
    }
}