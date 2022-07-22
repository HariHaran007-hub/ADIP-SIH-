package com.rcappstudio.adip.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.messaging.FirebaseMessaging
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.model.AidsDoc
import com.rcappstudio.adip.data.model.RequestStatus
import com.rcappstudio.adip.data.model.UserModel
import com.rcappstudio.adip.databinding.ActivityMainBinding
import com.rcappstudio.adip.utils.Constants
import com.rcappstudio.adip.utils.LoadingDialog

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private lateinit var loadingDialog : LoadingDialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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
        binding.bottomNavMenu.setItemSelected(R.id.ngoLocation)
        binding.bottomNavMenu.setOnItemSelectedListener { item ->
            when(item){
                R.id.news->{
                    switchToFragment(R.id.newsFragment)
                }
                R.id.ngoLocation->{
                    switchToFragment(R.id.ngoMapsFragment)
                }
                R.id.registrationPortal->{
                    switchToFragment(R.id.registrationCategoryFragment)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.support_chat -> startActivity(Intent(this, ChatActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
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
        binding.bottomNavMenu.setItemSelected(R.id.ngoLocation)
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