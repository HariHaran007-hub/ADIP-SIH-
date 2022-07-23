package com.rcappstudio.adip.ui.news

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.rcappstudio.adip.R
import com.rcappstudio.adip.databinding.FragmentNewsBinding
import com.rcappstudio.adip.utils.Constants
import java.util.*

class NewsFragment : Fragment() {

    private lateinit var binding : FragmentNewsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshLayoutSetup()
        getNewsFromDatabase()
    }

    private fun refreshLayoutSetup(){
        binding.refreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                Log.d("TAG", "onRefresh: refereshed")
                binding.refreshLayout.isRefreshing = false
            }
        })
    }

    private fun getNewsFromDatabase(){
        FirebaseDatabase.getInstance().getReference(Constants.NEWS).get()
            .addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    for(s in snapshot.children){
                        val news = s.getValue(NewsModel::class.java)
                        Log.d("NewsData", "getNewsFromDatabase: ${news!!.headLines}")
                    }
                }
            }
    }
}