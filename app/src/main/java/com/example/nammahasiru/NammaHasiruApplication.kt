package com.example.nammahasiru

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.WorkManager
import com.example.nammahasiru.data.DefaultSaplingRepository
import com.example.nammahasiru.data.LocationRepository
import com.example.nammahasiru.data.SaplingRepository

class NammaHasiruApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        container = AppDataContainer(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val name = getString(R.string.notification_channel_name)
        val descriptionText = getString(R.string.notification_channel_description)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = descriptionText
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID: String = "namma_hasiru_tree_checks"
    }
}

interface AppContainer {
    val saplingRepository: SaplingRepository
    val locationRepository: LocationRepository
}

class AppDataContainer(private val context: android.content.Context) : AppContainer {
    private val appContext = context.applicationContext
    private val database: NammaHasiruDatabase by lazy {
        NammaHasiruDatabase.getDatabase(appContext)
    }

    override val saplingRepository: SaplingRepository by lazy {
        DefaultSaplingRepository(
            database = database,
            saplingDao = database.saplingDao(),
            workManager = WorkManager.getInstance(appContext),
        )
    }

    override val locationRepository: LocationRepository by lazy {
        LocationRepository(appContext)
    }
}
