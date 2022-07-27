package com.rcappstudio.adip.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.model.RequestStatus
import com.rcappstudio.adip.databinding.ActivityMainBinding
import com.rcappstudio.adip.utils.Constants
import com.rcappstudio.adip.utils.LoadingDialog


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private lateinit var loadingDialog : LoadingDialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        checkNetworkConnectivity()
        setContentView(binding.root)
        initBottomNavigationView()
        generateFcmToken()


        val pref = applicationContext.getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE)

        val userPath= pref.getString(Constants.USER_PROFILE_PATH, null)

        FirebaseDatabase.getInstance().getReference("${userPath!!}/requestStatus").get()
            .addOnSuccessListener {
                if(it.exists()){
                    val userModel = it.getValue(RequestStatus::class.java)
                    Log.d("UserDataIS", "onCreate: "+ userModel!!.latLng!!.lat)
                } else{
                    Log.d("UserData", "onCreate: incorrect path reference")
                }
            }
    }

    private fun initBottomNavigationView(){
        binding.bottomNavMenu.setItemSelected(R.id.home)
        binding.bottomNavMenu.setOnItemSelectedListener { item ->
            when(item){
               R.id.home->{
                    switchToFragment(R.id.homeFragment)
               }
                R.id.ngoLocation->{
                    switchToFragment(R.id.ngoMapsFragment)
                }
                R.id.registrationPortal->{
                    switchToFragment(R.id.registrationCategoryFragment)
                }
                R.id.requestStatus->{
                    switchToFragment(R.id.applicationStatusFragment)
                }
            }
        }
    }

    private fun checkNetworkConnectivity(){

    }



    private fun getNavController(): NavController {
        return (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment).navController
    }

    private fun switchToFragment(destinationId: Int) {
        if (isFragmentInBackStack(destinationId)) {
            getNavController().popBackStack(destinationId, false)
        } else {
            getNavController().navigate(destinationId)
        }
    }

    private fun isFragmentInBackStack(destinationId: Int) =
        try {
            getNavController().getBackStackEntry(destinationId)
            true
        } catch (e: Exception) {
            false
        }

    override fun onBackPressed() {
        super.onBackPressed()
        binding.bottomNavMenu.setItemSelected(R.id.home)
    }

    private fun generateFcmToken(){
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            saveToken(it)
        }
    }

    private fun saveToken(token : String){
        val pref = applicationContext.getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE)
        val userPath = pref.getString(Constants.USER_PROFILE_PATH, null)
        FirebaseDatabase.getInstance().getReference("$userPath/fcmToken").setValue(token)
    }

}