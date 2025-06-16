package com.example.gpstracking

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gpstracking.ui.theme.GPSTrackingTheme
import java.net.HttpURLConnection
import java.net.URL

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPSTrackingTheme {
                var username by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(text = "Register", style = MaterialTheme.typography.h5)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        performRegister(username, password) { result ->
                            Toast.makeText(this@RegisterActivity, result, Toast.LENGTH_SHORT).show()
                            if (result == "Registration successful") {
                                finish() // Close RegisterActivity and return to LoginActivity
                            }
                        }
                    }) {
                        Text("Register")
                    }
                }
            }
        }
    }

    private fun performRegister(username: String, password: String, onResult: (String) -> Unit) {
        Thread {
            try {
                val url = URL("http://10.0.2.2/gps/Register.php")
                val postData = "username=$username&password=$password"

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type","application/x-www-form-urlencoded")

                    outputStream.use { it.write(postData.toByteArray()) }

                    val response = inputStream.bufferedReader().readText()
                    Handler(Looper.getMainLooper()).post {
                        onResult(response.trim())
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    onResult("Error: ${e.message}")
                }
            }
        }.start()
    }
}
