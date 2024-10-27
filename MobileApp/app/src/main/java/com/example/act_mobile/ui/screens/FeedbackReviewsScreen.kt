package com.example.act_mobile.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun FeedbackReviewsScreen(onBackClick: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var reviewText by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    var reviews by remember { mutableStateOf(listOf<Review>()) }

    //  reviews from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("reviews")
            .get()
            .addOnSuccessListener { result ->
                reviews = result.documents.mapNotNull { document ->
                    document.toObject(Review::class.java)
                }
            }
    }

    BackHandler(onBack = { onBackClick() })

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // name input
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // review input
            TextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                label = { Text("Leave your review") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 6
            )
            Spacer(modifier = Modifier.height(16.dp))

            // star ratings
            Row {
                repeat(5) { index ->
                    Text(
                        text = if (index < rating) "★" else "☆",
                        modifier = Modifier.clickable { rating = index + 1 },
                        style = MaterialTheme.typography.h4
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (userId != null && reviewText.isNotBlank() && name.isNotBlank()) {
                        val review = hashMapOf(
                            "userId" to userId,
                            "username" to name,
                            "review" to reviewText,
                            "rating" to rating
                        )
                        firestore.collection("reviews").add(review)
                            .addOnSuccessListener {
                                // empties all fields after submit
                                name = ""
                                reviewText = ""
                                rating = 0

                                // get reviews again to update the list
                                firestore.collection("reviews")
                                    .get()
                                    .addOnSuccessListener { result ->
                                        reviews = result.documents.mapNotNull { document ->
                                            document.toObject(Review::class.java)
                                        }
                                    }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Review")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // displaying all reviews
            LazyColumn {
                items(reviews) { review ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(text = "Name: ${review.username}", style = MaterialTheme.typography.h6)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Review: ${review.review}")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Rating: " + "★".repeat(review.rating) + "☆".repeat(5 - review.rating))
                        }
                    }
                }
            }
        }
    }
}

// review data class
data class Review(
    val userId: String = "",
    val username: String = "",
    val review: String = "",
    val rating: Int = 0
)
