package com.example.gpstracking
import android.location.Location
import kotlinx.coroutines.flow.Flow

//abstracting location tracking logic
interface LocationClient {
    fun getLocationUpdates(interval: Long): Flow<Location>

    class LocationException(message: String): Exception()
}