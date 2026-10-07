package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferencesManager
import com.example.data.repository.OrthodoxCalendarRepository
import com.example.notification.NotificationHelper

class OrthodoxCalendarApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: OrthodoxCalendarRepository
        private set

    lateinit var preferencesManager: UserPreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "orthodox_calendar_db"
        ).fallbackToDestructiveMigration().build()

        repository = OrthodoxCalendarRepository(database.favoriteDao())
        preferencesManager = UserPreferencesManager(applicationContext)

        NotificationHelper.createNotificationChannels(this)
        NotificationHelper.scheduleDailyAlarm(this)
    }

    companion object {
        lateinit var instance: OrthodoxCalendarApplication
            private set
    }
}
