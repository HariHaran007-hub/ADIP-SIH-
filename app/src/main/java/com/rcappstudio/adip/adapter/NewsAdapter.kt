package com.rcappstudio.adip.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rcappstudio.adip.R
import com.rcappstudio.adip.ui.news.NewsModel
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

class NewsAdapter (
    private val context : Context,
    private var newsList : MutableList<NewsModel>
    ) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvHeadLine: TextView = view.findViewById(R.id.headLinesTextView)
        val tvContent : TextView = view.findViewById(R.id.tvContent)
        val imageView : ImageView = view.findViewById(R.id.headLineImage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val news = newsList[position]
        Picasso.get()
            .load(news.imageUrl)
            .fit()
            .centerCrop()
            .into(holder.imageView)
        holder.tvContent.text = news.contentDescription
        holder.tvHeadLine.text = news.headLines
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    fun updateList(list : MutableList<NewsModel>){
        this.newsList = list
        notifyDataSetChanged()
    }
}