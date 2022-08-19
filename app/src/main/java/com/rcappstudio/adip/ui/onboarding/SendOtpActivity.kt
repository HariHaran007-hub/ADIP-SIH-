package com.rcappstudio.adip.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase

import com.rcappstudio.adip.databinding.ActivitySendOtpBinding
import com.rcappstudio.adip.utils.LoadingDialog
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import java.lang.Exception
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


class SendOtpActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySendOtpBinding
    private lateinit var loadingDialog : LoadingDialog
    private lateinit var mobileNumber : String
    private lateinit var udidNumber : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()
        loadingDialog = LoadingDialog(this, "Sending OTP to +91 ${binding.inputMobNo.text.toString().trim()}")



        binding.btnsend.setOnClickListener{
            fetchXmlData()
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
                        if(binding.inputMobNo.text.toString() == udid.toString() ){
                            val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                                .setPhoneNumber("+91"+mobileNo)       // Phone number to verify
                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                .setActivity(this)                 // Activity (for callback binding)
                                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                .build()
                            mobileNumber = mobileNo
                            udidNumber = udid
                            loadingDialog.startLoading()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                            break
                        }
                    }
                }
            }
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
}