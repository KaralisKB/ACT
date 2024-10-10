// NavigationHandler remains the same as you posted:
package com.example.act.utils

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.act.AuthenticatedHomeActivity
import com.example.act.MarketActivity
import com.example.act.NotificationsActivity
import com.example.act.PortfolioActivity
import com.example.act.R
import com.example.act.TradeActivity
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
                    val intent = Intent(activity, AuthenticatedHomeActivity::class.java)
                    activity.startActivity(intent)
                    true
                }
                R.id.nav_portfolio -> {
                    val intent = Intent(activity, PortfolioActivity::class.java)
                    activity.startActivity(intent)
                    true
                }
                R.id.nav_market -> {
                    val intent = Intent(activity, MarketActivity::class.java)
                    activity.startActivity(intent)
                    true
                }
                R.id.nav_trade -> {
                    val intent = Intent(activity, TradeActivity::class.java)
                    activity.startActivity(intent)
                    true
                }
                R.id.nav_notifications -> {
                    val intent = Intent(activity, NotificationsActivity::class.java)
                    activity.startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
