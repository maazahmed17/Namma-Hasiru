package com.example.nammahasiru.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nammahasiru.MainActivity
import com.example.nammahasiru.NammaHasiruApplication
import com.example.nammahasiru.NammaHasiruDatabase
import com.example.nammahasiru.R
import com.example.nammahasiru.model.SaplingStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class SaplingReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val saplingId = inputData.getLong(KEY_SAPLING_ID, -1L)
        if (saplingId <= 0L) return Result.failure()

        val dao = NammaHasiruDatabase.getDatabase(applicationContext).saplingDao()
        val sapling = dao.getSaplingByIdOnce(saplingId) ?: return Result.success()
        if (sapling.status != SaplingStatus.PENDING) return Result.success()

        val plantingInstant = runCatching { Instant.parse(sapling.plantingDate) }.getOrNull()
        val plantingText = if (plantingInstant != null) {
            val zoned = plantingInstant.atZone(ZoneId.systemDefault())
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(zoned)
        } else {
            sapling.plantingDate
        }

        val deepLinkUri = Uri.parse("nammahasiru://status/$saplingId")
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = deepLinkUri
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            saplingId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            NammaHasiruApplication.NOTIFICATION_CHANNEL_ID,
        )
            .setSmallIcon(R.drawable.ic_notification_tree)
            .setContentTitle(applicationContext.getString(R.string.reminder_notification_title))
            .setContentText(
                applicationContext.getString(
                    R.string.reminder_notification_body,
                    sapling.species,
                    plantingText,
                ),
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(saplingId.toInt(), notification)
        return Result.success()
    }

    companion object {
        const val KEY_SAPLING_ID: String = "sapling_id"
    }
}
