package com.rcappstudio.adip

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.material.internal.ContextUtils.getActivity
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.rcappstudio.adip.databinding.ActivityOcrBinding
import com.rcappstudio.adip.ui.onboarding.DetailsCollectingActivity
import com.rcappstudio.adip.ui.onboarding.UploadProfileActivity
import java.io.FileDescriptor
import java.io.IOException

class OcrActivity : AppCompatActivity() {

    private lateinit var udidDataList: MutableList<String>

    val REQUEST_CAMERA_CODE = 100

    private lateinit var binding: ActivityOcrBinding

    private lateinit var visualDisability : String
    private lateinit var orthopedicDisability : String
    private lateinit var hearingDisability : String
    private lateinit var mentalDisability : String

    val orthopedicDisabilityConst = "Orthopedic disability"
    val hearingDisabilityConst = "Hearing disability"
    val visualSisabilityConst = "Visual disability"
    val mentallyAndMultipleDisabilityConst = "Mentally and multiple disability"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcrBinding.inflate(layoutInflater)
        setContentView(binding.root)



        udidDataList = mutableListOf()
        binding.uploadData.setOnClickListener {
            permissionChecker()
        }

        binding.uploadFileToNextPage.setOnClickListener {
            if(binding.tvDisability.text.isNotEmpty())
                showAlertDialog()
            else
                Snackbar.make(binding.root, "Please select the file", Snackbar.LENGTH_LONG).show()
        }
    }
    private val getImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        if (it != null) {
            binding.roundedImageView.setImageURI(it)
            imageProcess(it)
        }
    }
    private fun permissionChecker() {
        Dexter.withContext(this)
            .withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
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

    private fun imageProcess(uri: Uri?) {
        udidDataList.clear()
        var bitmap: Bitmap? = null
        try {

            val parcelFileDescriptor = contentResolver.openFileDescriptor(uri!!, "r")
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
//            Log.d("imageProcessing", "imageProcess: ${s}")
            udidDataList.addAll(s.lowercase().trim().split(" "))

            Log.d("imageProcessing", "imageProcess: $udidDataList")
            conditionValidation()
        }
    }


    private fun conditionValidation(){
        binding.indicationCardView.visibility = View.VISIBLE
        binding.tvDisabilityHeader.visibility = View.VISIBLE
        binding.tvDisability.visibility = View.VISIBLE
        binding.uploadFileToNextPage.visibility = View.VISIBLE

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

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure to update the data?")

        builder.setPositiveButton("Yes") { dialog, which ->
            Toast.makeText(this, "Yeah!!", Toast.LENGTH_SHORT).show()
            val intentVal = Intent(this, UploadProfileActivity::class.java)
            intentVal.putExtra(DetailsCollectingActivity.NAME,intent.getStringExtra(DetailsCollectingActivity.NAME) )
            intentVal.putExtra(DetailsCollectingActivity.UDID_NUMBER, intent.getStringExtra(DetailsCollectingActivity.UDID_NUMBER))
            intentVal.putExtra(DetailsCollectingActivity.DATE_OF_BIRTH, intent.getStringExtra(DetailsCollectingActivity.DATE_OF_BIRTH))
            intentVal.putExtra(DetailsCollectingActivity.STATE, intent.getStringExtra(DetailsCollectingActivity.STATE))
            intentVal.putExtra(DetailsCollectingActivity.DISTRICT, intent.getStringExtra(DetailsCollectingActivity.DISTRICT))
            intentVal.putExtra(DetailsCollectingActivity.MOBILE_NO, intent.getStringExtra(DetailsCollectingActivity.MOBILE_NO))
            intentVal.putExtra("disabilityCategory", binding.tvDisability.text.toString())
            startActivity(intentVal)
        }

        builder.setNegativeButton("No") { dialog, which ->
            Toast.makeText(
                this,
                "Noooo", Toast.LENGTH_SHORT
            ).show()
        }
        builder.show()
    }

}