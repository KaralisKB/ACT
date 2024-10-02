package com.example.act

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase when the application is created
        FirebaseApp.initializeApp(this)
    }
}
