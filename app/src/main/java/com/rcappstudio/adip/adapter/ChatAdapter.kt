package com.rcappstudio.adip.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.rcappstudio.adip.R
import com.rcappstudio.adip.data.model.MessageModel

class ChatAdapter(private val context: Context, private var chatList: MutableList<MessageModel>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val MESSAGE_TYPE_LEFT = 0
    private val MESSAGE_TYPE_RIGHT = 1
    var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == MESSAGE_TYPE_RIGHT) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_right, parent, false)
            return ViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_left, parent, false)
            return ViewHolder(view)
        }

    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chatList[position]
        holder.txtUserName.text = chat.message

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val txtUserName: TextView = view.findViewById(R.id.tvMessage)

    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (chatList[position].senderId == firebaseUser!!.uid) {
            return MESSAGE_TYPE_RIGHT
        } else {
            return MESSAGE_TYPE_LEFT
        }
    }

    fun updateList(list : MutableList<MessageModel>){
        this.chatList = list
        notifyDataSetChanged()
    }
}