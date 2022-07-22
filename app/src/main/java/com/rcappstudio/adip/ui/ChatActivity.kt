package com.rcappstudio.adip.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.rcappstudio.adip.adapter.ChatAdapter
import com.rcappstudio.adip.databinding.ActivityChatBinding
import com.rcappstudio.adip.data.model.MessageModel
import com.rcappstudio.adip.notifications.NotificationData
import com.rcappstudio.adip.notifications.PushNotification
import com.rcappstudio.adip.notifications.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var binding : ActivityChatBinding

    private lateinit var senderUid : String
    private lateinit var chatAdapter : ChatAdapter

    private lateinit var receiverUid : String


    private lateinit var  senderRoom : String
    private lateinit var receiverRoom : String

    private lateinit var chatList : MutableList<MessageModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
        binding  = ActivityChatBinding.inflate(layoutInflater)
        database = FirebaseDatabase.getInstance().getReference("message")
        setContentView(binding.root)
        init()
        clickListener()
        readMessage()
    }

    private fun init(){
        senderUid = FirebaseAuth.getInstance().uid.toString()
        chatList = mutableListOf()

        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this )
        binding.chatRecyclerView.setHasFixedSize(true)
        chatAdapter = ChatAdapter(this@ChatActivity , chatList)
        binding.chatRecyclerView.adapter = chatAdapter
    }

    private fun clickListener(){
        binding.sendMessage.setOnClickListener {
            if(binding.messageBox.text.isNullOrEmpty()){
                //Do nothing
            } else {
                sendMessage(binding.messageBox.text.toString())
                binding.messageBox.text = null
            }
        }
    }

    private fun sendMessage(message : String){

        val messageModel = MessageModel(message, senderUid ,Date().time)

        database.push()
            .setValue(messageModel)
        PushNotification(
            NotificationData("New message", messageModel.message.toString()),
            TOPIC
        ).also {
            sendNotification(it)
        }
        readMessage()
    }

    fun readMessage() {
        val firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("message")


        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (dataSnapShot in snapshot.children) {
                    val message = dataSnapShot.getValue(MessageModel::class.java)
                    chatList.add(message!!)
                }
                chatAdapter.updateList(chatList)
            }
        })
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
//                Log.d("TAG", "Response: ${Gson().toJson(response)}")
            } else {
//                Log.e("TAG", response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e("TAG", e.toString())
        }
    }

    companion object {
        const val TOPIC = "/topics/myTopic2"
    }
}