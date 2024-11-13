package com.example.act_mobile

import android.app.Application
import com.google.firebase.FirebaseApp
import com.stripe.android.Stripe

class MyApp : Application() {
    lateinit var stripe: Stripe

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        stripe = Stripe(applicationContext, "pk_test_51QIBRoCd7KzAIIn8OgrburJTZJ7vjGuqYMLCp82O8Eea4DJscWgHI546t2hof0tGxcqP2RnXT8Nvdx6EwWnDnDdY001y8vRZmy")
    }
}

