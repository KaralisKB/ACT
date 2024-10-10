package com.example.act

import android.content.Intent
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
                                // Save the username to SharedPreferences
                                val sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("username", userName)  // Saving the entered username
                                editor.apply()

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

    // Save username to SharedPreferences
    private fun saveUsernameToSharedPreferences(userName: String) {
        val sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", userName)  // Save the username
        editor.apply()
    }
}
