package com.example.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.OrthodoxCalendarApplication
import com.example.R
import com.example.data.model.OrthodoxDayInfo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class OrthodoxAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext as? OrthodoxCalendarApplication ?: return
        val prefs = app.preferencesManager
        if (!prefs.dailyNotificationEnabled.value) return

        val today = LocalDate.now()
        val info = app.repository.getDayInfo(today)
        NotificationHelper.showDailyFeastNotification(context, info)

        // Reschedule for next day
        NotificationHelper.scheduleDailyAlarm(context)
    }
}

object NotificationHelper {
    const val CHANNEL_ID = "orthodox_calendar_channel"
    const val NOTIFICATION_ID = 1001
    const val TEST_NOTIFICATION_ID = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showDailyFeastNotification(context: Context, info: OrthodoxDayInfo, isTest: Boolean = false) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
        val dateTitle = info.date.format(formatter)

        val mainTitle = info.mainFeast?.title
            ?: info.saints.firstOrNull()
            ?: "День православного календаря"

        val subtext = "${info.fastingRule.title} • ${info.fastingDescription}"

        val bigText = buildString {
            append("📅 $dateTitle (по ст. ст. ${info.oldStyleDate.format(formatter)})\n")
            append("✝️ $mainTitle\n")
            append("🍲 ${info.fastingDescription}\n\n")
            if (info.mainFeast != null && info.troparion.isNotEmpty()) {
                append("«${info.troparion.take(160)}...»")
            } else {
                append("Святые дня: ${info.saints.take(2).joinToString("; ")}")
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_orthodox_cross)
            .setContentTitle(if (isTest) "🔔 Тест: $mainTitle" else "✝️ $dateTitle: $mainTitle")
            .setContentText(subtext)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        try {
            NotificationManagerCompat.from(context).notify(
                if (isTest) TEST_NOTIFICATION_ID else NOTIFICATION_ID,
                builder.build()
            )
        } catch (e: SecurityException) {
            // Permission not yet granted
        }
    }

    fun scheduleDailyAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, OrthodoxAlarmReceiver::class.java).apply {
            action = "com.example.orthodoxcalendar.ACTION_DAILY_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            // Safe alarm scheduling fallback
        }
    }
}
