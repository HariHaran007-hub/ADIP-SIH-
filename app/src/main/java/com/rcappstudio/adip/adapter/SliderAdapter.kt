package com.rcappstudio.adip.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.ClickListener
import com.rcappstudio.adip.data.model.UpdatesBanner
import com.rcappstudio.adip.databinding.ItemOnboardingBinding
import com.rcappstudio.adip.databinding.SlideItemBannerBinding
import com.squareup.picasso.Picasso

class SliderAdapter(
    private val context: Context,
    private val bannerList: MutableList<UpdatesBanner>,
    private val viewPager2: ViewPager2,
    private val onclick : (UpdatesBanner, Int) -> Unit
    ): RecyclerView.Adapter<SliderAdapter.ViewHolder>() {

    private var _binding: SlideItemBannerBinding? = null
    private val binding get() = _binding!!

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        _binding = SlideItemBannerBinding.inflate(LayoutInflater.from(context), parent, false)
        val view = binding.root
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val banner = bannerList[position]
        Picasso.get()
            .load(banner.imageUrl)
            .fit()
            .centerCrop()
            .into(binding.imageSlide)

        binding.root.setOnClickListener{
            onclick.invoke(banner, position)
        }

//        if(bannerList.size - 2 == position ){
//            viewPager2.post(runnable)
//        }
    }

    override fun getItemCount(): Int {
       return bannerList.size
    }

//    val runnable = Runnable{
//        bannerList.addAll(bannerList)
//        notifyDataSetChanged()
//    }

}