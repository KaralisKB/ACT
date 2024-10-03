//package com.example.act
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.example.act.api.AuthApiService
//import com.example.act.api.AuthResponse
//import com.example.act.api.LoginRequest
//import com.example.act.api.RetrofitClient
//import com.google.firebase.auth.FirebaseAuth
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class LoginActivity : AppCompatActivity() {
//
//    private lateinit var auth: FirebaseAuth
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
//
//        // Initialize Firebase Auth
//        auth = FirebaseAuth.getInstance()
//
//        val emailField: EditText = findViewById(R.id.emailEditText)
//        val passwordField: EditText = findViewById(R.id.passwordEditText)
//        val loginButton: Button = findViewById(R.id.loginButton)
//
//        loginButton.setOnClickListener {
//            val email = emailField.text.toString()
//            val password = passwordField.text.toString()
//
//            if (email.isNotEmpty() && password.isNotEmpty()) {
//                loginUser(email, password)
//            } else {
//                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun loginUser(email: String, password: String) {
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, navigate to authenticated home screen
//                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
//                    val intent = Intent(this@LoginActivity, AuthenticatedHomeActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                } else {
//                    // If sign in fails, display a message to the user.
//                    Toast.makeText(this@LoginActivity, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }
//}

package com.example.act

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.act.api.LoginRequest
import com.example.act.api.RetrofitClient
import com.example.act.api.AuthApiService
import com.example.act.api.AuthResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Replace with your Web Client ID from Firebase
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // UI Elements
        val emailField: EditText = findViewById(R.id.emailEditText)
        val passwordField: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)
        val googleSignInButton: Button = findViewById(R.id.googleSignInButton)

        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, AuthenticatedHomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this@LoginActivity, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign-In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    val intent = Intent(this@LoginActivity, AuthenticatedHomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this@LoginActivity, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
