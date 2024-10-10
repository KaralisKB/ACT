package com.example.act

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE)

        val emailField: EditText = findViewById(R.id.emailEditText)
        val passwordField: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Fetch the user's name from Firestore
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (document != null) {
                                    val userName = document.getString("userName")

                                    // Save username to SharedPreferences
                                    val editor = sharedPreferences.edit()
                                    editor.putString("username", userName)
                                    editor.apply()

                                    // Navigate to AuthenticatedHomeActivity
                                    val intent = Intent(
                                        this@LoginActivity,
                                        AuthenticatedHomeActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Log.e("LoginActivity", "No such user exists.")
                                    showToast("No such user exists.")  // Only show important toast messages
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("LoginActivity", "Error fetching user data: ", exception)
                                showToast("Error: ${exception.message}")
                            }
                    }
                } else {
                    Log.e("LoginActivity", "Authentication failed: ${task.exception?.message}")
                    showToast("Authentication failed: ${task.exception?.message}")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
