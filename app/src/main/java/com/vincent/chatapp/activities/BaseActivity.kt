package com.vincent.chatapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.vincent.chatapp.utilities.Constant
import com.vincent.chatapp.utilities.PreferencesManager

open class BaseActivity : AppCompatActivity() {

    private lateinit var documentReference: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferencesManager = PreferencesManager(applicationContext)
        val database = FirebaseFirestore.getInstance()
        documentReference = database.collection(Constant.KEY_COLLECTION_USERS)
            .document(preferencesManager.getString(Constant.KEY_USER_ID)!!)
    }

    override fun onPause() {
        super.onPause()
        documentReference.update(Constant.KEY_AVAILABILITY, 0)
    }

    override fun onResume() {
        super.onResume()
        documentReference.update(Constant.KEY_AVAILABILITY, 1)
    }
}