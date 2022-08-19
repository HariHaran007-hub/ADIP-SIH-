package com.rcappstudio.adip.ui.activity

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.rcappstudio.adip.R
import com.rcappstudio.adip.databinding.ActivitySplashBinding
import com.rcappstudio.adip.ui.onboarding.OnboardingActivity
import com.rcappstudio.adip.utils.Constants

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var translator: Translator
    private lateinit var binding : ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = ActivitySplashBinding.inflate(layoutInflater)

        setContentView(binding.root)
        prepareModel()
        val colorFrom = ResourcesCompat.getColor(resources, R.color.white, null)
        val colorTo = ResourcesCompat.getColor(resources, R.color.pinkFaded, null)

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 2000 // milliseconds

        colorAnimation.addUpdateListener { animator -> binding.rlRoot.setBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.addUpdateListener { animator -> binding.rlRoot.setBackgroundColor(animator.animatedValue as Int) }

        Handler().postDelayed({
            colorAnimation.start()
        }, 500)

        Handler().postDelayed({
            TransitionManager.beginDelayedTransition(binding.root, AutoTransition())
            binding.lottieAnimationView2.elevation = 5F
            binding.appName.visibility = View.GONE
            binding.knotIdea.visibility = View.VISIBLE

            Handler().postDelayed({
                if(FirebaseAuth.getInstance().currentUser != null){
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else{
                    startActivity(Intent(this, OnboardingActivity::class.java))
                    finish()
                }
            } , 600)

        }, 3000)

        }
    private fun prepareModel(){
        val sharedPreferences = getSharedPreferences(
            Constants.SHARED_PREF_FILE,
            Context.MODE_PRIVATE
        )
        val lang = sharedPreferences.getString(Constants.LANGUAGE, null)
        if(lang != null){
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(lang)
                .build()
            translator = Translation.getClient(options)

            translator.downloadModelIfNeeded().addOnSuccessListener {

            }.addOnFailureListener {

            }
        } else{
            sharedPreferences.edit().putString(Constants.LANGUAGE , TranslateLanguage.ENGLISH).apply()
        }
    }
}