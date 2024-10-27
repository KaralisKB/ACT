package com.example.act_mobile

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.act_mobile.ui.screens.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // storing the  profile pic URI
        var profileImageUri by mutableStateOf<Uri?>(null)

        // profil pic picker to open the gallery
        val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            profileImageUri = uri  // updates the profile pic URI
        }

        // firebase auth instance
        val auth = FirebaseAuth.getInstance()

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
                    }
                )
                "register" -> RegisterScreen(
                    onRegisterSuccess = { currentScreen = "login" } // after regisitering go to login
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
                    onDeleteAccountClick = { deleteAccount(auth) { currentScreen = "welcome" } }, //  account deletion
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
