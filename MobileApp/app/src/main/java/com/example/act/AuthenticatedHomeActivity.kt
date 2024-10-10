package com.example.act

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
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
    private lateinit var sharedPreferences: SharedPreferences  // For retrieving user details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticated_home)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE)

        // Retrieve username and balance from SharedPreferences
        val username = sharedPreferences.getString("username", "User")
        val currentBalance = sharedPreferences.getString("currentBalance", "$$$")

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

        // Set profile image and user name in the navigation drawer header
        profileImageView.setImageResource(R.drawable.ic_profile_pic) // Replace with actual image
        userNameTextView.text = username  // Display the actual username from SharedPreferences

        // Update the home page text with the username and current balance
        val welcomeMessage: TextView = findViewById(R.id.welcomeMessage)
        welcomeMessage.text = "Welcome, $username"

        val currentBalanceTextView: TextView = findViewById(R.id.currentBalanceTextView)
        currentBalanceTextView.text = "Current Balance: $currentBalance"

        val sharesHeaderTextView: TextView = findViewById(R.id.sharesHeaderTextView)
        sharesHeaderTextView.text = "$username's Current Shares"

        // Display empty tables with "No shares yet" message
        populateBoughtShares()
        populateSoldShares()

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

    private fun populateBoughtShares() {
        val boughtSharesTable = findViewById<TableLayout>(R.id.boughtSharesTable)
        boughtSharesTable.removeAllViews() // Clear any existing rows

        // Display a "No shares yet" message for bought shares
        val noSharesRow = TableRow(this)
        val noSharesText = TextView(this)
        noSharesText.text = "You have no bought shares yet."
        noSharesText.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        // Span the message across all columns (5 columns based on your table structure)
        val span = TableRow.LayoutParams()
        span.span = 5 // Assuming 5 columns
        noSharesText.layoutParams = span

        noSharesRow.addView(noSharesText)
        boughtSharesTable.addView(noSharesRow)
    }

    private fun populateSoldShares() {
        val soldSharesTable = findViewById<TableLayout>(R.id.soldSharesTable)
        soldSharesTable.removeAllViews() // Clear any existing rows

        // Display a "No shares yet" message for sold shares
        val noSharesRow = TableRow(this)
        val noSharesText = TextView(this)
        noSharesText.text = "You have no sold shares yet."
        noSharesText.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        // Span the message across all columns (5 columns based on your table structure)
        val span = TableRow.LayoutParams()
        span.span = 5 // Assuming 5 columns
        noSharesText.layoutParams = span

        noSharesRow.addView(noSharesText)
        soldSharesTable.addView(noSharesRow)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle navigation toggle clicks
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
