package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.FastingRule
import com.example.data.model.FeastType
import com.example.data.model.OrthodoxHoliday
import com.example.data.model.PrayerItem
import com.example.ui.theme.LiturgicalCrossPurple
import com.example.ui.theme.LiturgicalFasting
import com.example.ui.theme.LiturgicalGold
import com.example.ui.theme.LiturgicalPaschaRed
import com.example.ui.theme.LiturgicalTheotokosBlue
import com.example.ui.theme.LiturgicalTrinityGreen
import com.example.ui.theme.LocalReadingFontScale
import com.example.ui.theme.OrthodoxGold
import com.example.ui.theme.OrthodoxRedPrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ScalableText(
    text: String,
    modifier: Modifier = Modifier,
    baseFontSize: TextUnit = 16.sp,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = FontFamily.Serif,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val scale = LocalReadingFontScale.current
    val calculatedSize = baseFontSize * scale
    val calculatedLineHeight = if (lineHeight != TextUnit.Unspecified) lineHeight * scale else TextUnit.Unspecified

    Text(
        text = text,
        modifier = modifier,
        fontSize = calculatedSize,
        lineHeight = calculatedLineHeight,
        color = color,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun OrthodoxOrnamentHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_orthodox_cross),
                contentDescription = "Orthodox Cross",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f))
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )

        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FastingRuleBadge(
    fastingRule: FastingRule,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val bgColor = when (fastingRule) {
        FastingRule.NO_FAST -> MaterialTheme.colorScheme.surfaceVariant
        FastingRule.FISH_ALLOWED -> Color(0xFFE1F5FE)
        FastingRule.OIL_ALLOWED -> Color(0xFFF1F8E9)
        FastingRule.WITHOUT_OIL, FastingRule.DRY_EATING, FastingRule.STRICT_FAST -> Color(0xFFFBE9E7)
        FastingRule.CHEESE_ALLOWED -> Color(0xFFFFF8E1)
    }

    val textColor = when (fastingRule) {
        FastingRule.NO_FAST -> MaterialTheme.colorScheme.onSurfaceVariant
        FastingRule.FISH_ALLOWED -> Color(0xFF0277BD)
        FastingRule.OIL_ALLOWED -> Color(0xFF33691E)
        FastingRule.WITHOUT_OIL, FastingRule.DRY_EATING, FastingRule.STRICT_FAST -> Color(0xFFC62828)
        FastingRule.CHEESE_ALLOWED -> Color(0xFFF57F17)
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fastingRule.foodIcon,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = fastingRule.title,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FeastTypeBadge(
    feastType: FeastType,
    modifier: Modifier = Modifier
) {
    val (color, name) = when (feastType) {
        FeastType.PASCHA -> Pair(LiturgicalPaschaRed, "Пасха")
        FeastType.TWELVE_GREAT -> Pair(OrthodoxRedPrimary, "Двунадесятый")
        FeastType.GREAT -> Pair(OrthodoxGold, "Великий праздник")
        FeastType.THEOTOKOS -> Pair(LiturgicalTheotokosBlue, "Богородичный")
        FeastType.LORD_FEAST -> Pair(LiturgicalGold, "Господский")
        FeastType.SAINT_DAY -> Pair(LiturgicalTrinityGreen, "Память святого")
        FeastType.MEMORIAL_SATURDAY -> Pair(LiturgicalCrossPurple, "Родительская суббота")
        FeastType.FAST_PERIOD -> Pair(LiturgicalFasting, "Постный период")
        FeastType.SOLID_WEEK -> Pair(LiturgicalTrinityGreen, "Сплошная седмица")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NearestFeastHeroCard(
    holiday: OrthodoxHoliday,
    daysUntil: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val countdownText = when (daysUntil) {
        0L -> "СЕГОДНЯ!"
        1L -> "ЗАВТРА"
        in 2L..4L -> "ЧЕРЕЗ $daysUntil ДНЯ"
        else -> "ЧЕРЕЗ $daysUntil ДНЕЙ"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("nearest_feast_card"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            OrthodoxRedPrimary,
                            Color(0xFF42060E)
                        )
                    )
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.orthodox_banner),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                alpha = 0.25f
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(OrthodoxGold.copy(alpha = 0.9f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "БЛИЖАЙШИЙ ВЕЛИКИЙ ПРАЗДНИК",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = countdownText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = holiday.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = holiday.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFDE8E8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Подробнее о празднике и молитва",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FeastItemCard(
    holiday: OrthodoxHoliday,
    date: LocalDate?,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
    val dateString = date?.format(formatter) ?: if (holiday.dateDay > 0) "${holiday.dateDay}.${holiday.dateMonth}" else "Переходящий"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("feast_item_${holiday.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_orthodox_cross),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                        FeastTypeBadge(feastType = holiday.feastType)
                    }
                }

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Избранное",
                        tint = if (isFavorite) OrthodoxGold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = holiday.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )

            if (holiday.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = holiday.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = holiday.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PrayerItemCard(
    prayer: PrayerItem,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("prayer_item_${prayer.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = prayer.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "В закладки",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = prayer.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = prayer.targetFeastOrSaint,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = prayer.text.take(180) + "...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Serif,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
