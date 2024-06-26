package ir.example.androidsocket

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {


    companion object {
        var isAppInForeground = false
            private set
    }


    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    fun notifyAppForeground() {
        if (!isAppInForeground) {
            isAppInForeground = true
        }
    }

    fun notifyAppBackground() {
        if (isAppInForeground) {
            isAppInForeground = false
        }
    }

}