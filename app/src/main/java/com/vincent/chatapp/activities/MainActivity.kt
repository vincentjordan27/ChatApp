package com.vincent.chatapp.activities

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import com.vincent.chatapp.R
import com.vincent.chatapp.adapters.RecentConversationAdapter
import com.vincent.chatapp.databinding.ActivityMainBinding
import com.vincent.chatapp.models.ChatMessage
import com.vincent.chatapp.utilities.Constant
import com.vincent.chatapp.utilities.PreferencesManager

class MainActivity : BaseActivity(), EventListener<QuerySnapshot> {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var conversations: ArrayList<ChatMessage>
    private lateinit var adapter: RecentConversationAdapter
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferencesManager = PreferencesManager(applicationContext)
        init()
        loadUserDetails()
        getToken()
        setListeners()
        listenConversation()
    }

    private fun init() {
        conversations = ArrayList()
        adapter = RecentConversationAdapter(conversations)
        adapter.onConversionClicked = {
            val intent = Intent(applicationContext, ChatActivity::class.java)
            intent.putExtra(Constant.KEY_USER, it)
            startActivity(intent)
        }
        binding.conversationsRecycleView.adapter = adapter
        database = FirebaseFirestore.getInstance()
    }

    private fun setListeners() {
        binding.imageSignOut.setOnClickListener {
            signOut()
        }
        binding.fabNewChat.setOnClickListener {
            startActivity(Intent(applicationContext, UsersActivity::class.java))
        }
    }

    private fun loadUserDetails() {
        binding.textName.text = preferencesManager.getString(Constant.KEY_NAME)
        val bytes = Base64.decode(preferencesManager.getString(Constant.KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imageProfile.setImageBitmap(bitmap)
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            updateToken(it)
        }
    }

    private fun updateToken(token: String) {
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constant.KEY_COLLECTION_USERS).document(
            preferencesManager.getString(Constant.KEY_USER_ID)!!
        )
        documentReference.update(Constant.KEY_FCM_TOKEN, token)
            .addOnFailureListener { showToast("Unable to update token") }
    }

    private fun signOut() {
        showToast("Signing out..")
        val database = FirebaseFirestore.getInstance()
        val documentReference =
            database.collection(Constant.KEY_COLLECTION_USERS).document(
                preferencesManager.getString(Constant.KEY_USER_ID)!!
            )
        val updates = HashMap<String, Any>()
        updates[Constant.KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(updates)
            .addOnSuccessListener {
                preferencesManager.clear()
                startActivity(Intent(applicationContext, SignInActivity::class.java))
            }
            .addOnFailureListener { showToast("Unable to sign out") }

    }

    private fun listenConversation() {
        database.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constant.KEY_SENDER_ID, preferencesManager.getString(Constant.KEY_USER_ID))
            .addSnapshotListener(this)
        database.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constant.KEY_RECEIVER_ID, preferencesManager.getString(Constant.KEY_USER_ID))
            .addSnapshotListener(this)
    }

    override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
        if (error != null) {
            return
        }
        if (value != null) {
            for (documentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    val senderId = documentChange.document.getString(Constant.KEY_SENDER_ID)
                    val receiverId = documentChange.document.getString(Constant.KEY_RECEIVER_ID)
                    if (preferencesManager.getString(Constant.KEY_USER_ID).equals(senderId)) {
                        val chatMessage = ChatMessage(
                            senderId!!,
                            receiverId!!,
                            documentChange.document.getString(Constant.KEY_LAST_MESSAGE)!!,
                            dateTime = "",
                            documentChange.document.getLong(Constant.KEY_TIME_SEND)!!,
                            documentChange.document.getString(Constant.KEY_RECEIVER_ID)!!,
                            documentChange.document.getString(Constant.KEY_RECEIVER_NAME)!!,
                            documentChange.document.getString(Constant.KEY_RECEIVER_IMAGE)!!
                        )
                        conversations.add(chatMessage)
                    }
                    else if (preferencesManager.getString(Constant.KEY_USER_ID).equals(receiverId)) {
                        val chatMessage = ChatMessage(
                            senderId!!,
                            receiverId!!,
                            documentChange.document.getString(Constant.KEY_LAST_MESSAGE)!!,
                            dateTime = "",
                            documentChange.document.getLong(Constant.KEY_TIME_SEND)!!,
                            documentChange.document.getString(Constant.KEY_SENDER_ID)!!,
                            documentChange.document.getString(Constant.KEY_SENDER_NAME)!!,
                            documentChange.document.getString(Constant.KEY_SENDER_IMAGE)!!
                        )
                        conversations.add(chatMessage)
                    }
                }else if (documentChange.type == DocumentChange.Type.MODIFIED) {
                    val senderId = documentChange.document.getString(Constant.KEY_SENDER_ID)
                    val receiverId = documentChange.document.getString(Constant.KEY_RECEIVER_ID)
                    for (conversation in conversations) {
                        if (conversation.senderId == senderId && conversation.receiverId == receiverId) {
                            conversation.message = documentChange.document.getString(Constant.KEY_LAST_MESSAGE)!!
                            conversation.dateObject = documentChange.document.getLong(Constant.KEY_TIME_SEND)!!
                            break
                        }
                    }
                }
            }

            conversations.sortWith(compareByDescending { it.dateObject })
            adapter.notifyDataSetChanged()
            binding.conversationsRecycleView.smoothScrollToPosition(0)
            binding.conversationsRecycleView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
}