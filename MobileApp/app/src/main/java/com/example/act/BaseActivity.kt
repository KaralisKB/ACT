package com.example.act

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.act.utils.NavigationHandler
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)  // Assumes activity_base.xml is your layout file

        // Set up Drawer Layout and Bottom Navigation
        drawerLayout = findViewById(R.id.drawer_layout)
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        navigationView = findViewById(R.id.navigationView)

        // Setup for Drawer (Side Navigation)
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup Bottom Navigation
        NavigationHandler.setupBottomNavigation(this)

        // Handle side navigation item selection
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> startActivity(Intent(this, AuthenticatedHomeActivity::class.java))
                R.id.nav_portfolio -> startActivity(Intent(this, PortfolioActivity::class.java))
                R.id.nav_market -> startActivity(Intent(this, MarketActivity::class.java))
                R.id.nav_trade -> startActivity(Intent(this, TradeActivity::class.java))
                R.id.nav_notifications -> startActivity(Intent(this, NotificationsActivity::class.java))
            }
            drawerLayout.closeDrawers() // Close drawer after selection
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) true else super.onOptionsItemSelected(item)
    }
}
