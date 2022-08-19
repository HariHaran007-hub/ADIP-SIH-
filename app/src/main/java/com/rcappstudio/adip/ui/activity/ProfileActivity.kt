package com.rcappstudio.adip.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.nl.translate.TranslateLanguage
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.model.UserModel
import com.rcappstudio.adip.databinding.ActivityProfileBinding
import com.rcappstudio.adip.utils.Constants
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var languageAdapter : ArrayAdapter<CharSequence>

    private var selectedLanguage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchUserDetails()
        initLangAdapter()
    }
    private fun initLangAdapter(){
        languageAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.array_language,
            R.layout.spinner_layout
        )
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.languageSpinner.adapter = languageAdapter;

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedLanguage = binding.languageSpinner.selectedItem.toString()
                val parentId : Int = adapterView!!.id

                if(parentId == R.id.languageSpinner){
                    when(selectedLanguage){

                        "தமிழ்"->{
                            applicationContext.getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE).edit().apply {
                                putString(Constants.LANGUAGE, TranslateLanguage.TAMIL)
                                Snackbar.make(binding.root, "Restart to see தமிழ் content", Toast.LENGTH_LONG).show()
                            }.commit()
                        }

                        "English"->{
                            applicationContext.getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE).edit().apply {
                                putString(Constants.LANGUAGE, TranslateLanguage.ENGLISH)
                                Snackbar.make(binding.root, "Restart to see English content", Toast.LENGTH_LONG).show()

                            }.commit()

                        }

                        "हिन्दी"->{
                            applicationContext.getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE).edit().apply {
                                putString(Constants.LANGUAGE, TranslateLanguage.HINDI)
                                Snackbar.make(binding.root, "Restart to see हिन्दी content", Toast.LENGTH_LONG).show()

                            }.commit()

                        }

                        "తెలుగు"->{
                            applicationContext.getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE).edit().apply {
                                putString(Constants.LANGUAGE, TranslateLanguage.TELUGU)
                                Snackbar.make(binding.root, "Restart to see తెలుగు content", Toast.LENGTH_LONG).show()
                            }.commit()
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
    }

    private fun fetchUserDetails(){
        val sharePref = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE)
        val userPath = sharePref.getString(Constants.USER_PROFILE_PATH, null)
        FirebaseDatabase.getInstance().getReference(userPath!!).get().addOnSuccessListener {
            if(it.exists()){
                initView(it.getValue(UserModel::class.java)!!)
            }
        }
    }

    private fun initView(user : UserModel){
        Picasso.get()
            .load(user.profileImageUrl)
            .fit().centerCrop()
            .into(binding.circleImageView)

        binding.tvMobileNo.text = "Mobile: ${user.mobileNo}"
        binding.tvName.text = "Name: ${user.name}"
        binding.tvUdidNo.text = "UDID no: ${user.udidNo}"
        binding.tvDOB.text = "DOB: ${user.dateOfBirth}"
        binding.tvState.text = "State: ${user.state}"
        binding.tvDistrict.text = "District: ${user.district}"
    }
}