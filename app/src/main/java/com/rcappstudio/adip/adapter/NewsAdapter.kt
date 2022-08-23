package com.rcappstudio.adip.adapter

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.rcappstudio.adip.R
import com.rcappstudio.adip.ui.news.NewsModel
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

class NewsAdapter (
    private val context : Context,
    private var newsList : MutableList<NewsModel>,
    private var translator: Translator,
    private var voiceUrl : String
    ) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvHeadLine: TextView = view.findViewById(R.id.headLinesTextView)
        val tvContent : TextView = view.findViewById(R.id.tvContent)
        val imageView : ImageView = view.findViewById(R.id.headLineImage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       // prepareModel()
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val news = newsList[position]
        translator.translate(news.contentDescription!!).addOnSuccessListener {
                holder.tvContent.text = it
        }
        holder.tvContent.setOnLongClickListener{
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + holder.tvContent.text.toString())
            mp.prepare()
            mp.start()
            true
        }
        translator.translate( news.headLines!!).addOnSuccessListener {
                holder.tvHeadLine.text = it
        }

        holder.tvHeadLine.setOnLongClickListener {
            val mp = MediaPlayer()
            mp.setDataSource(voiceUrl + holder.tvHeadLine.text.toString())
            mp.prepare()
            mp.start()
            true
        }
        Picasso.get()
            .load(news.imageUrl)
            .fit()
            .centerCrop()
            .into(holder.imageView)

    }

    override fun getItemCount(): Int {
        return newsList.size
    }

//    private fun prepareModel(){
//        val options = TranslatorOptions.Builder()
//            .setSourceLanguage(TranslateLanguage.ENGLISH)
//            .setTargetLanguage(TranslateLanguage.TAMIL)
//            .build()
//        translator = Translation.getClient(options)
//
//
//    }

    fun updateList(list : MutableList<NewsModel>){
        this.newsList = list
        notifyDataSetChanged()
    }
}