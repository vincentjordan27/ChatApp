package com.vincent.chatapp.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.FirebaseFirestore
import com.vincent.chatapp.databinding.ActivitySignUpBinding
import com.vincent.chatapp.utilities.Constant
import com.vincent.chatapp.utilities.PreferencesManager
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var preferencesManager: PreferencesManager
    private var encodedImage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferencesManager = PreferencesManager(applicationContext)
        setListeners()
    }

    private fun setListeners() {
        binding.textSignIn.setOnClickListener { onBackPressed() }
        binding.buttonSignUp.setOnClickListener {
            if (isValidSignUpDetails()) {
                signUp()
            }
        }
        binding.layoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun signUp() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val user = HashMap<String, Any>()
        user[Constant.KEY_NAME] = binding.inputName.text.toString()
        user[Constant.KEY_EMAIL] = binding.inputEmail.text.toString()
        user[Constant.KEY_PASSWORD] = binding.inputPassword.text.toString()
        user[Constant.KEY_IMAGE] = encodedImage
        database.collection(Constant.KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener { documentReference ->
                loading(false)
                preferencesManager.putBoolean(Constant.KEY_IS_SIGNED_ID, true)
                preferencesManager.putString(Constant.KEY_USER_ID, documentReference.id)
                preferencesManager.putString(Constant.KEY_NAME, binding.inputName.text.toString())
                preferencesManager.putString(Constant.KEY_IMAGE, encodedImage)
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            .addOnFailureListener{ exception ->
                loading(false)
                showToast(exception.message.toString())
            }

    }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            if (it.data != null) {
                val imageUri : Uri? = it.data!!.data
                try {
                    val inputStream = imageUri?.let { it1 -> contentResolver.openInputStream(it1) }
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imageProfile.setImageBitmap(bitmap)
                    binding.textAddImage.visibility = View.GONE
                    encodedImage = encodeImage(bitmap)
                }catch (e : FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun encodeImage(bitmap: Bitmap) : String {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun isValidSignUpDetails() : Boolean {
        when {
            encodedImage == "" -> {
                showToast("Select profile image")
                return false
            }
            binding.inputName.text.toString().trim().isEmpty() -> {
                showToast("Enter name")
                return false
            }
            binding.inputEmail.text.toString().trim().isEmpty() -> {
                showToast("Enter email")
                return false
            }
            binding.inputEmail.text.toString().trim().isEmpty() -> {
                showToast("Enter email")
                return false
            }
            binding.inputPassword.text.toString().trim().isEmpty() -> {
                showToast("Enter password")
                return false
            }
            binding.inputConfirmPassword.text.toString().trim().isEmpty() -> {
                showToast("Confirm your password")
                return false
            }
            binding.inputConfirmPassword.text.toString() != binding.inputConfirmPassword.text.toString() -> {
                showToast("Password & confirm password must same")
                return false
            }
            else -> return true
        }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonSignUp.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.buttonSignUp.visibility = View.VISIBLE
        }
    }
}