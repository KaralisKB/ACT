//package com.example.act
//
//import android.os.Bundle
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.example.act.api.AuthApiService
//import com.example.act.api.AuthResponse
//import com.example.act.api.RegisterRequest
//import com.example.act.api.RetrofitClient
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class RegisterActivity : AppCompatActivity() {
//
//    private lateinit var apiService: AuthApiService
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_register)
//
//        // Initialize Retrofit and API service
//        apiService = RetrofitClient.instance.create(AuthApiService::class.java)
//
//        // Use the correct IDs from the XML file
//        val emailField: EditText = findViewById(R.id.registerEmailEditText)
//        val passwordField: EditText = findViewById(R.id.registerPasswordEditText)
//        val registerButton: Button = findViewById(R.id.registerButton)
//
//        registerButton.setOnClickListener {
//            val email = emailField.text.toString()
//            val password = passwordField.text.toString()
//
//            if (email.isNotEmpty() && password.isNotEmpty()) {
//                registerUser(email, password)
//            } else {
//                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun registerUser(email: String, password: String) {
//        val request = RegisterRequest(email, password)
//
//        apiService.registerUser(request).enqueue(object : Callback<AuthResponse> {
//            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
//                if (response.isSuccessful) {
//                    val authResponse = response.body()
//                    if (authResponse != null && authResponse.success) {
//                        Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
//                        finish() // Go back to the login screen
//                    } else {
//                        val errorMessage = authResponse?.message ?: "Unknown error occurred"
//                        Toast.makeText(this@RegisterActivity, "Registration failed: $errorMessage", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this@RegisterActivity, "Registration failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//
//            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
//                Toast.makeText(this@RegisterActivity, "Registration failed: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//}

package com.example.act

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Authentication and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Use the correct IDs from the XML file
        val emailField: EditText = findViewById(R.id.registerEmailEditText)
        val passwordField: EditText = findViewById(R.id.registerPasswordEditText)
        val userNameField: EditText = findViewById(R.id.registerUserNameEditText) // Assuming there's a username field
        val registerButton: Button = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val userName = userNameField.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && userName.isNotEmpty()) {
                registerUser(email, password, userName)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String, userName: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Registration successful
                    val userId = auth.currentUser?.uid

                    val user = hashMapOf(
                        "userName" to userName,
                        "email" to email
                    )

                    // Add user data to Firestore
                    if (userId != null) {
                        db.collection("users").document(userId).set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                                finish() // Go back to the login screen
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save user data: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Registration failed
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

