package com.example.act_mobile.ui.network

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.firestore.ListenerRegistration

fun fetchUserBalance(userId: String, onResult: (Double?, String?) -> Unit): ListenerRegistration {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(userId)

    // for real-time updates
    return userRef.addSnapshotListener { snapshot, e ->
        if (e != null) {
            Log.e("fetchUserBalance", "Error fetching balance: ", e)
            onResult(null, "Error fetching balance: ${e.message}")
            return@addSnapshotListener
        }

        if (snapshot != null && snapshot.exists()) {
            val balance = snapshot.getDouble("balance")
            if (balance != null) {
                onResult(balance, null)  // passes the balance if successful
            } else {
                onResult(null, "Failed to retrieve balance.")
            }
        } else {
            onResult(null, "Document does not exist.")
        }
    }
}
