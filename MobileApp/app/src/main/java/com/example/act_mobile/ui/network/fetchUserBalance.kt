package com.example.act_mobile.ui.network

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.firestore.ListenerRegistration

//// Modify this function to correctly update and retrieve user balance
//fun fetchUserBalance(userId: String, onResult: (Double?, String?) -> Unit) {
//    val db = FirebaseFirestore.getInstance()
//    val userRef = db.collection("users").document(userId)
//
//    userRef.get()
//        .addOnSuccessListener { document ->
//            val balance = document.getDouble("balance")
//            if (balance != null) {
//                onResult(balance, null)  // Pass the balance if successful
//            } else {
//                onResult(null, "Failed to retrieve balance.")
//            }
//        }
//        .addOnFailureListener { e ->
//            Log.e("fetchUserBalance", "Error fetching balance: ", e)
//            onResult(null, "Error fetching balance: ${e.message}")
//        }
//}
// Modify this function to set up a real-time listener for balance updates
fun fetchUserBalance(userId: String, onResult: (Double?, String?) -> Unit): ListenerRegistration {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(userId)

    // Use addSnapshotListener for real-time updates
    return userRef.addSnapshotListener { snapshot, e ->
        if (e != null) {
            Log.e("fetchUserBalance", "Error fetching balance: ", e)
            onResult(null, "Error fetching balance: ${e.message}")
            return@addSnapshotListener
        }

        if (snapshot != null && snapshot.exists()) {
            val balance = snapshot.getDouble("balance")
            if (balance != null) {
                onResult(balance, null)  // Pass the balance if successful
            } else {
                onResult(null, "Failed to retrieve balance.")
            }
        } else {
            onResult(null, "Document does not exist.")
        }
    }
}
