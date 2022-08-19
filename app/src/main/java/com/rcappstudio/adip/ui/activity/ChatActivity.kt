package com.rcappstudio.adip.ui.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.rcappstudio.adip.R
import com.rcappstudio.adip.adapter.ChatAdapter
import com.rcappstudio.adip.adapter.InitChat
import com.rcappstudio.adip.databinding.ActivityChatBinding
import com.rcappstudio.adip.data.model.MessageModel
import com.rcappstudio.adip.data.model.UserModel
import com.rcappstudio.adip.utils.Constants.Companion.DISTRICT
import com.rcappstudio.adip.utils.Constants.Companion.SHARED_PREF_FILE
import com.rcappstudio.adip.utils.Constants.Companion.STATE
import com.rcappstudio.adip.utils.Constants.Companion.SUPPORT_CHAT

import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding : ActivityChatBinding

    private lateinit var userPath : String

    private lateinit var senderUid : String
    private lateinit var chatAdapter : ChatAdapter
    private lateinit var state : String
    private lateinit var district : String
    private lateinit var messagerName : String
    private lateinit var supportDatabaseReference: DatabaseReference


    private lateinit var chatList : MutableList<MessageModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
        binding  = ActivityChatBinding.inflate(layoutInflater)

        val sharedPref = getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE)
        state = sharedPref.getString(STATE, null)!!
        district = sharedPref.getString(DISTRICT, null)!!
        userPath = sharedPref.getString(com.rcappstudio.adip.utils.Constants.USER_PROFILE_PATH, null)!!


        supportDatabaseReference =  FirebaseDatabase.getInstance().getReference("$SUPPORT_CHAT/${FirebaseAuth.getInstance().uid}")
        setContentView(binding.root)
        fetchUserDetails()
        init()
        clickListener()

    }

    private fun fetchUserDetails(){


       FirebaseDatabase.getInstance().getReference(userPath)
            .get().addOnSuccessListener {
                if(it.exists()){
                     val c = it.getValue(UserModel::class.java)!!
                    messagerName = c.name!!
                    checkChatDatabase(c)
                }
            }
    }

    private fun checkChatDatabase(userModel: UserModel){
        supportDatabaseReference
            .get().addOnSuccessListener {
                if(it.exists()){
                   //TODO: display the message
                    readMessage()
                } else{
                    supportDatabaseReference
                        .setValue(InitChat(userModel.profileImageUrl, userModel.udidNo,FirebaseAuth.getInstance().uid,userModel.name))
                }
            }
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

        val messageModel = MessageModel(message, senderUid ,messagerName ,Date().time)

        supportDatabaseReference.child("message").push()
            .setValue(messageModel)

    }

    private fun readMessage() {
        supportDatabaseReference.child("message").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    chatList.clear()
                    for (dataSnapShot in snapshot.children) {
                        val message = dataSnapShot.getValue(MessageModel::class.java)
                        chatList.add(message!!)
                    }
                    chatAdapter.updateList(chatList)
                    binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
                }
                }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menuItem ->{
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:6385342854")
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }



//    companion object {
//        const val TOPIC = "/topics/myTopic2"
//    }
}