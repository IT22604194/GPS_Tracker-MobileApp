package com.example.gpstracking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices

class LocationWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params)  {
    override fun doWork(): Result {
        getLocation()
        return Result.success()
    }
    private fun getLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.d("LocationWorker", "Lat: ${location.latitude}, Lon: ${location.longitude}")
                // TODO: Send to server here
            }
        }
    }

}