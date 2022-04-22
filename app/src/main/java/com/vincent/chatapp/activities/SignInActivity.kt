package com.vincent.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.vincent.chatapp.databinding.ActivitySignInBinding
import com.vincent.chatapp.utilities.Constant
import com.vincent.chatapp.utilities.PreferencesManager

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferencesManager = PreferencesManager(applicationContext)
        if (preferencesManager.getBoolean(Constant.KEY_IS_SIGNED_ID)) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        setListeners()
    }

    private fun setListeners() {
        binding.textCreateNewAccount.setOnClickListener {
            Intent(applicationContext, SignUpActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.buttonSignIn.setOnClickListener {
            if (isValidSignInDetails()) {
                signIn()
            }
        }
    }

    private fun signIn() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constant.KEY_COLLECTION_USERS)
            .whereEqualTo(Constant.KEY_EMAIL, binding.inputEmail.text.toString())
            .whereEqualTo(Constant.KEY_PASSWORD, binding.inputPassword.text.toString())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null && task.result.documents.size > 0) {
                    val snapshot = task.result.documents[0]
                    preferencesManager.putBoolean(Constant.KEY_IS_SIGNED_ID, true)
                    preferencesManager.putString(Constant.KEY_USER_ID, snapshot.id)
                    preferencesManager.putString(Constant.KEY_NAME, snapshot.getString(Constant.KEY_NAME)!!)
                    preferencesManager.putString(Constant.KEY_IMAGE, snapshot.getString(Constant.KEY_IMAGE)!!)
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }else {
                    loading(false)
                    showToast("Unable to sign in")
                }
            }

    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonSignIn.visibility = View.GONE
        }else {
            binding.progressBar.visibility = View.GONE
            binding.buttonSignIn.visibility = View.VISIBLE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidSignInDetails() : Boolean {
        return when {
            binding.inputEmail.text.trim().toString().isEmpty() -> {
                showToast("Enter email")
                false
            }
            binding.inputPassword.text.trim().toString().isEmpty() -> {
                showToast("Enter password")
                false
            }
            else -> {
                true
            }
        }
    }
}