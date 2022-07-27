package com.rcappstudio.adip.ui.news

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.FirebaseDatabase
import com.rcappstudio.adip.adapter.NewsAdapter
import com.rcappstudio.adip.databinding.ActivityNewsBinding
import com.rcappstudio.adip.utils.Constants

class NewsActivity : AppCompatActivity() {
    private lateinit var newsAdapter : NewsAdapter
    private lateinit var newsList: MutableList<NewsModel>

    private lateinit var binding : ActivityNewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        refreshLayoutSetup()
        getNewsFromDatabase()
    }

    private fun initRecyclerView(){
        newsList = mutableListOf()
        newsAdapter = NewsAdapter(applicationContext , newsList)
        binding.rvNews.layoutManager = LinearLayoutManager(applicationContext)
        binding.rvNews.adapter = newsAdapter
    }

    private fun refreshLayoutSetup(){
        binding.refreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                getNewsFromDatabase()
            }
        })
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
                    binding.refreshLayout.isRefreshing = false
                }
            }
    }
}