package com.rcappstudio.adip.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.model.NgoData
import com.rcappstudio.adip.data.model.RequestStatus
import com.rcappstudio.adip.databinding.ActivityMainBinding
import com.rcappstudio.adip.utils.Constants
import com.rcappstudio.adip.utils.LoadingDialog


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        supportActionBar!!.hide()
        checkNetworkConnectivity()
        setContentView(binding.root)
        initBottomNavigationView()
        generateFcmToken()


        val shardPref = getSharedPreferences(Constants.SHARED_PREF_FILE , MODE_PRIVATE)
        val userPath = shardPref.getString(Constants.USER_PROFILE_PATH, null )!!
//        FirebaseDatabase.getInstance().getReference("$userPath/requestStatus/1660606584448/ngoList")
//            .push().setValue(NgoData(mutableListOf("Hearing aids", "Educational kits", "Assistive and alarm devices") ,false, "-N8rxCZD6AXF44pu3ASo" ))
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

    private fun addAidsDatabase(){
        FirebaseDatabase.getInstance().getReference("")
    }

}