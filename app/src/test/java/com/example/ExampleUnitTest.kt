package com.example

import com.example.data.model.FastingRule
import com.example.data.model.FeastType
import com.example.data.repository.OrthodoxCalendarCalculator
import com.example.data.repository.OrthodoxFeastRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {
    @Test
    fun paschaCalculation_2026_isCorrect() {
        // In 2026, Orthodox Easter (Pascha) is on April 12, 2026
        val pascha2026 = OrthodoxCalendarCalculator.calculatePascha(2026)
        assertEquals(LocalDate.of(2026, 4, 12), pascha2026)
    }

    @Test
    fun paschaCalculation_2025_isCorrect() {
        // In 2025, Orthodox Easter was on April 20, 2025
        val pascha2025 = OrthodoxCalendarCalculator.calculatePascha(2025)
        assertEquals(LocalDate.of(2025, 4, 20), pascha2025)
    }

    @Test
    fun oldStyleCalculation_isMinus13Days() {
        val gregorian = LocalDate.of(2026, 1, 7)
        val oldStyle = OrthodoxCalendarCalculator.toOldStyle(gregorian)
        assertEquals(LocalDate.of(2025, 12, 25), oldStyle)
    }

    @Test
    fun fixedFeasts_containsTwelveMajorFeasts() {
        val feasts = OrthodoxFeastRegistry.fixedFeasts
        assertTrue(feasts.any { it.title.contains("Рождество Господа") })
        assertTrue(feasts.any { it.title.contains("Крещение") })
        assertTrue(feasts.any { it.title.contains("Благовещение") })
        assertTrue(feasts.any { it.title.contains("Преображение") })
        assertTrue(feasts.any { it.title.contains("Успение") })
        assertTrue(feasts.any { it.title.contains("Покров") })
        assertTrue(feasts.any { it.title.contains("Николая") })
    }

    @Test
    fun movableFeasts_containsPaschaAndPentecost() {
        val movables = OrthodoxFeastRegistry.movableFeasts
        assertTrue(movables.any { it.feastType == FeastType.PASCHA })
        assertTrue(movables.any { it.title.contains("Троицы") })
        assertTrue(movables.any { it.title.contains("Вознесение") })
        assertTrue(movables.any { it.title.contains("Вход Господень") })
    }
}
