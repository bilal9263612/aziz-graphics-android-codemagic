package com.azizgraphics.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.azizgraphics.FileItem
import com.azizgraphics.R

class HtmlViewerActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_html_viewer)

        val fileItem = intent.getSerializableExtra("FILE_ITEM") as? FileItem
        if (fileItem == null) {
            finish()
            return
        }

        // Setup Toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = fileItem.displayName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // Setup WebView
        webView = findViewById(R.id.web_view)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        webSettings.allowUniversalAccessFromFileURLs = true
        webSettings.allowFileAccessFromFileURLs = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // Prevent navigation outside the app for embedded files
                return false
            }
        }

        // Load the embedded HTML file
        val fileUri = "file://" + fileItem.originalPath
        webView.loadUrl(fileUri)
    }
}
