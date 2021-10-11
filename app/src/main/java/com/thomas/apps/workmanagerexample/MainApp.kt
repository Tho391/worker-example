package com.thomas.apps.workmanagerexample

import android.app.Application
import com.thomas.apps.workmanagerexample.NotificationUtils.createLogNotificationChannel
import com.thomas.apps.workmanagerexample.NotificationUtils.createLogNotificationChannelGroup
import timber.log.Timber

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()

        setUpTimber()

        createLogNotificationChannelGroup()
        createLogNotificationChannel()

    }

    private fun setUpTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}