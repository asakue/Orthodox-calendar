package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppNavTab
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.FeastDetailScreen
import com.example.ui.screens.HolidaysScreen
import com.example.ui.screens.PrayerDetailScreen
import com.example.ui.screens.PrayersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.OrthodoxCalendarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = applicationContext as OrthodoxCalendarApplication
        val factory = MainViewModelFactory(app.repository, app.preferencesManager)

        setContent {
            val mainViewModel: MainViewModel = viewModel(factory = factory)
            val themeMode by mainViewModel.themeMode.collectAsState()
            val fontSize by mainViewModel.fontSize.collectAsState()

            OrthodoxCalendarTheme(
                themeMode = themeMode,
                readingFontSize = fontSize
            ) {
                OrthodoxAppRoot(viewModel = mainViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrthodoxAppRoot(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedHoliday by viewModel.selectedHoliday.collectAsState()
    val selectedPrayer by viewModel.selectedPrayer.collectAsState()

    if (selectedHoliday != null) {
        FeastDetailScreen(
            holiday = selectedHoliday!!,
            viewModel = viewModel,
            onBack = { viewModel.selectHoliday(null) }
        )
        return
    }

    if (selectedPrayer != null) {
        PrayerDetailScreen(
            prayer = selectedPrayer!!,
            viewModel = viewModel,
            onBack = { viewModel.selectPrayer(null) }
        )
        return
    }

    if (currentTab != AppNavTab.TODAY) {
        BackHandler {
            viewModel.selectTab(AppNavTab.TODAY)
        }
    }

    val bottomTabs = listOf(
        AppNavTab.TODAY to "Сегодня",
        AppNavTab.CALENDAR to "Календарь",
        AppNavTab.HOLIDAYS to "Праздники",
        AppNavTab.PRAYERS to "Молитвы",
        AppNavTab.FAVORITES to "Избранное"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_orthodox_cross),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .size(28.dp)
                        )
                    }
                ) {
                    AppNavTab.entries.forEach { tab ->
                        NavigationRailItem(
                            selected = currentTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = getTabIcon(tab),
                                    contentDescription = tab.title
                                )
                            },
                            label = { Text(getShortTabName(tab)) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_rail_${tab.name.lowercase()}")
                        )
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = getTopBarTitle(currentTab),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                ) { innerPadding ->
                    TabContent(
                        currentTab = currentTab,
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_orthodox_cross),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = getTopBarTitle(currentTab),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { viewModel.selectTab(AppNavTab.SETTINGS) },
                                modifier = Modifier.testTag("top_bar_settings_btn")
                            ) {
                                Icon(
                                    imageVector = if (currentTab == AppNavTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Настройки",
                                    tint = if (currentTab == AppNavTab.SETTINGS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        bottomTabs.forEach { (tab, shortName) ->
                            val isSelected = currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = getTabIcon(tab),
                                        contentDescription = shortName,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = shortName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                alwaysShowLabel = true,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_bar_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                TabContent(
                    currentTab = currentTab,
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
fun TabContent(
    currentTab: AppNavTab,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    when (currentTab) {
        AppNavTab.TODAY -> TodayScreen(
            viewModel = viewModel,
            onHolidayClick = { viewModel.selectHoliday(it) },
            onPrayerClick = { viewModel.selectPrayer(it) },
            modifier = modifier
        )
        AppNavTab.CALENDAR -> CalendarScreen(
            viewModel = viewModel,
            onHolidayClick = { viewModel.selectHoliday(it) },
            modifier = modifier
        )
        AppNavTab.HOLIDAYS -> HolidaysScreen(
            viewModel = viewModel,
            onHolidayClick = { viewModel.selectHoliday(it) },
            modifier = modifier
        )
        AppNavTab.PRAYERS -> PrayersScreen(
            viewModel = viewModel,
            onPrayerClick = { viewModel.selectPrayer(it) },
            modifier = modifier
        )
        AppNavTab.FAVORITES -> FavoritesScreen(
            viewModel = viewModel,
            onHolidayClick = { viewModel.selectHoliday(it) },
            onPrayerClick = { viewModel.selectPrayer(it) },
            modifier = modifier
        )
        AppNavTab.SETTINGS -> SettingsScreen(
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

fun getShortTabName(tab: AppNavTab): String = when (tab) {
    AppNavTab.TODAY -> "Сегодня"
    AppNavTab.CALENDAR -> "Календарь"
    AppNavTab.HOLIDAYS -> "Праздники"
    AppNavTab.PRAYERS -> "Молитвы"
    AppNavTab.FAVORITES -> "Избранное"
    AppNavTab.SETTINGS -> "Настройки"
}

fun getTopBarTitle(tab: AppNavTab): String = when (tab) {
    AppNavTab.TODAY -> "Православный Календарь"
    AppNavTab.CALENDAR -> "Церковный Календарь"
    AppNavTab.HOLIDAYS -> "Православные Праздники"
    AppNavTab.PRAYERS -> "Православный Молитвослов"
    AppNavTab.FAVORITES -> "Избранное"
    AppNavTab.SETTINGS -> "Настройки и правила"
}

fun getTabIcon(tab: AppNavTab) = when (tab) {
    AppNavTab.TODAY -> Icons.Default.Today
    AppNavTab.CALENDAR -> Icons.Default.CalendarMonth
    AppNavTab.HOLIDAYS -> Icons.Default.AutoStories
    AppNavTab.PRAYERS -> Icons.Default.MenuBook
    AppNavTab.FAVORITES -> Icons.Default.Star
    AppNavTab.SETTINGS -> Icons.Default.Settings
}
