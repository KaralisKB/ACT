package com.example.act.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.act.R
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavigationHandler {

    // Setup for Drawer (Side Navigation)
    fun setupDrawerNavigation(activity: AppCompatActivity, drawerLayout: DrawerLayout) {
        val toggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // Setup for Bottom Navigation
    fun setupBottomNavigation(activity: AppCompatActivity) {
        val bottomNavigationView: BottomNavigationView = activity.findViewById(R.id.bottomNavigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Handle home navigation
                    true
                }
                R.id.nav_portfolio -> {
                    // Handle portfolio navigation
                    true
                }
                R.id.nav_market -> {
                    // Handle market navigation
                    true
                }
                R.id.nav_trade -> {
                    // Handle trade navigation
                    true
                }
                R.id.nav_notifications -> {
                    // Handle notifications navigation
                    true
                }
                else -> false
            }
        }
    }
}
