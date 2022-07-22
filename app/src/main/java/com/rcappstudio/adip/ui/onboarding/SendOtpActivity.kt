package com.rcappstudio.adip.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*

import com.rcappstudio.adip.databinding.ActivitySendOtpBinding
import com.rcappstudio.adip.utils.LoadingDialog
import java.util.concurrent.TimeUnit


class SendOtpActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySendOtpBinding
    private lateinit var loadingDialog : LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this, "Sending OTP to +91 ${binding.inputMobNo.text.toString().trim()}")

        supportActionBar!!.hide()

        binding.btnsend.setOnClickListener{

            val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber("+91"+binding.inputMobNo.text.toString().trim())       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
            loadingDialog.startLoading()
            PhoneAuthProvider.verifyPhoneNumber(options)
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
            intent.putExtra("mobile",binding.inputMobNo.text.toString())
            intent.putExtra("backendotp",verificationId)
            startActivity(intent)
            finish()
            Toast.makeText(this@SendOtpActivity, "Code sent!!", Toast.LENGTH_SHORT).show()
        }
    }
}