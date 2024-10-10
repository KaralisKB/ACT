package com.example.act

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.act.utils.NavigationHandler
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class AuthenticatedHomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle  // For handling the hamburger icon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticated_home)

        // Initialize drawer layout and navigation
        drawerLayout = findViewById(R.id.drawer_layout)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        val navigationView: NavigationView = findViewById(R.id.navigationView)

        // Set up bottom and drawer navigation using the handler
        NavigationHandler.setupBottomNavigation(this)
        NavigationHandler.setupDrawerNavigation(this, drawerLayout)

        // Get header views from navigation
        val headerView = navigationView.getHeaderView(0)
        val profileImageView: ImageView = headerView.findViewById(R.id.profileImageView)
        val userNameTextView: TextView = headerView.findViewById(R.id.userNameTextView)

        // Set profile image and user name (replace with actual data)
        profileImageView.setImageResource(R.drawable.ic_profile_pic) // Replace with actual image
        userNameTextView.text = "Your User Name" // Replace with actual user name

        // Initialize the ActionBarDrawerToggle for the hamburger icon
        toggle = ActionBarDrawerToggle(
            this, drawerLayout,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Enable the hamburger icon
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
