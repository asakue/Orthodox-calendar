package com.example.data.repository

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object OrthodoxCalendarCalculator {

    /**
     * Calculates the Gregorian date of Orthodox Pascha for a given year
     * using the traditional Alexandrian computus (Meeus/Gauss algorithm for Julian Easter converted to Gregorian).
     */
    fun calculatePascha(year: Int): LocalDate {
        val a = year % 4
        val b = year % 7
        val c = year % 19
        val d = (19 * c + 15) % 30
        val e = (2 * a + 4 * b - d + 34) % 7
        val month = (d + e + 114) / 31
        val day = ((d + e + 114) % 31) + 1

        // The date in the Julian calendar
        val julianDate = LocalDate.of(year, month, day)

        // Gregorian offset: for 1900-2099, the offset is +13 days
        val gregorianOffset = 13L
        return julianDate.plusDays(gregorianOffset)
    }

    /**
     * Checks if the given date is in a solid week (сплошная седмица, when Wednesday and Friday are not fasting).
     */
    fun isSolidWeek(date: LocalDate, pascha: LocalDate): Boolean {
        val year = date.year
        // Святки: Jan 7 - Jan 17 (inclusive)
        if (date.monthValue == 1 && date.dayOfMonth in 7..17) return true

        val offset = ChronoUnit.DAYS.between(pascha, date)
        // Седмица о мытаре и фарисее: Pascha - 69 to Pascha - 63
        if (offset in -69..-63) return true

        // Сырная седмица (Масленица): Pascha - 55 to Pascha - 49
        if (offset in -55..-49) return true

        // Светлая (Пасхальная) седмица: Pascha to Pascha + 6
        if (offset in 0..6) return true

        // Троицкая седмица: Pascha + 49 to Pascha + 55
        if (offset in 49..55) return true

        return false
    }

    /**
     * Determines whether the given date falls into one of the four multi-day fasting periods.
     */
    fun getMultiDayFastName(date: LocalDate, pascha: LocalDate): String? {
        val offset = ChronoUnit.DAYS.between(pascha, date)

        // Великий Пост: Pascha - 48 (Чистый понедельник) to Pascha - 1 (Великая Суббота)
        if (offset in -48..-1) return "Великий Пост"

        // Петров Пост: Pascha + 57 (понедельник Всех Святых) to July 11 (inclusive)
        val petrovStart = pascha.plusDays(57)
        val petrovEnd = LocalDate.of(date.year, 7, 11)
        if (!date.isBefore(petrovStart) && !date.isAfter(petrovEnd)) {
            return "Петров Пост"
        }

        // Успенский Пост: Aug 14 - Aug 27
        if (date.monthValue == 8 && date.dayOfMonth in 14..27) {
            return "Успенский Пост"
        }

        // Рождественский Пост: Nov 28 - Jan 6
        if ((date.monthValue == 11 && date.dayOfMonth >= 28) ||
            (date.monthValue == 12) ||
            (date.monthValue == 1 && date.dayOfMonth <= 6)
        ) {
            return "Рождественский Пост"
        }

        return null
    }

    /**
     * Converts a modern Gregorian date to Julian calendar (Старый стиль -13 days).
     */
    fun toOldStyle(date: LocalDate): LocalDate {
        return date.minusDays(13)
    }
}
