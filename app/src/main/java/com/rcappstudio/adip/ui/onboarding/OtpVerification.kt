package com.rcappstudio.adip.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.model.UserIdList
import com.rcappstudio.adip.ui.activity.MainActivity
import com.rcappstudio.adip.utils.Constants
import java.util.concurrent.TimeUnit


class OtpVerification : AppCompatActivity() {
    var et1: EditText? = null
    var et2: EditText? = null
    var et3: EditText? = null
    var et4: EditText? = null
    var et5: EditText? = null
    var et6: EditText? = null
    var btnsubmit: Button? = null
    var getbackendotp: String? = null
    var progressBar: ProgressBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        et1 = findViewById<EditText>(R.id.inputotp1)
        et2 = findViewById<EditText>(R.id.inputotp2)
        et3 = findViewById<EditText>(R.id.inputotp3)
        et4 = findViewById<EditText>(R.id.inputotp4)
        et5 = findViewById<EditText>(R.id.inputotp5)
        et6 = findViewById<EditText>(R.id.inputotp6)
        progressBar = findViewById<ProgressBar>(R.id.probar2)
        Log.d("TAG", "onCreate: "+ intent.getStringExtra("mobile"))


        val textView = findViewById<TextView>(R.id.txtmobileno)
        textView.text = String.format(
            "+91-%S", intent.getStringExtra("mobile")
        )
        getbackendotp = intent.getStringExtra("backendotp")
        btnsubmit = findViewById<Button>(R.id.btnsubmit) as Button
        btnsubmit!!.setOnClickListener{
            if (et1!!.text.toString().trim { it <= ' ' }.isNotEmpty() && et2!!.text.toString()
                    .trim { it <= ' ' }.isNotEmpty()
                && et3!!.text.toString().trim { it <= ' ' }.isNotEmpty()
                && et4!!.text.toString().trim { it <= ' ' }.isNotEmpty()
                && et5!!.text.toString().trim { it <= ' ' }.isNotEmpty()
                && et6!!.text.toString().trim { it <= ' ' }.isNotEmpty()
            ) {

                // marging user's input in a string
                val getuserotp = et1!!.text.toString() +
                        et2!!.text.toString() +
                        et3!!.text.toString() +
                        et4!!.text.toString() +
                        et5!!.text.toString() +
                        et6!!.text.toString()
                if (getbackendotp != null) {
                    progressBar!!.visibility = View.VISIBLE
                    btnsubmit!!.visibility = View.INVISIBLE
                    val phoneAuthCredential = PhoneAuthProvider.getCredential(
                        getbackendotp!!, getuserotp
                    )
                    FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                        .addOnCompleteListener { task ->
                            progressBar!!.visibility = View.GONE
                            btnsubmit!!.visibility = View.VISIBLE
                            if (task.isSuccessful) {
                                val mobileNo = intent.getStringExtra("mobile")

//                                intent.flags =
//                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                FirebaseDatabase.getInstance()
                                    .getReference("${Constants.USER_ID_LIST}/${FirebaseAuth.getInstance().uid}")
                                    .get().addOnSuccessListener {
                                        if(it.exists()){
                                            val userData = it.getValue(UserIdList::class.java)
                                            val userPath = "${Constants.USER}/${userData!!.state}/${userData.district}/${FirebaseAuth.getInstance().uid}"


                                            application.getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE)
                                                .edit().apply {
                                                    putString(Constants.STATE, userData.state)
                                                    putString(Constants.DISTRICT, userData.district)
                                                    putString(Constants.USER_PROFILE_PATH , userPath)
                                                }.apply()

                                            startActivity(Intent(applicationContext , MainActivity::class.java))
                                            finish()
                                        }
                                        else{
                                            val intent = Intent(applicationContext,DetailsCollectingActivity::class.java)
                                            intent.putExtra("mobile", mobileNo)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }

                            } else {
                                Toast.makeText(
                                    this@OtpVerification,
                                    "Enter correct OTP",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(this@OtpVerification, "Please check internet", Toast.LENGTH_SHORT)
                        .show()
                }

                //Toast.makeText(MainActivity2.this, "OTP Verify", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this@OtpVerification, "Please fill all number", Toast.LENGTH_SHORT)
                    .show()
            }
            // movenumtonext();
        }
        findViewById<View>(R.id.sendotp_again).setOnClickListener {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + intent.getStringExtra("mobile"),
                60,
                TimeUnit.SECONDS,
                this@OtpVerification,
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {}
                    override fun onVerificationFailed(e: FirebaseException) {
                        Toast.makeText(this@OtpVerification, e.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onCodeSent(
                        newbackendotp: String,
                        forceResendingToken: PhoneAuthProvider.ForceResendingToken
                    ) {
                        getbackendotp = newbackendotp
                        Toast.makeText(
                            this@OtpVerification,
                            "OTP Send Sucessfuly",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
        movenumtonext() //move num to next
    }

    private fun movenumtonext() {
        et1!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString().trim { it <= ' ' }.isNotEmpty()) {
                    et2!!.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        et2!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString().trim { it <= ' ' }.isNotEmpty()) {
                    et3!!.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        et3!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString().trim { it <= ' ' }.isNotEmpty()) {
                    et4!!.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        et4!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString().trim { it <= ' ' }.isNotEmpty()) {
                    et5!!.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        et5!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString().trim { it <= ' ' }.isNotEmpty()) {
                    et6!!.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }
}