package com.example.act_mobile.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.act_mobile.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject

@Composable
fun DrawerScreen(
    username: String,
    profileImageUri: Uri?,
    currentBalance: MutableState<Double>,
    onImageClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSupportClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onNewsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddFundsClick: (Int) -> Unit
) {
    var showAddFundsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val firestore = FirebaseFirestore.getInstance()
    val stripePaymentLink = "https://buy.stripe.com/test_9AQ02c8Bd8SJ0tG4gg"


    // Fetch user balance from Firestore
    LaunchedEffect(Unit) {
        userId?.let {
            firestore.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.contains("balance")) {
                        currentBalance.value = document.getDouble("balance") ?: 0.0
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("DrawerScreen", "Error fetching balance: ", exception)
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .size(100.dp)
                .clickable { onImageClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = if (profileImageUri != null) {
                    rememberAsyncImagePainter(profileImageUri)
                } else {
                    painterResource(id = R.drawable.ic_profile_plsceholder)
                },
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = username, style = MaterialTheme.typography.titleLarge)
        Text(text = "Available Funds: $${"%.2f".format(currentBalance.value)}")

        Button(
            onClick = {
                openStripePaymentLink(context, stripePaymentLink)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            modifier = Modifier
                .width(150.dp)
                .height(40.dp)
        ) {
            Text(text = "+ Add Funds", fontSize = 14.sp)
        }


        if (showAddFundsDialog) {
            AddFundsDialog(
                onDismiss = { showAddFundsDialog = false },
                onNext = { amount ->
                    showAddFundsDialog = false
                    onAddFundsClick(amount) // Pass the amount to MainActivity to open PayPal screen
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        DrawerMenuItem(text = "News", onClick = onNewsClick)
        DrawerMenuItem(text = "Support & Help", onClick = onSupportClick)
        DrawerMenuItem(text = "Feedback & Reviews", onClick = onFeedbackClick)
        DrawerMenuItem(text = "Settings", onClick = onSettingsClick)
        DrawerMenuItem(text = "Log Out", onClick = onLogoutClick)
    }
}

fun openStripePaymentLink(context: Context, paymentLink: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentLink))
    context.startActivity(intent)
}


@Composable
fun AddFundsDialog(
    onDismiss: () -> Unit,
    onNext: (Int) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Funds") },
        text = {
            Column {
                Text(text = "How much would you like to add?")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountInt = amount.toIntOrNull()
                    if (amountInt != null && amountInt > 0) {
                        onNext(amountInt) // Trigger PayPal order creation
                    }
                }
            ) {
                Text("Next")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DrawerMenuItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text)
    }
}
//

//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import coil.compose.rememberAsyncImagePainter
//import com.example.act_mobile.R
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import android.util.Log
//import com.android.volley.Request
//import com.android.volley.toolbox.JsonObjectRequest
//import com.android.volley.toolbox.Volley
//import org.json.JSONObject
//
//@Composable
//fun DrawerScreen(
//    username: String,
//    profileImageUri: Uri?,
//    currentBalance: MutableState<Double>, // Change to MutableState<Double>
//    onImageClick: () -> Unit,
//    onLogoutClick: () -> Unit,
//    onSupportClick: () -> Unit,
//    onFeedbackClick: () -> Unit,
//    onNewsClick: () -> Unit,
//    onSettingsClick: () -> Unit,
//    onAddFundsClick: (Int) -> Unit // Remove @Composable here
//) {
//    var showAddFundsDialog by remember { mutableStateOf(false) }
//    val context = LocalContext.current
//    val userId = FirebaseAuth.getInstance().currentUser?.uid
//    val firestore = FirebaseFirestore.getInstance()
//
//    // Fetch user balance from Firestore
//    LaunchedEffect(Unit) {
//        userId?.let {
//            firestore.collection("users").document(it).get()
//                .addOnSuccessListener { document ->
//                    if (document != null && document.contains("balance")) {
//                        currentBalance.value = document.getDouble("balance") ?: 0.0 // Update MutableState
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.e("DrawerScreen", "Error fetching balance: ", exception)
//                }
//        }
//    }
//
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // Profile and balance UI
//        Spacer(modifier = Modifier.height(20.dp))
//        Box(
//            modifier = Modifier
//                .size(100.dp)
//                .clickable { onImageClick() },
//            contentAlignment = Alignment.Center
//        ) {
//            Image(
//                painter = if (profileImageUri != null) {
//                    rememberAsyncImagePainter(profileImageUri)
//                } else {
//                    painterResource(id = R.drawable.placeholder_image)
//                },
//                contentDescription = "Profile Image",
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.size(100.dp)
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(text = username, style = MaterialTheme.typography.titleLarge)
//        Text(text = "Available Funds: $${"%.2f".format(currentBalance)}")
//
//        // Add Funds Button
//        Button(
//            onClick = { showAddFundsDialog = true },
//            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
//            modifier = Modifier
//                .width(150.dp)
//                .height(40.dp)
//        ) {
//            Text(text = "+ Add Funds", fontSize = 14.sp)
//        }
//
//        if (showAddFundsDialog) {
//            AddFundsDialog(
//                onDismiss = { showAddFundsDialog = false },
//                onNext = { amount ->
//                    showAddFundsDialog = false
//                    onAddFundsClick(amount) // Pass amount to parent callback
//                }
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Drawer menu items
//        DrawerMenuItem(text = "News", onClick = onNewsClick)
//        DrawerMenuItem(text = "Support & Help", onClick = onSupportClick)
//        DrawerMenuItem(text = "Feedback & Reviews", onClick = onFeedbackClick)
//        DrawerMenuItem(text = "Settings", onClick = onSettingsClick)
//        DrawerMenuItem(text = "Log Out", onClick = onLogoutClick)
//    }
//}
//
//@Composable
//fun AddFundsDialog(
//    onDismiss: () -> Unit,
//    onNext: (Int) -> Unit
//) {
//    var amount by remember { mutableStateOf("") }
//    val context = LocalContext.current
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text(text = "Add Funds") },
//        text = {
//            Column {
//                Text(text = "How much would you like to add?")
//                Spacer(modifier = Modifier.height(8.dp))
//                OutlinedTextField(
//                    value = amount,
//                    onValueChange = { amount = it },
//                    label = { Text("Amount") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//        },
//        confirmButton = {
//            Button(
//                onClick = {
//                    val amountInt = amount.toIntOrNull()
//                    if (amountInt != null && amountInt > 0) {
//                        onNext(amountInt) // Trigger PayPal order creation
//                    } else {
//                        Log.e("AddFundsDialog", "Invalid amount entered")
//                    }
//                }
//            ) {
//                Text("Next")
//            }
//        },
//        dismissButton = {
//            Button(onClick = onDismiss) { Text("Cancel") }
//        }
//    )
//}
//
//// Helper function to create PayPal order and open approval URL in browser
//fun createPayPalOrder(context: Context, amount: Int, onResult: (String?) -> Unit) {
//    val request = JsonObjectRequest(
//        Request.Method.POST,
//        "https://act-production-5e24.up.railway.app/create-order",
//        JSONObject().put("amount", amount),
//        { response ->
//            // Extract approval URL and open it
//            val approvalUrl = response.optString("approval_url", null)
//            onResult(approvalUrl)
//        },
//        { error ->
//            Log.e("AddFundsDialog", "Error creating PayPal order: ${error.message}")
//            onResult(null)
//        }
//    )
//    Volley.newRequestQueue(context).add(request)
//}
//
//@Composable
//fun DrawerMenuItem(text: String, onClick: () -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClick)
//            .padding(16.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(text = text)
//    }
//}
//
