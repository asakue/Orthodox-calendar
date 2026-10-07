package com.example.data.model

import java.time.LocalDate

enum class FeastType(val displayName: String, val level: Int) {
    PASCHA("Пасха — Светлое Христово Воскресение", 1),
    TWELVE_GREAT("Двунадесятый праздник", 2),
    GREAT("Великий праздник", 3),
    THEOTOKOS("Богородичный праздник", 4),
    LORD_FEAST("Господский праздник", 4),
    SAINT_DAY("День памяти святых", 5),
    MEMORIAL_SATURDAY("Родительская суббота", 6),
    FAST_PERIOD("Многодневный пост", 7),
    SOLID_WEEK("Сплошная седмица", 8)
}

enum class FastingRule(val title: String, val shortDesc: String, val foodIcon: String) {
    NO_FAST("Поста нет", "Разрешается любая пища (скоромная)", "🍖"),
    FISH_ALLOWED("Пост с рыбой", "Разрешается рыба, растительное масло и вино", "🐟"),
    OIL_ALLOWED("Пост с маслом", "Горячая пища с растительным маслом и вино", "🫒"),
    WITHOUT_OIL("Пост без масла", "Горячая пища без растительного масла", "🍲"),
    DRY_EATING("Сухоядение", "Хлеб, вода, свежие, сушеные или моченые плоды", "🍞"),
    STRICT_FAST("Строгий пост", "Воздержание от пищи до первой звезды или вечера", "✝️"),
    CHEESE_ALLOWED("Масленица", "Разрешаются молочные продукты, яйца, рыба (без мяса)", "🧀")
}

data class OrthodoxHoliday(
    val id: String,
    val title: String,
    val subtitle: String,
    val dateMonth: Int = 0, // 1..12, 0 if movable
    val dateDay: Int = 0,   // 1..31, 0 if movable
    val isMovable: Boolean = false,
    val paschaOffset: Int = 0, // days relative to Pascha
    val feastType: FeastType,
    val fastingRuleOverride: FastingRule? = null,
    val description: String,
    val history: String,
    val spiritualMeaning: String,
    val gospelReading: String,
    val apostleReading: String = "",
    val troparion: String,
    val troparionTone: String = "Глас 4",
    val kontakion: String,
    val kontakionTone: String = "Глас 8",
    val prayer: String,
    val magnification: String = "",
    val iconName: String = "orthodox_banner"
)

data class OrthodoxDayInfo(
    val date: LocalDate,
    val oldStyleDate: LocalDate,
    val mainFeast: OrthodoxHoliday?,
    val allFeasts: List<OrthodoxHoliday>,
    val saints: List<String>,
    val fastingRule: FastingRule,
    val fastingDescription: String,
    val isGreatFeast: Boolean,
    val gospelReading: String,
    val apostleReading: String,
    val troparion: String,
    val kontakion: String,
    val prayer: String,
    val dayNote: String = ""
)

data class PrayerItem(
    val id: String,
    val title: String,
    val category: String,
    val targetFeastOrSaint: String,
    val text: String,
    val translation: String = "",
    val troparion: String = "",
    val kontakion: String = "",
    val commentary: String = ""
)

enum class AppThemeMode(val title: String) {
    SYSTEM("Как в системе"),
    LIGHT("Светлая"),
    DARK("Тёмная (Ночной режим)"),
    PARCHMENT("Старинный пергамент")
}

enum class ReadingFontSize(val title: String, val scale: Float) {
    SMALL("Маленький", 0.9f),
    NORMAL("Стандартный", 1.0f),
    LARGE("Крупный", 1.15f),
    EXTRA_LARGE("Очень крупный", 1.35f)
}
