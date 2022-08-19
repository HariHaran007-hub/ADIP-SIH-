package com.rcappstudio.adip.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.nl.translate.Translator
import com.rcappstudio.adip.adapter.NewsAdapter
import com.rcappstudio.adip.databinding.FragmentNewsBinding
import com.rcappstudio.adip.ui.news.NewsModel
import com.rcappstudio.adip.utils.Constants

class NewsFragment : Fragment() {

    private lateinit var newsAdapter : NewsAdapter
    private lateinit var newsList: MutableList<NewsModel>
    private lateinit var translator : Translator

    private lateinit var binding : FragmentNewsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        refreshLayoutSetup()
        getNewsFromDatabase()
    }

    private fun initRecyclerView(){
        newsList = mutableListOf()
        newsAdapter = NewsAdapter(requireContext() , newsList, translator, )
        binding.rvNews.layoutManager = LinearLayoutManager(requireContext())
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