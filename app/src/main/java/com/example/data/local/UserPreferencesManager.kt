package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppThemeMode
import com.example.data.model.ReadingFontSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("orthodox_calendar_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getSavedThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _fontSize = MutableStateFlow(getSavedFontSize())
    val fontSize: StateFlow<ReadingFontSize> = _fontSize.asStateFlow()

    private val _dailyNotificationEnabled = MutableStateFlow(getSavedDailyNotification())
    val dailyNotificationEnabled: StateFlow<Boolean> = _dailyNotificationEnabled.asStateFlow()

    private val _notificationHour = MutableStateFlow(prefs.getInt("notif_hour", 8))
    val notificationHour: StateFlow<Int> = _notificationHour.asStateFlow()

    private val _notificationMinute = MutableStateFlow(prefs.getInt("notif_min", 0))
    val notificationMinute: StateFlow<Int> = _notificationMinute.asStateFlow()

    private val _eveNotificationEnabled = MutableStateFlow(prefs.getBoolean("notif_eve", true))
    val eveNotificationEnabled: StateFlow<Boolean> = _eveNotificationEnabled.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    private fun getSavedThemeMode(): AppThemeMode {
        val name = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name)
        return try {
            AppThemeMode.valueOf(name ?: AppThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setFontSize(size: ReadingFontSize) {
        prefs.edit().putString("font_size", size.name).apply()
        _fontSize.value = size
    }

    private fun getSavedFontSize(): ReadingFontSize {
        val name = prefs.getString("font_size", ReadingFontSize.NORMAL.name)
        return try {
            ReadingFontSize.valueOf(name ?: ReadingFontSize.NORMAL.name)
        } catch (e: Exception) {
            ReadingFontSize.NORMAL
        }
    }

    fun setDailyNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notif_daily", enabled).apply()
        _dailyNotificationEnabled.value = enabled
    }

    private fun getSavedDailyNotification(): Boolean {
        return prefs.getBoolean("notif_daily", true)
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        prefs.edit().putInt("notif_hour", hour).putInt("notif_min", minute).apply()
        _notificationHour.value = hour
        _notificationMinute.value = minute
    }

    fun setEveNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notif_eve", enabled).apply()
        _eveNotificationEnabled.value = enabled
    }
}
