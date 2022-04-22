package com.vincent.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import com.vincent.chatapp.adapters.UserAdapter
import com.vincent.chatapp.databinding.ActivityUsersBinding
import com.vincent.chatapp.models.User
import com.vincent.chatapp.utilities.Constant
import com.vincent.chatapp.utilities.PreferencesManager

class UsersActivity : BaseActivity() {

    private lateinit var binding: ActivityUsersBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        preferencesManager = PreferencesManager(applicationContext)
        setContentView(binding.root)
        setListeners()
        getUsers()
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener { onBackPressed() }
    }

    private fun getUsers() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constant.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener { task ->
                loading(false)
                val currentUserId = preferencesManager.getString(Constant.KEY_USER_ID)
                if (task.isSuccessful && task.result != null) {
                    val users = ArrayList<User>()
                    for (snapshot in task.result ) {
                        if (currentUserId.equals(snapshot.id)) {
                            continue
                        }
                        val user = User(
                            snapshot.getString(Constant.KEY_NAME),
                            snapshot.getString(Constant.KEY_IMAGE),
                            snapshot.getString(Constant.KEY_EMAIL),
                            snapshot.getString(Constant.KEY_FCM_TOKEN),
                            snapshot.id
                        )
                        users.add(user)
                    }
                    if (users.size > 0) {
                        val userAdapter = UserAdapter(users)
                        userAdapter.onUserClicked = { user ->
                            val intent = Intent(applicationContext, ChatActivity::class.java)
                            intent.putExtra(Constant.KEY_USER, user)
                            startActivity(intent)
                            finish()
                        }
                        binding.usersRecycleView.adapter = userAdapter
                        binding.usersRecycleView.visibility = View.VISIBLE
                    } else {
                        showErrorMessage()
                    }
                }

            }
    }

    private fun showErrorMessage() {
        binding.textErrorMessage.text = String.format("%s", "No user available")
        binding.textErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        }else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}