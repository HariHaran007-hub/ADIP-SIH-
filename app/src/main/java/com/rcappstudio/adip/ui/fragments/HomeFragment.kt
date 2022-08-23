package com.rcappstudio.adip.ui.fragments

import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.rcappstudio.adip.R
import com.rcappstudio.adip.adapter.NewsAdapter
import com.rcappstudio.adip.adapter.SliderAdapter
import com.rcappstudio.adip.data.model.UpdatesBanner
import com.rcappstudio.adip.databinding.FragmentHomeBinding
import com.rcappstudio.adip.ui.activity.AgencyActivity
import com.rcappstudio.adip.ui.activity.ChatActivity
import com.rcappstudio.adip.ui.activity.ProfileActivity
import com.rcappstudio.adip.ui.news.NewsActivity
import com.rcappstudio.adip.ui.news.NewsModel
import com.rcappstudio.adip.ui.onboarding.WebViewActivity
import com.rcappstudio.adip.utils.Constants
import com.rcappstudio.adip.utils.isConnected

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var bannerList : MutableList<UpdatesBanner>

    private lateinit var newsAdapter : NewsAdapter
    private lateinit var newsList: MutableList<NewsModel>
    private lateinit var isdialog : AlertDialog
    private lateinit var inflater: LayoutInflater
    private lateinit var translator : Translator
    private  var toHindi : Boolean = true

    val english = "en"
    val tamil = "ta"
    val hindi = "hi"
    var lang = ""
    private var voiceUrl = "https://translate.google.com/translate_tts?ie=UTF-&&client=tw-ob&tl=${english}&q="

    private val sliderRunnable = Runnable{
        binding.updatesBanner.currentItem = binding.updatesBanner.currentItem + 1
    }


    private var sliderHandler = Handler()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.inflater = inflater
        binding = FragmentHomeBinding.inflate(layoutInflater)
        binding.customToolBar.toolbar.title = "Home"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareModel()
        showNetworkErrorDialog()
        binding.scroll.isSmoothScrollingEnabled = true
        binding.scroll.fullScroll(View.FOCUS_DOWN)
        binding.rvNews.isNestedScrollingEnabled = false
        init()

    }

    private fun init(){
        if(isConnected(requireContext())){
            fetchBannerDetails()
            initRecyclerView()
            getNewsFromDatabase()
            clickListener()
            isdialog.dismiss()
        } else{
            isdialog.show()
            Log.d("networkState", "onViewCreated: no internet")
        }
    }

    private fun fetchBannerDetails(){
        bannerList = mutableListOf()
        FirebaseDatabase.getInstance().getReference("updatesBanner")
            .get().addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    for (c in snapshot.children){
                        val banner = c.getValue(UpdatesBanner::class.java)
                        bannerList.add(banner!!)
                    }
                    initViewPager()
                }
            }
    }

    private fun initViewPager(){

        binding.updatesBanner.adapter = SliderAdapter(requireContext(),bannerList, binding.updatesBanner){
            item, pos->
            val intent =  Intent(requireContext(), WebViewActivity::class.java)
            intent.putExtra("url", item.contentUrl)
            startActivity(intent)

        }

        binding.updatesBanner.clipToPadding = false
        binding.updatesBanner.clipChildren = false
        binding.updatesBanner.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer()

        compositePageTransformer.addTransformer(MarginPageTransformer(40))
        compositePageTransformer.addTransformer(object : ViewPager2.PageTransformer{
            override fun transformPage(page: View, position: Float) {
                var r = 1 - Math.abs(position).toFloat()
                page.scaleY = (0.85 + r * 0.15f).toFloat()
            }

        })
        binding.updatesBanner.setPageTransformer(compositePageTransformer)

        binding.updatesBanner.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable,3000)
            }

        })
    }
    private fun initRecyclerView(){
        newsList = mutableListOf()
        newsAdapter = NewsAdapter(requireContext() , newsList, translator, voiceUrl)
        binding.rvNews.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvNews.adapter = newsAdapter
    }

    private fun getNewsFromDatabase(){
        newsList.clear()
        FirebaseDatabase.getInstance().getReference(Constants.NEWS).get()
            .addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    for(s in snapshot.children){
                        val news = s.getValue(NewsModel::class.java)
                        newsList.add(news!!)
                    }
                    newsAdapter.updateList(newsList)
                }
            }
    }

    private fun clickListener(){
        binding.supportChat.setOnClickListener{
            startActivity(Intent(requireContext(), ChatActivity::class.java))
        }

        binding.profile.setOnClickListener{
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        binding.news.setOnClickListener{
            startActivity(Intent(requireContext(), NewsActivity::class.java))
        }
        binding.agency.setOnClickListener {
            startActivity(Intent(requireContext(), AgencyActivity::class.java))
        }
    }

    private fun showNetworkErrorDialog(){

        val dialogView = inflater.inflate(R.layout.no_internet, null)
        val builder = AlertDialog.Builder(requireContext())
        val retryButton = dialogView.findViewById<Button>(R.id.retry)
        retryButton.setOnClickListener {
            init()
        }
        builder.setView(dialogView)
        builder.setCancelable(false)
        isdialog = builder.create()
    }

    private fun prepareModel(){
        val sharedPreferences = requireContext().getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE)
            .getString(Constants.LANGUAGE, null)
        if(sharedPreferences != null){
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(sharedPreferences)
                .build()
            translator = Translation.getClient(options)

            if(sharedPreferences == TranslateLanguage.TAMIL)
                voiceUrl = "https://translate.google.com/translate_tts?ie=UTF-&&client=tw-ob&tl=${tamil}&q="
            else if(sharedPreferences == TranslateLanguage.HINDI)
                voiceUrl = "https://translate.google.com/translate_tts?ie=UTF-&&client=tw-ob&tl=${hindi}&q="


            translator.downloadModelIfNeeded().addOnSuccessListener {
                translateLanguage()
            }.addOnFailureListener {

            }
        }
    }

    private fun translateLanguage() {
        translator.translate(binding.tvProfile.text.toString()).addOnSuccessListener {
            Log.d("tabData", "translateLanguage: $it")
            binding.tvProfile.text = it
        }

        binding.profile.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.tvProfile.text.toString())
            mp.prepare()
            mp.start()
            true
        }

        translator.translate(binding.tvSupport.text.toString()).addOnSuccessListener {
            Log.d("tabData", "translateLanguage: $it")
            binding.tvSupport.text = it
        }

        binding.supportChat.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.tvSupport.text.toString())
            mp.prepare()
            mp.start()
            true
        }



        translator.translate(binding.tvNews.text.toString()).addOnSuccessListener {
            Log.d("tabData", "translateLanguage: $it")
            binding.tvNews.text = it
        }

        binding.news.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.tvNews.text.toString())
            mp.prepare()
            mp.start()
            true
        }

        translator.translate(binding.tvAgency.text.toString()).addOnSuccessListener {
            Log.d("tabData", "translateLanguage: $it")
            binding.tvAgency.text = it
        }

        binding.agency.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.tvAgency.text.toString())
            mp.prepare()
            mp.start()
            true
        }

        translator.translate(binding.tvRequestStatus.text.toString()).addOnSuccessListener {
            Log.d("tabData", "translateLanguage: $it")
            binding.tvRequestStatus.text = it
        }

        binding.tvRequestStatus.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.tvRequestStatus.text.toString())
            mp.prepare()
            mp.start()
            true
        }

        translator.translate(binding.tvLatestNews.text.toString()).addOnSuccessListener {
            Log.d("tabData", "translateLanguage: $it")
            binding.tvLatestNews.text = it
        }

        binding.tvLatestNews.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.tvLatestNews.text.toString())
            mp.prepare()
            mp.start()
            true
        }

        translator.translate(binding.tvSchemeStatistics.text.toString()).addOnSuccessListener {
            Log.d("tabData", "translateLanguage: $it")
            binding.tvSchemeStatistics.text = it
        }
        binding.tvSchemeStatistics.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.tvSchemeStatistics.text.toString())
            mp.prepare()
            mp.start()
            true
        }


        translator.translate(binding.tvNoOfApplication.text.toString()).addOnSuccessListener {
            Log.d("tabData", "translateLanguage: $it")
            binding.tvNoOfApplication.text = it
        }

        binding.tvNoOfApplication.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.tvNoOfApplication.text.toString())
            mp.prepare()
            mp.start()
            true
        }

        translator.translate(binding.tvNoOfVerification.text.toString()).addOnSuccessListener {
            Log.d("tabData", "translateLanguage: $it")
            binding.tvNoOfVerification.text = it
        }

        binding.tvNoOfVerification.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.tvNoOfVerification.text.toString())
            mp.prepare()
            mp.start()
            true
        }

        translator.translate(binding.customToolBar.toolbar.title.toString()).addOnSuccessListener {
            binding.customToolBar.toolbar.title = it
        }

        binding.customToolBar.toolbar.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + binding.customToolBar.toolbar.title.toString())
            mp.prepare()
            mp.start()
            true
        }
    }

}