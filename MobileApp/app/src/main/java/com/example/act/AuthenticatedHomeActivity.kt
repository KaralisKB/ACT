package com.example.act

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AuthenticatedHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticated_home)
        // This is where authenticated users will land after logging in successfully
    }
}
