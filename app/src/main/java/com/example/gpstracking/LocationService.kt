package com.example.gpstracking

import android.util.Log
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.net.HttpURLConnection
import java.net.URL

class LocationService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient

    override fun onBind(intent: Intent?): IBinder? {
        return null // a started service, not a bound one
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking location....")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationClient
            .getLocationUpdates(5 * 60 * 1000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude
                val lon = location.longitude

                Log.d("LocationService", "Lat: $lat, Lon: $lon")

                // Send to server using HttpURLConnection in a background thread
                Thread {
                    try {
                        val url = URL("http://10.0.2.2/gps/save_location.php")
                        val postData = "rep_id=rep123&latitude=$lat&longitude=$lon"

                        with(url.openConnection() as HttpURLConnection) {
                            requestMethod = "POST"
                            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                            doOutput = true
                            outputStream.write(postData.toByteArray(Charsets.UTF_8))

                            val responseCode = responseCode
                            Log.d("ServerResponse", "HTTP Response Code: $responseCode")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("ServerError", "Failed to send location: ${e.message}")
                    }
                }.start()

                val updateNotification = notification.setContentText(
                    "Location: ($lat, $lon)"
                )
                notificationManager.notify(1, updateNotification.build())

            }
            .launchIn(serviceScope)

        startForeground(1,notification.build())
    }
    private fun stop(){
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP  = "ACTION_STOP"
    }


}
