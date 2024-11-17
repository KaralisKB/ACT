package com.example.act_mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.act_mobile.ui.screens.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.example.act_mobile.ui.network.fetchUserBalance
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        var profileImageUri by mutableStateOf<Uri?>(null)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                handleGoogleSignInResult(account)
            } catch (e: ApiException) {
                if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                    Log.d("MainActivity", "Google Sign-In was canceled by the user.")
                } else {
                    Log.e("MainActivity", "Google Sign-In failed: ${e.statusCode}")
                }
            }
        }

        // profile pic picker launcher
        val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            profileImageUri = uri
        }

        setContent {
            var currentScreen by remember { mutableStateOf(if (auth.currentUser == null) "welcome" else "home") }
            var username by remember { mutableStateOf(auth.currentUser?.displayName ?: auth.currentUser?.email ?: "Your Username") }
            val currentBalance = remember { mutableStateOf(0.0) }

            val userId = auth.currentUser?.uid

            // Fetch balance when userId changes
            LaunchedEffect(userId) {
                if (userId != null) {
                    fetchUserBalance(userId) { balance, error ->
                        if (balance != null) {
                            currentBalance.value = balance
                        } else {
                            Log.e("MainActivity", error ?: "Unknown error fetching balance")
                        }
                    }
                }
            }

            when (currentScreen) {
                "welcome" -> WelcomeScreen(
                    onLoginClick = { currentScreen = "login" },
                    onRegisterClick = { currentScreen = "register" }
                )

                "login" -> LoginScreen(
                    onLoginSuccess = {
                        auth.currentUser?.let { user ->
                            username = user.displayName ?: user.email ?: "Your Username"
                        }
                        currentScreen = "home"
                    },
                    onGoogleSignInClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        signInLauncher.launch(signInIntent)
                    },
                    onRegisterClick = { currentScreen = "register" }
                )

                "register" -> RegisterScreen(
                    onRegisterSuccess = { currentScreen = "login" },
                    onLoginClick = { currentScreen = "login" }
                )

                "home" -> {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        HomeWithDrawer(
                            userId = userId,
                            profileImageUri = profileImageUri,
                            currentBalance = currentBalance,
                            imagePickerLauncher = { imagePickerLauncher.launch("image/*") },
                            onLogoutClick = {
                                auth.signOut()
                                currentScreen = "welcome"
                            },
                            onSupportClick = { currentScreen = "support" },
                            onFeedbackClick = { currentScreen = "feedback" },
                            onNewsClick = { currentScreen = "news" },
                            onSettingsClick = { currentScreen = "settings" },
                            onAddFundsClick = { amount -> handleAddFunds(amount) }
                        )
                    }
                }

                "support" -> SupportHelpScreen(onBackClick = { currentScreen = "home" })
                "feedback" -> FeedbackReviewsScreen(onBackClick = { currentScreen = "home" })
                "news" -> NewsScreen(onBackClick = { currentScreen = "home" })
                "settings" -> SettingsScreen(
                    onBackClick = { currentScreen = "home" },
                    onEditUsernameClick = { currentScreen = "editUsername" },
                    onChangeEmailClick = { currentScreen = "changeEmail" },
                    onChangePasswordClick = { /* Change Password */ },
                    onDeleteAccountClick = { email, password ->
                        deleteAccount(auth, email, password, onSuccess = { currentScreen = "welcome" }, onError = { it.printStackTrace() })
                    },
                    onNotificationsClick = { /* Notifications */ },
                    onPrivacyPreferencesClick = { /* Privacy preferences */ },
                    onAppearanceClick = { /* Appearance */ }
                )

                "editUsername" -> ChangeUsernameScreen(
                    currentUsername = username,
                    onUsernameChange = { newUsername -> username = newUsername },
                    onBackClick = { currentScreen = "settings" },
                    onSaveClick = { newUsername ->
                        updateUsername(auth.currentUser?.uid ?: "", newUsername) {
                            currentScreen = "settings"
                        }
                    }
                )

                "changeEmail" -> ChangeEmailScreen(
                    currentEmail = auth.currentUser?.email ?: "N/A",
                    onBackClick = { currentScreen = "settings" },
                    onSaveClick = { newEmail ->
                        updateEmail(auth, newEmail) {
                            currentScreen = "settings"
                        }
                    }
                )
            }
        }
    }


    private fun handleAddFunds(amount: Int) {
        val context = this
        val request = JsonObjectRequest(
            Request.Method.POST,
            "https://act-production-5e24.up.railway.app/create-order",
            JSONObject().put("amount", amount),
            { response ->
                val approvalUrl = response.optString("approval_url", null)
                if (approvalUrl != null) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(approvalUrl))
                    context.startActivity(intent)
                } else {
                    Log.e("MainActivity", "Approval URL is missing in the response")
                }
            },
            { error ->
                Log.e("MainActivity", "Error creating PayPal order: ${error.message}")
            }
        )

        Volley.newRequestQueue(context).add(request)
    }

    private fun handleGoogleSignInResult(account: GoogleSignInAccount?) {
        account?.let {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "Google Sign In successful")
                } else {
                    task.exception?.printStackTrace()
                }
            }
        } ?: Log.e("MainActivity", "Google Sign-In failed: Account is null")
    }
}


@Composable
fun HomeWithDrawer(
    userId: String,
    profileImageUri: Uri?,
    currentBalance: MutableState<Double>,
    imagePickerLauncher: () -> Unit,
    onLogoutClick: () -> Unit,
    onSupportClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onNewsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddFundsClick: (Int) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("Loading...") }
    var userBalance by remember { mutableStateOf(currentBalance) }

    // Firebase Firestore instance
    val firestore = FirebaseFirestore.getInstance()

    //  username and balance from Firestore
    LaunchedEffect(userId) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    username = document.getString("username") ?: "Unknown User"
                    currentBalance.value = document.getDouble("balance") ?: 0.0
                }
            }
            .addOnFailureListener { e ->
                username = "Error loading username"
                Log.e("HomeWithDrawer", "Error fetching user data: ", e)
            }
    }
    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerScreen(
                username = username,
                profileImageUri = profileImageUri,
                onImageClick = { imagePickerLauncher() },
                currentBalance = currentBalance, // Pass as MutableState<Double>
                onLogoutClick = onLogoutClick,
                onSupportClick = onSupportClick,
                onFeedbackClick = onFeedbackClick,
                onNewsClick = onNewsClick,
                onSettingsClick = onSettingsClick,
                onAddFundsClick = onAddFundsClick
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ACT") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isOpen) drawerState.close()
                                else drawerState.open()
                            }
                        }) {
                            Icon(painterResource(id = R.drawable.ic_menu), contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            MainAppScreen(
                modifier = Modifier.padding(paddingValues),
                currentBalance = userBalance.toString(),
                username = username,
            )
        }
    }
}

