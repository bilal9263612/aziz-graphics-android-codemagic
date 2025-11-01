package com.azizgraphics.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.azizgraphics.R\nimport android.os.Environment

class SplashActivity : AppCompatActivity() {

    private val STORAGE_PERMISSION_CODE = 101

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Proceed to main activity.
                navigateToMain()
            } else {
                // Permission is denied. Show a message and exit or navigate to main with limited functionality.
                Toast.makeText(this, "Storage permission denied. App functionality will be limited.", Toast.LENGTH_LONG).show()
                navigateToMain()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Display logo for a short duration
        Handler(Looper.getMainLooper()).postDelayed({
            checkStoragePermission()
        }, 2000) // 2 second delay for splash screen
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (R) and above requires MANAGE_EXTERNAL_STORAGE
            if (Environment.isExternalStorageManager()) {
                navigateToMain()
            } else {
                // Request MANAGE_EXTERNAL_STORAGE permission
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, STORAGE_PERMISSION_CODE)
            }
        } else {
            // Below Android 11, use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                navigateToMain()
            } else {
                // Request READ_EXTERNAL_STORAGE permission
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    navigateToMain()
                } else {
                    Toast.makeText(this, "Storage permission denied. App functionality will be limited.", Toast.LENGTH_LONG).show()
                    navigateToMain()
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
