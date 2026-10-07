package com.example.data.repository

import com.example.data.local.FavoriteDao
import com.example.data.local.FavoriteEntity
import com.example.data.model.FastingRule
import com.example.data.model.FeastType
import com.example.data.model.OrthodoxDayInfo
import com.example.data.model.OrthodoxHoliday
import com.example.data.model.PrayerItem
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class OrthodoxCalendarRepository(
    private val favoriteDao: FavoriteDao
) {

    private val russianDateFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
    private val fullDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy г.", Locale("ru"))

    fun getDayInfo(date: LocalDate): OrthodoxDayInfo {
        val pascha = OrthodoxCalendarCalculator.calculatePascha(date.year)
        val oldStyleDate = OrthodoxCalendarCalculator.toOldStyle(date)
        val paschaOffset = ChronoUnit.DAYS.between(pascha, date).toInt()

        // 1. Check for movable feasts matching today
        val matchingMovable = OrthodoxFeastRegistry.movableFeasts.filter { it.paschaOffset == paschaOffset }

        // 2. Check for fixed feasts matching today
        val matchingFixed = OrthodoxFeastRegistry.fixedFeasts.filter {
            it.dateMonth == date.monthValue && it.dateDay == date.dayOfMonth
        }

        val allFeasts = (matchingMovable + matchingFixed).sortedBy { it.feastType.level }
        val mainFeast = allFeasts.firstOrNull()

        // 3. Saints for today
        val saints = OrthodoxFeastRegistry.getSaintsForDate(date.monthValue, date.dayOfMonth)

        // 4. Calculate Fasting Rule
        val (fastRule, fastDesc) = calculateFastingRule(date, pascha, paschaOffset, mainFeast)

        // 5. Default Readings & Liturgical texts if not explicitly on a major feast
        val gospel = mainFeast?.gospelReading ?: "Евангельское зачало дня по уставу"
        val apostle = mainFeast?.apostleReading ?: "Апостольское чтение рядовое"
        val troparion = mainFeast?.troparion ?: "Тропарь святым дня: Правило веры и образ кротости..."
        val kontakion = mainFeast?.kontakion ?: "Кондак дня: В молитвах неусыпая..."
        val prayer = mainFeast?.prayer ?: "Молитва дня: Господи Иисусе Христе, помилуй нас..."

        val isGreat = mainFeast != null && (
            mainFeast.feastType == FeastType.PASCHA ||
            mainFeast.feastType == FeastType.TWELVE_GREAT ||
            mainFeast.feastType == FeastType.GREAT
        )

        return OrthodoxDayInfo(
            date = date,
            oldStyleDate = oldStyleDate,
            mainFeast = mainFeast,
            allFeasts = allFeasts,
            saints = saints,
            fastingRule = fastRule,
            fastingDescription = fastDesc,
            isGreatFeast = isGreat,
            gospelReading = gospel,
            apostleReading = apostle,
            troparion = troparion,
            kontakion = kontakion,
            prayer = prayer
        )
    }

    private fun calculateFastingRule(
        date: LocalDate,
        pascha: LocalDate,
        paschaOffset: Int,
        mainFeast: OrthodoxHoliday?
    ): Pair<FastingRule, String> {
        // Explicit override on Great Feasts (e.g., Pascha has no fast, Palm Sunday allows fish)
        if (mainFeast?.fastingRuleOverride != null) {
            val rule = mainFeast.fastingRuleOverride
            return Pair(rule, "${rule.title}: ${rule.shortDesc}")
        }

        // Check if in a solid week (Сплошная седмица)
        if (OrthodoxCalendarCalculator.isSolidWeek(date, pascha)) {
            // Check if Cheese week (Maslenitsa)
            val offset = ChronoUnit.DAYS.between(pascha, date)
            if (offset in -55..-49) {
                return Pair(FastingRule.CHEESE_ALLOWED, "Масленица (Сырная седмица): разрешаются яйца, молоко, рыба, без мяса")
            }
            return Pair(FastingRule.NO_FAST, "Сплошная седмица: поста в среду и пятницу нет")
        }

        // Multi-day Fasts
        val multiDayFast = OrthodoxCalendarCalculator.getMultiDayFastName(date, pascha)
        if (multiDayFast != null) {
            when (multiDayFast) {
                "Великий Пост" -> {
                    // Holy Week
                    if (paschaOffset in -6..-1) {
                        return when (paschaOffset) {
                            -2 -> Pair(FastingRule.STRICT_FAST, "Великая Пятница: Строгий пост (до выноса Плащаницы)")
                            -3 -> Pair(FastingRule.OIL_ALLOWED, "Великий Четверг: Пища с растительным маслом")
                            else -> Pair(FastingRule.DRY_EATING, "Страстная седмица: Сухоядение")
                        }
                    }
                    // Weekend during Great Lent -> oil allowed
                    return if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
                        Pair(FastingRule.OIL_ALLOWED, "Великий пост (выходной): Разрешается горячая пища с маслом и вино")
                    } else if (date.dayOfWeek == DayOfWeek.MONDAY || date.dayOfWeek == DayOfWeek.WEDNESDAY || date.dayOfWeek == DayOfWeek.FRIDAY) {
                        Pair(FastingRule.DRY_EATING, "Великий пост: Сухоядение (хлеб, вода, овощи, фрукты)")
                    } else {
                        Pair(FastingRule.WITHOUT_OIL, "Великий пост: Горячая пища без масла")
                    }
                }
                "Успенский Пост" -> {
                    return if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
                        Pair(FastingRule.OIL_ALLOWED, "Успенский пост: Пища с растительным маслом")
                    } else {
                        Pair(FastingRule.DRY_EATING, "Успенский пост: Сухоядение / без масла")
                    }
                }
                "Рождественский Пост" -> {
                    // Weekends allow fish until Jan 2
                    return if (date.monthValue == 1 && date.dayOfMonth in 2..5) {
                        Pair(FastingRule.WITHOUT_OIL, "Предпразднство Рождества: Строгий пост без рыбы")
                    } else if (date.monthValue == 1 && date.dayOfMonth == 6) {
                        Pair(FastingRule.STRICT_FAST, "Рождественский сочельник: Пост до первой вечерней звезды (сочиво)")
                    } else if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY || date.dayOfWeek == DayOfWeek.TUESDAY || date.dayOfWeek == DayOfWeek.THURSDAY) {
                        Pair(FastingRule.FISH_ALLOWED, "Рождественский пост: Разрешается рыба и масло")
                    } else {
                        Pair(FastingRule.WITHOUT_OIL, "Рождественский пост: Горячая пища без масла")
                    }
                }
                "Петров Пост" -> {
                    return if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY || date.dayOfWeek == DayOfWeek.TUESDAY || date.dayOfWeek == DayOfWeek.THURSDAY) {
                        Pair(FastingRule.FISH_ALLOWED, "Петров пост: Разрешается рыба и растительное масло")
                    } else {
                        Pair(FastingRule.WITHOUT_OIL, "Петров пост: Пища без масла")
                    }
                }
            }
        }

        // Single Day Strict Fasts
        if (date.monthValue == 1 && date.dayOfMonth == 18) {
            return Pair(FastingRule.WITHOUT_OIL, "Крещенский сочельник: Строгий однодневный пост")
        }
        if (date.monthValue == 9 && date.dayOfMonth == 11) {
            return Pair(FastingRule.WITHOUT_OIL, "Усекновение главы Иоанна Предтечи: Однодневный пост")
        }
        if (date.monthValue == 9 && date.dayOfMonth == 27) {
            return Pair(FastingRule.WITHOUT_OIL, "Воздвижение Креста Господня: Однодневный пост")
        }

        // Regular Wednesdays and Fridays
        if (date.dayOfWeek == DayOfWeek.WEDNESDAY || date.dayOfWeek == DayOfWeek.FRIDAY) {
            return Pair(FastingRule.WITHOUT_OIL, "Постный день (Среда/Пятница): воздержание от мясной и молочной пищи")
        }

        return Pair(FastingRule.NO_FAST, "Поста нет: скоромная пища (разрешается всё)")
    }

    /**
     * Finds the next major/great feast from today.
     */
    fun getUpcomingGreatFeast(fromDate: LocalDate): Pair<OrthodoxHoliday, Long> {
        var closestFeast: OrthodoxHoliday? = null
        var minDays: Long = Long.MAX_VALUE

        // Check for up to 365 days ahead
        for (i in 0L..365L) {
            val checkDate = fromDate.plusDays(i)
            val info = getDayInfo(checkDate)
            val greatFeast = info.allFeasts.firstOrNull {
                it.feastType == FeastType.PASCHA ||
                it.feastType == FeastType.TWELVE_GREAT ||
                it.feastType == FeastType.GREAT
            }
            if (greatFeast != null) {
                closestFeast = greatFeast
                minDays = i
                break
            }
        }

        val fallback = OrthodoxFeastRegistry.fixedFeasts.first()
        return Pair(closestFeast ?: fallback, minDays)
    }

    /**
     * Gets all holidays for a given year sorted chronologically.
     */
    fun getAllHolidaysForYear(year: Int): List<Pair<LocalDate, OrthodoxHoliday>> {
        val pascha = OrthodoxCalendarCalculator.calculatePascha(year)
        val result = mutableListOf<Pair<LocalDate, OrthodoxHoliday>>()

        // Movable
        OrthodoxFeastRegistry.movableFeasts.forEach { feast ->
            val date = pascha.plusDays(feast.paschaOffset.toLong())
            if (date.year == year) {
                result.add(Pair(date, feast))
            }
        }

        // Fixed
        OrthodoxFeastRegistry.fixedFeasts.forEach { feast ->
            val date = LocalDate.of(year, feast.dateMonth, feast.dateDay)
            result.add(Pair(date, feast))
        }

        return result.sortedBy { it.first }
    }

    fun getAllPrayers(): List<PrayerItem> = OrthodoxPrayerRegistry.allPrayers

    fun getHolidayById(id: String, year: Int = LocalDate.now().year): Pair<LocalDate, OrthodoxHoliday>? {
        val all = getAllHolidaysForYear(year)
        return all.firstOrNull { it.second.id == id }
    }

    fun getPrayerById(id: String): PrayerItem? {
        val direct = OrthodoxPrayerRegistry.allPrayers.firstOrNull { it.id == id }
        if (direct != null) return direct

        // Check dynamic feast prayers
        OrthodoxFeastRegistry.fixedFeasts.forEach { f ->
            val matched = OrthodoxPrayerRegistry.getPrayersForHoliday(f)
            val found = matched.firstOrNull { it.id == id }
            if (found != null) return found
        }
        OrthodoxFeastRegistry.movableFeasts.forEach { f ->
            val matched = OrthodoxPrayerRegistry.getPrayersForHoliday(f)
            val found = matched.firstOrNull { it.id == id }
            if (found != null) return found
        }
        return null
    }

    // Favorites
    fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()

    fun isFavorite(targetId: String): Flow<Boolean> = favoriteDao.isFavorite(targetId)

    suspend fun toggleFavoriteHoliday(holiday: OrthodoxHoliday, dateString: String) {
        val favId = "fav_feast_${holiday.id}"
        val existing = favoriteDao.deleteByTargetId(holiday.id)
        // If delete didn't hit or we want toggle behavior:
        val entity = FavoriteEntity(
            id = favId,
            type = "FEAST",
            title = holiday.title,
            subtitle = holiday.subtitle,
            targetId = holiday.id,
            dateInfo = dateString
        )
        favoriteDao.insertFavorite(entity)
    }

    suspend fun removeFavorite(targetId: String) {
        favoriteDao.deleteByTargetId(targetId)
    }

    suspend fun toggleFavoritePrayer(prayer: PrayerItem) {
        val favId = "fav_prayer_${prayer.id}"
        val entity = FavoriteEntity(
            id = favId,
            type = "PRAYER",
            title = prayer.title,
            subtitle = prayer.category,
            targetId = prayer.id,
            dateInfo = prayer.targetFeastOrSaint
        )
        favoriteDao.insertFavorite(entity)
    }
}
