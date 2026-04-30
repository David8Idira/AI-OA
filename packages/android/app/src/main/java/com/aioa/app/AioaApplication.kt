package com.aioa.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AioaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}