package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

object CoarseLocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return coarse || fine
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCoarseCoordinates(context: Context): Pair<Double, Double>? {
        if (!hasLocationPermission(context)) return null

        val fusedResult = withTimeoutOrNull(6000L) {
            suspendCancellableCoroutine { cont ->
                val client = LocationServices.getFusedLocationProviderClient(context.applicationContext)
                val cts = CancellationTokenSource()
                cont.invokeOnCancellation { cts.cancel() }

                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            cont.resume(location.latitude to location.longitude)
                        } else {
                            client.lastLocation
                                .addOnSuccessListener { last ->
                                    if (last != null) {
                                        cont.resume(last.latitude to last.longitude)
                                    } else {
                                        cont.resume(null)
                                    }
                                }
                                .addOnFailureListener { cont.resume(null) }
                        }
                    }
                    .addOnFailureListener {
                        cont.resume(null)
                    }
            }
        }
        if (fusedResult != null) return fusedResult

        return runCatching {
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val networkLoc = manager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val passiveLoc = manager?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            val chosen = networkLoc ?: passiveLoc
            chosen?.let { it.latitude to it.longitude }
        }.getOrNull()
    }
}
