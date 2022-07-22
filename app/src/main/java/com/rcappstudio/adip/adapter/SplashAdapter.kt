package com.rcappstudio.adip.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rcappstudio.adip.data.ClickListener
import com.rcappstudio.adip.R
import com.rcappstudio.adip.databinding.ItemOnboardingBinding

class SplashAdapter(private val context: Context, private val listener: ClickListener): RecyclerView.Adapter<SplashAdapter.ViewHolder>() {

    private var _binding: ItemOnboardingBinding? = null
    private val binding get() = _binding!!

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val imageList: List<Int> = listOf(
        R.raw.verification,
        R.raw.news,
        R.raw.find_location
    )
    private val headingList: List<String> = listOf(context.getString(R.string.verification),
        context.getString(R.string.provide),
        context.getString(R.string.location_ngo)
        )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        _binding = ItemOnboardingBinding.inflate(LayoutInflater.from(context), parent, false)
        val view = binding.root
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        binding.ivSplashBackground.setAnimation(imageList[position])
        binding.tvHeading.text = headingList[position]
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

}