package com.example.fizyoapp.presentation.user.illnessrecord

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PdfViewerScreen(
    pdfUrl: String,
    title: String,
    onClose: () -> Unit,
    onShare: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pdfBase64 by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // PDF'i yükleme işlevi
    fun loadPdf() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val base64 = withContext(Dispatchers.IO) {
                    downloadPdfAsBase64(pdfUrl)
                }
                pdfBase64 = base64
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "PDF yüklenirken bir hata oluştu: ${e.message}"
                isLoading = false
            }
        }
    }

    // İlk yükleme
    LaunchedEffect(pdfUrl) {
        loadPdf()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Üst çubuk
        TopAppBar(
            title = { Text(title, maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat"
                    )
                }
            },
            actions = {
                if (onShare != null) {
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Paylaş"
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(59, 62, 104),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )

        // İçerik alanı
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(59, 62, 104))
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Bilinmeyen hata",
                            color = Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { loadPdf() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(59, 62, 104)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Tekrar Dene"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tekrar Dene")
                        }
                    }
                }
                pdfBase64 != null -> {
                    AndroidView(
                        factory = { context ->
                            createWebView(context, pdfBase64!!)
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { webView ->
                            // WebView güncellemeleri gerekirse
                        }
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createWebView(context: Context, base64Pdf: String): WebView {
    return WebView(context).apply {
        settings.apply {
            javaScriptEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            cacheMode = WebSettings.LOAD_NO_CACHE
            domStorageEnabled = true
        }

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                return false
            }
        }

        // HTML içinde PDF'i göstermek için
        val pdfHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body, html {
                        margin: 0;
                        padding: 0;
                        width: 100%;
                        height: 100%;
                        overflow: hidden;
                    }
                    #pdf-viewer {
                        width: 100%;
                        height: 100%;
                        border: none;
                    }
                </style>
            </head>
            <body>
                <embed id="pdf-viewer" 
                    src="data:application/pdf;base64,$base64Pdf" 
                    type="application/pdf" 
                    width="100%" 
                    height="100%" />
            </body>
            </html>
        """.trimIndent()

        loadDataWithBaseURL(null, pdfHtml, "text/html", "UTF-8", null)
    }
}

private suspend fun downloadPdfAsBase64(fileUrl: String): String {
    return withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(fileUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP error code: ${connection.responseCode}")
            }

            val inputStream = connection.inputStream
            val outputStream = ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            val pdfBytes = outputStream.toByteArray()
            inputStream.close()
            outputStream.close()

            Base64.encodeToString(pdfBytes, Base64.DEFAULT)
        } finally {
            connection?.disconnect()
        }
    }
}