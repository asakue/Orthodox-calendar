package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FavoriteEntity
import com.example.data.local.UserPreferencesManager
import com.example.data.model.AppThemeMode
import com.example.data.model.FeastType
import com.example.data.model.OrthodoxDayInfo
import com.example.data.model.OrthodoxHoliday
import com.example.data.model.PrayerItem
import com.example.data.model.ReadingFontSize
import com.example.data.repository.OrthodoxCalendarRepository
import com.example.notification.NotificationHelper
import com.example.widget.OrthodoxCalendarAppWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class AppNavTab(val title: String) {
    TODAY("Сегодня"),
    CALENDAR("Календарь"),
    HOLIDAYS("Праздники"),
    PRAYERS("Молитвослов"),
    FAVORITES("Избранное"),
    SETTINGS("Настройки")
}

class MainViewModel(
    private val repository: OrthodoxCalendarRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _currentTab = MutableStateFlow(AppNavTab.TODAY)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _dayInfo = MutableStateFlow(repository.getDayInfo(LocalDate.now()))
    val dayInfo: StateFlow<OrthodoxDayInfo> = _dayInfo.asStateFlow()

    private val _selectedHoliday = MutableStateFlow<OrthodoxHoliday?>(null)
    val selectedHoliday: StateFlow<OrthodoxHoliday?> = _selectedHoliday.asStateFlow()

    private val _selectedPrayer = MutableStateFlow<PrayerItem?>(null)
    val selectedPrayer: StateFlow<PrayerItem?> = _selectedPrayer.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _holidayFilter = MutableStateFlow("Все")
    val holidayFilter: StateFlow<String> = _holidayFilter.asStateFlow()

    private val _prayerFilter = MutableStateFlow("Все")
    val prayerFilter: StateFlow<String> = _prayerFilter.asStateFlow()

    val themeMode: StateFlow<AppThemeMode> = preferencesManager.themeMode
    val fontSize: StateFlow<ReadingFontSize> = preferencesManager.fontSize
    val dailyNotificationEnabled: StateFlow<Boolean> = preferencesManager.dailyNotificationEnabled
    val notificationHour: StateFlow<Int> = preferencesManager.notificationHour
    val notificationMinute: StateFlow<Int> = preferencesManager.notificationMinute

    val favorites: StateFlow<List<FavoriteEntity>> = repository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nearestFeast: Pair<OrthodoxHoliday, Long> = repository.getUpcomingGreatFeast(LocalDate.now())

    val allYearHolidays: List<Pair<LocalDate, OrthodoxHoliday>> =
        repository.getAllHolidaysForYear(LocalDate.now().year)

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
        _selectedHoliday.value = null
        _selectedPrayer.value = null
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _dayInfo.value = repository.getDayInfo(date)
    }

    fun selectHoliday(holiday: OrthodoxHoliday?) {
        _selectedHoliday.value = holiday
    }

    fun selectPrayer(prayer: PrayerItem?) {
        _selectedPrayer.value = prayer
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setHolidayFilter(filter: String) {
        _holidayFilter.value = filter
    }

    fun setPrayerFilter(filter: String) {
        _prayerFilter.value = filter
    }

    fun toggleFavoriteHoliday(holiday: OrthodoxHoliday, date: LocalDate?) {
        viewModelScope.launch {
            val isFav = favorites.value.any { it.targetId == holiday.id }
            if (isFav) {
                repository.removeFavorite(holiday.id)
            } else {
                val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
                val dateStr = date?.format(formatter) ?: if (holiday.dateDay > 0) "${holiday.dateDay}.${holiday.dateMonth}" else "Переходящий"
                repository.toggleFavoriteHoliday(holiday, dateStr)
            }
        }
    }

    fun toggleFavoritePrayer(prayer: PrayerItem) {
        viewModelScope.launch {
            val isFav = favorites.value.any { it.targetId == prayer.id }
            if (isFav) {
                repository.removeFavorite(prayer.id)
            } else {
                repository.toggleFavoritePrayer(prayer)
            }
        }
    }

    fun removeFavorite(targetId: String) {
        viewModelScope.launch {
            repository.removeFavorite(targetId)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        preferencesManager.setThemeMode(mode)
    }

    fun setFontSize(size: ReadingFontSize) {
        preferencesManager.setFontSize(size)
    }

    fun setDailyNotificationEnabled(enabled: Boolean, context: Context) {
        preferencesManager.setDailyNotificationEnabled(enabled)
        if (enabled) {
            NotificationHelper.scheduleDailyAlarm(context)
        }
    }

    fun setNotificationTime(hour: Int, minute: Int, context: Context) {
        preferencesManager.setNotificationTime(hour, minute)
        NotificationHelper.scheduleDailyAlarm(context)
    }

    fun triggerTestNotification(context: Context) {
        val todayInfo = repository.getDayInfo(LocalDate.now())
        NotificationHelper.showDailyFeastNotification(context, todayInfo, isTest = true)
    }

    fun updateWidgets(context: Context) {
        OrthodoxCalendarAppWidgetProvider.updateAllWidgets(context)
    }

    fun getFilteredHolidays(): List<Pair<LocalDate, OrthodoxHoliday>> {
        val query = _searchQuery.value.trim().lowercase(Locale("ru"))
        val filter = _holidayFilter.value

        return allYearHolidays.filter { pair ->
            val holiday = pair.second
            val matchesFilter = when (filter) {
                "Все" -> true
                "Двунадесятые" -> holiday.feastType == FeastType.TWELVE_GREAT
                "Великие" -> holiday.feastType == FeastType.GREAT || holiday.feastType == FeastType.PASCHA
                "Богородичные" -> holiday.title.contains("Богород", ignoreCase = true) || holiday.feastType == FeastType.THEOTOKOS
                "Господские" -> holiday.feastType == FeastType.LORD_FEAST || holiday.feastType == FeastType.TWELVE_GREAT
                "Святые" -> holiday.feastType == FeastType.SAINT_DAY
                "Посты" -> holiday.feastType == FeastType.FAST_PERIOD
                else -> true
            }

            val matchesQuery = if (query.isEmpty()) true else {
                holiday.title.lowercase(Locale("ru")).contains(query) ||
                holiday.subtitle.lowercase(Locale("ru")).contains(query) ||
                holiday.description.lowercase(Locale("ru")).contains(query) ||
                holiday.troparion.lowercase(Locale("ru")).contains(query) ||
                holiday.prayer.lowercase(Locale("ru")).contains(query)
            }

            matchesFilter && matchesQuery
        }
    }

    fun getFilteredPrayers(): List<PrayerItem> {
        val query = _searchQuery.value.trim().lowercase(Locale("ru"))
        val filter = _prayerFilter.value
        val all = repository.getAllPrayers()

        return all.filter { prayer ->
            val matchesFilter = when (filter) {
                "Все" -> true
                "Главные" -> prayer.category == "Главные молитвы"
                "Утренние" -> prayer.category.contains("Утренние")
                "Богородице" -> prayer.category.contains("Богородице")
                "Святым" -> prayer.category.contains("святым")
                "В скорбях" -> prayer.category.contains("скорбях")
                "О семье" -> prayer.category.contains("семье")
                "Благодарственные" -> prayer.category.contains("Благодарственные")
                else -> true
            }

            val matchesQuery = if (query.isEmpty()) true else {
                prayer.title.lowercase(Locale("ru")).contains(query) ||
                prayer.targetFeastOrSaint.lowercase(Locale("ru")).contains(query) ||
                prayer.text.lowercase(Locale("ru")).contains(query) ||
                prayer.category.lowercase(Locale("ru")).contains(query)
            }

            matchesFilter && matchesQuery
        }
    }
}
