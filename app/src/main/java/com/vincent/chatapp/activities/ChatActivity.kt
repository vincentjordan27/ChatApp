package com.vincent.chatapp.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.vincent.chatapp.adapters.ChatAdapter
import com.vincent.chatapp.databinding.ActivityChatBinding
import com.vincent.chatapp.models.ChatMessage
import com.vincent.chatapp.models.User
import com.vincent.chatapp.utilities.Constant
import com.vincent.chatapp.utilities.PreferencesManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : BaseActivity(), OnCompleteListener<QuerySnapshot> {

    private lateinit var binding: ActivityChatBinding
    private lateinit var user: User
    private lateinit var chatMessages: ArrayList<ChatMessage>
    private lateinit var adapter: ChatAdapter
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var database: FirebaseFirestore
    private var conversionId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
        init()
        listenMessage()
    }

    private fun init() {
        preferencesManager = PreferencesManager(applicationContext)
        chatMessages = ArrayList()
        adapter = ChatAdapter(
            getBitmapFromDecodeString(user.image!!),
            chatMessages,
            preferencesManager.getString(Constant.KEY_USER_ID)!!
        )
        binding.chatRecycleView.adapter = adapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage() {
        val message = HashMap<String, Any>()
        message[Constant.KEY_SENDER_ID] = preferencesManager.getString(Constant.KEY_USER_ID)!!
        message[Constant.KEY_RECEIVER_ID] = user.id!!
        message[Constant.KEY_MESSAGE] = binding.inputMessage.text.toString()
        message[Constant.KEY_TIME_SEND] = Calendar.getInstance().timeInMillis
        database.collection(Constant.KEY_COLLECTION_CHAT).add(message)
        if (conversionId != "") {
            updateConversion(binding.inputMessage.text.toString())
        }else {
            val conversion = HashMap<String, Any>()
            conversion[Constant.KEY_SENDER_ID] = preferencesManager.getString(Constant.KEY_USER_ID)!!
            conversion[Constant.KEY_SENDER_NAME] = preferencesManager.getString(Constant.KEY_NAME)!!
            conversion[Constant.KEY_SENDER_IMAGE] = preferencesManager.getString(Constant.KEY_IMAGE)!!
            conversion[Constant.KEY_RECEIVER_ID] = user.id!!
            conversion[Constant.KEY_RECEIVER_NAME] = user.name!!
            conversion[Constant.KEY_RECEIVER_IMAGE] = user.image!!
            conversion[Constant.KEY_LAST_MESSAGE] = binding.inputMessage.text.toString()
            conversion[Constant.KEY_TIME_SEND] = Calendar.getInstance().timeInMillis
            addConversion(conversion)
        }
        binding.inputMessage.text = null
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun listenMessage() {
        database.collection(Constant.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constant.KEY_SENDER_ID, preferencesManager.getString(Constant.KEY_USER_ID)!!)
            .whereEqualTo(Constant.KEY_RECEIVER_ID, user.id)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val count = chatMessages.size
                    for (documentChange in value.documentChanges) {
                        if (documentChange.type == DocumentChange.Type.ADDED) {
                            val chatMessage = ChatMessage(
                                documentChange.document.getString(Constant.KEY_SENDER_ID)!!,
                                documentChange.document.getString(Constant.KEY_RECEIVER_ID)!!,
                                documentChange.document.getString(Constant.KEY_MESSAGE)!!,
                                getReadableDateTime(documentChange.document.getLong(Constant.KEY_TIME_SEND)!!),
                                documentChange.document.getLong(Constant.KEY_TIME_SEND)!!
                            )
                            chatMessages.add(chatMessage)
                        }
                    }
                    if (count == 0) {
                        adapter.notifyDataSetChanged()
                    } else {
                        chatMessages.sortWith(compareBy { it.dateObject })
                        adapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
                        binding.chatRecycleView.smoothScrollToPosition(chatMessages.size - 1)
                    }
                    binding.chatRecycleView.visibility = View.VISIBLE
                }
                binding.progressBar.visibility = View.GONE
                if (conversionId == "") {
                    checkForConversion()
                }
            }
        database.collection(Constant.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constant.KEY_SENDER_ID, user.id)
            .whereEqualTo(Constant.KEY_RECEIVER_ID, preferencesManager.getString(Constant.KEY_USER_ID)!!)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val count = chatMessages.size
                    for (documentChange in value.documentChanges) {
                        if (documentChange.type == DocumentChange.Type.ADDED) {
                            val chatMessage = ChatMessage(
                                documentChange.document.getString(Constant.KEY_SENDER_ID)!!,
                                documentChange.document.getString(Constant.KEY_RECEIVER_ID)!!,
                                documentChange.document.getString(Constant.KEY_MESSAGE)!!,
                                getReadableDateTime(documentChange.document.getLong(Constant.KEY_TIME_SEND)!!),
                                documentChange.document.getLong(Constant.KEY_TIME_SEND)!!
                            )
                            chatMessages.add(chatMessage)
                        }
                    }
                    if (count == 0) {
                        adapter.notifyDataSetChanged()
                    } else {
                        chatMessages.sortWith(compareBy { it.dateObject })
                        adapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
                        binding.chatRecycleView.smoothScrollToPosition(chatMessages.size - 1)
                    }
                    binding.chatRecycleView.visibility = View.VISIBLE
                }
                binding.progressBar.visibility = View.GONE
                if (conversionId == "") {
                    checkForConversion()
                }
            }
    }

    private fun getBitmapFromDecodeString(decodeImage: String) : Bitmap {
        val bytes = Base64.decode(decodeImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun loadReceiverDetails() {
        if (intent != null) {
            user = intent.getParcelableExtra<User>(Constant.KEY_USER)!!
            binding.textName.text = user.name
        }
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener { onBackPressed() }
        binding.layoutSend.setOnClickListener { sendMessage() }
    }

    private fun getReadableDateTime(timemillis: Long) : String {
        val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm:ss a")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timemillis
        Log.d("DEBUGS", formatter.format(calendar.time))
        return formatter.format(calendar.time)
    }

    private fun addConversion(conversion : HashMap<String, Any>) {
        database.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
            .add(conversion)
            .addOnSuccessListener { conversionId = it.id }
    }

    private fun updateConversion(message: String) {
        val documentReference = database.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
            .document(conversionId)
        documentReference.update(
            Constant.KEY_LAST_MESSAGE, message,
            Constant.KEY_TIME_SEND, Calendar.getInstance().timeInMillis
        )
    }

    private fun checkForConversion() {
        if (chatMessages.size != 0) {
            checkConversionRemotely(
                preferencesManager.getString(Constant.KEY_USER_ID)!!,
                user.id!!
            )
            checkConversionRemotely(
                user.id!!,
                preferencesManager.getString(Constant.KEY_USER_ID)!!
            )
        }
    }

    private fun checkConversionRemotely(senderId: String, receiverId: String) {
        database.collection(Constant.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constant.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constant.KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener(this)
    }

    override fun onComplete(task: Task<QuerySnapshot>) {
        if (task.isSuccessful && task.result != null && task.result.documents.size > 0) {
            val documentSnapshot = task.result.documents[0]
            conversionId = documentSnapshot.id
        }
    }
}