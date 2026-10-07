package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.OrthodoxCalendarApplication
import com.example.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class OrthodoxCalendarAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val app = context.applicationContext as? OrthodoxCalendarApplication
            val repository = app?.repository

            val today = LocalDate.now()
            val dayInfo = repository?.getDayInfo(today)
            val upcoming = repository?.getUpcomingGreatFeast(today)

            val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
            val dateText = "Сегодня, ${today.format(formatter)}"

            val feastTitle = dayInfo?.mainFeast?.title
                ?: dayInfo?.saints?.firstOrNull()
                ?: "Память святых дня"

            val fastRule = dayInfo?.fastingRule?.title ?: "Постный день"

            val upcomingText = if (upcoming != null) {
                val days = upcoming.second
                val daysStr = when (days) {
                    0L -> "сегодня!"
                    1L -> "завтра"
                    in 2L..4L -> "через $days дня"
                    else -> "через $days дн."
                }
                "${upcoming.first.title} ($daysStr)"
            } else {
                "Праздники года"
            }

            val views = RemoteViews(context.packageName, R.layout.widget_orthodox_calendar).apply {
                setTextViewText(R.id.widget_date_text, dateText)
                setTextViewText(R.id.widget_fasting_text, fastRule)
                setTextViewText(R.id.widget_today_feast_title, feastTitle)
                setTextViewText(R.id.widget_upcoming_feast, upcomingText)

                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, OrthodoxCalendarAppWidgetProvider::class.java)
            val appWidgetIds = manager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, OrthodoxCalendarAppWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
