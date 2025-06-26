package com.example.gpstracking

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.Icon

import androidx.compose.material.MaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.gpstracking.ui.theme.GPSTrackingTheme
import kotlinx.coroutines.*

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContent {
            GPSTrackingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0D47A1) // Dark blue
                ){
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Text(
                                text = "Welcome to GPS Tracker",
                                color = Color.White,
                                style = MaterialTheme.typography.h5
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Location Icon
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Location Icon",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )


                        }


                    }
                }

            }
        }
     //Coroutine to simulate delay before checking LOGIN
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000L)//2 second splash delay

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

            val repId = sharedPref.getString("username" , null)

            if (repId.isNullOrEmpty()) {
                startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
            } else {
                startActivity(Intent(this@HomeActivity, MainActivity::class.java))
            }

            finish() // Close HomeActivity

        }
    }
}