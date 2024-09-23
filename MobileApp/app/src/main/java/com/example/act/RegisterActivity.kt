package com.example.act

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val emailEditText: EditText = findViewById(R.id.registerEmailEditText)
        val passwordEditText: EditText = findViewById(R.id.registerPasswordEditText)
        val registerButton: Button = findViewById(R.id.registerButton)

        // Handle the registration process
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                // Redirect back to login after successful registration
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter valid email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
