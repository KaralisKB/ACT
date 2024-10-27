package com.example.act_mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.rememberImagePainter
import com.example.act_mobile.network.ApiClient
import com.example.act_mobile.network.NewsArticle
import kotlinx.coroutines.launch

@Composable
fun NewsScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Market News") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        val coroutineScope = rememberCoroutineScope()
        val newsArticles = remember { mutableStateListOf<NewsArticle>() }
        val isLoading = remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
                    val response = ApiClient.newsApi.getMarketNews(apiKey = "f788ed82831f48d1a5eb722182ff4a46")
                    newsArticles.addAll(response.articles)
                    isLoading.value = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (isLoading.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(newsArticles) { article ->
                    NewsItem(article)
                }
            }
        }
    }
}


@Composable
fun NewsItem(article: NewsArticle) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(
            text = article.title,
            style = MaterialTheme.typography.h6,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        article.urlToImage?.let { url ->
            Image(
                painter = rememberImagePainter(url),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = article.description ?: "",
            style = MaterialTheme.typography.body2,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = Color.Gray)
    }
}
