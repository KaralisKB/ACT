package com.example.act_mobile

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
import com.example.act_mobile.ui.screens.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // firebase auth instance
        val auth = FirebaseAuth.getInstance()

        // storing the  profile pic URI
        var profileImageUri by mutableStateOf<Uri?>(null)


        //  google gign in
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

        // profil pic picker to open the gallery
        val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            profileImageUri = uri  // updates the profile pic URI
        }

        setContent {
            var currentScreen by remember { mutableStateOf(if (auth.currentUser == null) "welcome" else "home") }

            // hold the username
            var username by remember { mutableStateOf(auth.currentUser?.displayName ?: auth.currentUser?.email ?: "Your Username") }

            // display what ever based on screen state
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
                    onRegisterClick = {
                        currentScreen = "register"
                    }
                )
                "register" -> RegisterScreen(
                    onRegisterSuccess = { currentScreen = "login" }, // after registering go to log in
                    onLoginClick = { currentScreen = "login" }
                )
                "home" -> {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        HomeWithDrawer(
                            userId = userId,
                            profileImageUri = profileImageUri,
                            imagePickerLauncher = { imagePickerLauncher.launch("image/*") },
                            onLogoutClick = {
                                auth.signOut()  // log out from Firebase
                                currentScreen = "welcome"
                            },
                            onSupportClick = { currentScreen = "support" }, // support screen
                            onFeedbackClick = { currentScreen = "feedback" }, // feedback screen
                            onNewsClick = { currentScreen = "news" },
                            onSettingsClick = { currentScreen = "settings" }, // settings screen
                            onAddFundsClick = { /*TO DO */ }
                        )
                    }
                }
                "support" -> SupportHelpScreen(
                    onBackClick = { currentScreen = "home" }  // back to home screen when back button pressed
                )
                "feedback" -> FeedbackReviewsScreen(
                    onBackClick = { currentScreen = "home" }
                )
                "news" -> NewsScreen(
                    onBackClick = { currentScreen = "home" }
                )
                "settings" -> SettingsScreen(
                    onBackClick = { currentScreen = "home" },
                    onEditUsernameClick = { currentScreen = "editUsername" }, // edit Username
                    onChangeEmailClick = { currentScreen = "changeEmail" },  // change Email
                    onChangePasswordClick = { /* change Password */ },
                    onDeleteAccountClick = { email, password ->
                        deleteAccount(
                            auth = auth,
                            email = email,
                            password = password,
                            onSuccess = {
                                currentScreen = "welcome"
                            },
                            onError = { exception ->
                                exception.printStackTrace()
                            }
                        )
                    },
                    onNotificationsClick = { /* notifications */ },
                    onPrivacyPreferencesClick = { /* privacy preference*/ },
                    onAppearanceClick = { /* light/dark */ }
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

    private fun handleGoogleSignInResult(account: GoogleSignInAccount?) {
        if (account == null) {
            Log.e("MainActivity", "Google Sign-In failed: Account is null")
            return
        }

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "Google Sign In successful")
                } else {
                    task.exception?.printStackTrace()
                }
            }
    }
}

@Composable
fun HomeWithDrawer(
    userId: String,
    profileImageUri: Uri?,
    imagePickerLauncher: () -> Unit,
    onLogoutClick: () -> Unit,
    onSupportClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onNewsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddFundsClick: () -> Unit

) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("Loading...") }
    var currentBalance by remember { mutableStateOf("1000") } // example


    // Firebase Firestore instance
    val firestore = FirebaseFirestore.getInstance()

    // getting username from Firestore
    LaunchedEffect(userId) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    username = document.getString("username") ?: "Unknown User"
                }
            }
            .addOnFailureListener { e ->
                username = "Error loading username"
            }
    }
// on click navifation
    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerScreen(
                username = username,
                profileImageUri = profileImageUri,
                onImageClick = { imagePickerLauncher() },
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
                username = username,
                currentBalance = currentBalance,
                onAddFundsClick = onAddFundsClick
            )
        }
    }
}
