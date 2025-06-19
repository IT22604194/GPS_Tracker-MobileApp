package com.example.gpstracking

import android.content.Intent
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

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPSTrackingTheme {
                var username by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }

                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {

                    Text(text = "Login", style = MaterialTheme.typography.h5)
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
                        performLogin(username, password) { result ->
                            if (result == "Login successful") {
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.putExtra("username", username)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, result, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Text("Login")
                    }

                    TextButton(onClick = {
                        startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                    }) {
                        Text("Don't have an account? Register here")
                    }
                }
            }
        }
    }

    fun performLogin(username: String, password: String, onResult: (String) -> Unit) {
        Thread {
            try {
                val url = URL("http://192.168.251.74/gps/Login.php")
                val postData = "username=$username&password=$password"

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

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
