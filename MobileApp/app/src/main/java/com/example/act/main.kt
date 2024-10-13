package com.example.act

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class Main : AppCompatActivity() {

    private lateinit var contentTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main) // Your main layout with BottomNavigationView


        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set default content when activity is created
        updateContent("Welcome to the Home Screen")

        // Set up item selected listener for BottomNavigationView
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    updateContent("Welcome to the Home Screen!")
                    Log.i("Main", "Home Selected")
                    true
                }
                R.id.nav_portfolio -> {
                    updateContent("Here is your Portfolio")
                    Log.i("Main", "Portfolio Selected")
                    true
                }
                R.id.nav_market -> {
                    updateContent("Browse the Market")
                    Log.i("Main", "Market Selected")
                    true
                }
                R.id.nav_trade -> {
                    updateContent("Start Trading")
                    Log.i("Main", "Trade Selected")
                    true
                }
                R.id.nav_notifications -> {
                    updateContent("Notifications Center")
                    Log.i("Main", "Notifications Selected")
                    true
                }
                else -> false
            }
        }
    }

    private fun updateContent(text: String) {
        // Update the content view dynamically
        contentTextView.text = text
    }
}
