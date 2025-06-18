package com.example.gpstracking

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.gpstracking.ui.theme.GPSTrackingTheme

class MainActivity : ComponentActivity() {

    private val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasLocationPermissions()) {
            ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, PERMISSION_REQUEST_CODE)
        }

        setContent {
            GPSTrackingTheme {
                var isTracking by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                if (hasLocationPermissions()) {
                                    startTracking()
                                    isTracking = true
                                } else {
                                    ActivityCompat.requestPermissions(
                                        this@MainActivity,
                                        LOCATION_PERMISSIONS,
                                        PERMISSION_REQUEST_CODE
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF388E3C))
                        ) {
                            Text("Start", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                stopTracking()
                                isTracking = false
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD32F2F))
                        ) {
                            Text("Stop", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = if (isTracking) "Tracking is ON" else "Tracking is OFF",
                            style = MaterialTheme.typography.h6
                        )
                    }
                }
            }
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return LOCATION_PERMISSIONS.all {
            ActivityCompat.checkSelfPermission(this, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startTracking() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            startService(this)
        }
    }

    private fun stopTracking() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            startService(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == android.content.pm.PackageManager.PERMISSION_GRANTED }) {
                startTracking()
            }
        }
    }
}
