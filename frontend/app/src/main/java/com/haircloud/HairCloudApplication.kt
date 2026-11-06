package com.haircloud

import android.app.Application
import com.haircloud.data.SessionManager

class HairCloudApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
    }
}
