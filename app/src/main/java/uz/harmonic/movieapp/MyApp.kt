package uz.harmonic.movieapp

import android.app.Application
import android.content.Context
import com.liulishuo.filedownloader.FileDownloader
import timber.log.Timber

class MyApp : Application() {
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        appContext = this
        FileDownloader.setupOnApplicationOnCreate(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        super.onCreate()
    }
}