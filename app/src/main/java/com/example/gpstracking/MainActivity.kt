package com.example.gpstracking

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.gpstracking.ui.theme.GPSTrackingTheme
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Secure session
        val masterKey = MasterKey.Builder(applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPref = EncryptedSharedPreferences.create(
            applicationContext,
            "UserSession",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val repId = sharedPref.getString("username", null)

        //  If no session, go back to login
        if (repId == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

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
                        Text("Welcome back, $repId!", style = MaterialTheme.typography.h6)
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (hasLocationPermissions()) {
                                    startTracking(repId)
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
                            Text("Clock In", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                stopTracking(repId)
                                isTracking = false
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD32F2F))
                        ) {
                            Text("Clock Out", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = if (isTracking) "Tracking is ON" else "Tracking is OFF",
                            style = MaterialTheme.typography.h6
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                sharedPref.edit().clear().apply()
                                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                                finish()
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF607D8B))
                        ) {
                            Text("Logout", color = Color.White)
                        }
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

    private fun startTracking(repId: String) {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            putExtra("rep_id", repId)
            startService(this)
        }
    }

    private fun stopTracking(repId: String) {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            putExtra("rep_id", repId)
            startService(this)
        }

        if (!hasLocationPermissions()) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude.toString()
                    val lon = location.longitude.toString()
                    val clockOutTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                    val url = "http://192.168.23.74/gps/Backend/location_handler.php"
                    val requestQueue = Volley.newRequestQueue(applicationContext)

                    val stringRequest = object : StringRequest(
                        Method.POST, url,
                        Response.Listener { response ->
                            Log.d("ClockOutSuccess", "Server response: $response")
                            Toast.makeText(this, "Clocked out successfully", Toast.LENGTH_SHORT).show()
                        },
                        Response.ErrorListener { error ->
                            Log.e("ClockOutError", "Error: ${error.message}")
                            Toast.makeText(this, "Clock out failed", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        override fun getParams(): MutableMap<String, String> {
                            val params = HashMap<String, String>()
                            params["rep_id"] = repId
                            params["latitude"] = lat
                            params["longitude"] = lon
                            params["clock_out_time"] = clockOutTime
                            params["action"] = "clock_out"
                            return params
                        }
                    }

                    requestQueue.add(stringRequest)
                } else {
                    Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                    Log.e("ClockOutError", "Location is null")
                }
            }
        } catch (e: SecurityException) {
            Log.e("ClockOutError", "SecurityException: ${e.message}")
            Toast.makeText(this, "Permission error accessing location", Toast.LENGTH_SHORT).show()
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
                val masterKey = MasterKey.Builder(applicationContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val sharedPref = EncryptedSharedPreferences.create(
                    applicationContext,
                    "UserSession",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )

                val repId = sharedPref.getString("username", "unknown") ?: "unknown"
                startTracking(repId)
            }
        }
    }
}
