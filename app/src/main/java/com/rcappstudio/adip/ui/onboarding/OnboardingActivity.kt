package com.rcappstudio.adip.ui.onboarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.rcappstudio.adip.data.ClickListener
import com.rcappstudio.adip.R
import com.rcappstudio.adip.adapter.SplashAdapter
import com.rcappstudio.adip.databinding.ActivityOnboardingBinding
import com.rcappstudio.adip.ui.activity.MainActivity

class OnboardingActivity : AppCompatActivity() , ClickListener {

    private lateinit var binding : ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        supportActionBar!!.hide()

        initViews()
    }

    private fun initViews() {
        //Setup view page adapter for images
        val pagerAdapter = SplashAdapter(this, this)
        binding.vpSplashImages.adapter = pagerAdapter

        binding.ivPageIndicator1.setOnClickListener { binding.vpSplashImages.currentItem = 0 }
        binding.ivPageIndicator2.setOnClickListener { binding.vpSplashImages.currentItem = 1 }
        binding.ivPageIndicator3.setOnClickListener { binding.vpSplashImages.currentItem = 2 }

        binding.vpSplashImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) { //Image change listener for updating layout components
                when (position) {
                    0 -> {
                        binding.ivPageIndicator1.setImageResource(R.drawable.img_page_indicator_active)
                        binding.ivPageIndicator2.setImageResource(R.drawable.img_page_indicator_inactive)
                        binding.ivPageIndicator3.setImageResource(R.drawable.img_page_indicator_inactive)

                        binding.btnCreate.text = "Next"
                    }
                    1 -> {
                        binding.ivPageIndicator2.setImageResource(R.drawable.img_page_indicator_active)
                        binding.ivPageIndicator1.setImageResource(R.drawable.img_page_indicator_inactive)
                        binding.ivPageIndicator3.setImageResource(R.drawable.img_page_indicator_inactive)

                        binding.btnCreate.text = "Next"
                    }
                    2 -> {
                        binding.ivPageIndicator3.setImageResource(R.drawable.img_page_indicator_active)
                        binding.ivPageIndicator2.setImageResource(R.drawable.img_page_indicator_inactive)
                        binding.ivPageIndicator1.setImageResource(R.drawable.img_page_indicator_inactive)

                        binding.btnCreate.text = "Let's create profile"
                    }
                }
            }
        })

        binding.btnCreate.setOnClickListener { buttonClick() }
    }

    override fun buttonClick() {
        when (binding.vpSplashImages.currentItem) {
            0 -> binding.vpSplashImages.currentItem = 1
            1 -> binding.vpSplashImages.currentItem = 2
            2 -> {
                TransitionManager.beginDelayedTransition(binding.root, AutoTransition())
                binding.vpSplashImages.visibility = View.INVISIBLE
                binding.lnrPageIndicator.visibility = View.INVISIBLE
                binding.btnCreate.visibility = View.INVISIBLE
                binding.tvHeading.visibility = View.VISIBLE

                Handler().postDelayed({
                    startActivity(Intent(this, SendOtpActivity::class.java))
                    finish()
                }, 1250)
            }
        }
    }

    override fun buttonClick(position: Int, type: String) {
        //
    }

    override fun buttonClick(position: Int) {
        //
    }

    override fun onStart() {
        if(FirebaseAuth.getInstance().currentUser != null){
            startActivity(Intent(this , MainActivity::class.java))
            finish()
        } else{
            super.onStart()
        }

    }

}