package com.rcappstudio.adip.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.FirebaseDatabase
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.model.UserModel
import com.rcappstudio.adip.databinding.ActivityNewsBinding
import com.rcappstudio.adip.databinding.ActivityProfileBinding
import com.rcappstudio.adip.utils.Constants
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchUserDetails()
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