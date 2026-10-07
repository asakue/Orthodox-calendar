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

            val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))
            val oldStyleFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))

            val newStyleDateStr = today.format(dayOfWeekFormatter).replaceFirstChar { it.uppercase() }
            val oldStyleDateStr = if (dayInfo != null) {
                "${dayInfo.oldStyleDate.format(oldStyleFormatter)} по ст. ст."
            } else {
                "${today.minusDays(13).format(oldStyleFormatter)} по ст. ст."
            }

            val feastTitle = if (dayInfo != null) {
                if (dayInfo.mainFeast != null) {
                    "✝️ ${dayInfo.mainFeast.title}"
                } else if (dayInfo.saints.isNotEmpty()) {
                    dayInfo.saints.first()
                } else {
                    "Память святых дня"
                }
            } else {
                "Православный день"
            }

            val fastTag = dayInfo?.fastingRule?.let { rule ->
                "${rule.foodIcon} ${rule.title}"
            } ?: "Постный день"

            val fastDetail = dayInfo?.fastingDescription
                ?: "Устав о трапезе дня по уставу Церкви"

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
                "Церковные праздники года"
            }

            val views = RemoteViews(context.packageName, R.layout.widget_orthodox_calendar).apply {
                setTextViewText(R.id.widget_date_text, newStyleDateStr)
                setTextViewText(R.id.widget_old_style_text, oldStyleDateStr)
                setTextViewText(R.id.widget_fasting_text, fastTag)
                setTextViewText(R.id.widget_today_feast_title, feastTitle)
                setTextViewText(R.id.widget_fasting_detail, fastDetail)
                setTextViewText(R.id.widget_upcoming_feast, upcomingText)

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
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
