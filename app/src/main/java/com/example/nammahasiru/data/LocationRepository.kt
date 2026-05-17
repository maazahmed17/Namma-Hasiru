package com.example.nammahasiru.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationRepository(context: Context) {
    private val appContext = context.applicationContext
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    suspend fun getCurrentHighAccuracyLocation(): Location? = suspendCoroutine { cont ->
        if (!hasLocationPermission()) {
            cont.resume(null)
            return@suspendCoroutine
        }
        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resume(null) }
    }

    suspend fun getLastKnownLocationFallback(): Location? = suspendCoroutine { cont ->
        if (!hasLocationPermission()) {
            cont.resume(null)
            return@suspendCoroutine
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resume(null) }
    }

    suspend fun getBestAvailableLocation(): Location? {
        val current = getCurrentHighAccuracyLocation()
        if (current != null) return current
        return getLastKnownLocationFallback()
    }
}
