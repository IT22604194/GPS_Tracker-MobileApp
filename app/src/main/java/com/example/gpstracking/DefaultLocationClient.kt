package com.example.gpstracking

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import  kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class DefaultLocationClient (
    private val context: Context,
    private val client: FusedLocationProviderClient
): LocationClient{

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            if(!context.hasLocationPermission()) {
                throw LocationClient.LocationException("Missing location permission")
            }

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if(!isGpsEnabled && !isNetworkEnabled) {
                throw LocationClient.LocationException("GPS is disabled")
            }

           val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,

               5 * 60 * 1000L // 5 minutes in milliseconds

            ).apply {
                setMinUpdateIntervalMillis(5 * 60 * 1000L)// Minimum update every 5 min
                setMaxUpdateDelayMillis(6 * 60 * 1000L) // Optional buffer
            }.build()



            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {


                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let { location ->
                        Log.d("LocationFlow", "New location: ${location.latitude}, ${location.longitude}")
                        launch { send(location) }
                    }
                }
            }
            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )

            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }

}
