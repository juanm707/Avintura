package com.example.avintura.workers

import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.avintura.AvinturaApplication
import com.example.avintura.R
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import kotlin.random.Random


class LocalEventsWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val URL = "https://www.localwineevents.com/wine-tasting-events-and-festivals/near/napa-california/"

    override fun doWork(): Result {
        // get number of events
        val doc = Jsoup.connect(URL).get()
        val container = doc.getElementsByClass("container mb-6")
        val numEvents = container[0].getElementsByClass("card mb-2").size

        if (numEvents > 0) {
            val firstEventInList = container[0].getElementsByClass("card mb-2")[0].getElementsByTag("h5").text()
            val notificationContentText = applicationContext.resources.getQuantityString(R.plurals.local_events, numEvents, numEvents, firstEventInList)

            val webpage: Uri = Uri.parse(URL)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            val pendingIntent = PendingIntent
                .getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(applicationContext, AvinturaApplication.LOCAL_EVENTS_CHANNEL_ID)
                .setSmallIcon(R.drawable.avintura_bottle_no_text)
                .setContentTitle(AvinturaApplication.LOCAL_EVENTS_NOTIFICATION_CONTENT_TITLE)
                .setContentText(notificationContentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)

            // Show the notification
            NotificationManagerCompat.from(applicationContext)
                .notify(Random.nextInt(), builder.build())
        }
        return Result.success()
    }
}