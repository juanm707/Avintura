package com.example.avintura

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.*
import com.example.avintura.database.AvinturaDatabase
import com.example.avintura.repository.AvinturaRepository
import com.example.avintura.workers.LocalEventsWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.TimeUnit

class AvinturaApplication : Application() {
    // No need to cancel this scope as it'll be torn down with the process
    private val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { AvinturaDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { AvinturaRepository(
        database,
        database.businessDao(),
        database.favoriteDao(),
        database.businessDetailDao(),
        database.photoDao(),
        database.reviewDao(),
        database.hourDao(),
        database.openDao(),
        database.featuredDao(),
        database.categoryTypeDao()
    )}

    override fun onCreate() {
        super.onCreate()
        makeNotificationChannel()
        createLocalEventsWorker()
    }

    private fun createLocalEventsWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<LocalEventsWorker>(7, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            LOCAL_EVENTS_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun makeNotificationChannel() {
        // Make a channel if necessary
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.local_events_channel)
            val descriptionText = getString(R.string.local_events_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(LOCAL_EVENTS_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val LOCAL_EVENTS_CHANNEL_ID = "local_events_id"
        val LOCAL_EVENTS_NOTIFICATION_CONTENT_TITLE: CharSequence = "Featured Local Wine Events by Napa"
        // The name of the work
        const val LOCAL_EVENTS_WORK_NAME = "local_events_work"
    }
}