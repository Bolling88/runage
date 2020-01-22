package xevenition.com.runage

import android.app.Application
import timber.log.Timber
import xevenition.com.runage.architecture.AppModule

class MainApplication: Application() {
    // Reference to the application graph that is used across the whole app
    val appComponent: ApplicationComponent =  DaggerApplicationComponent.builder().appModule(AppModule(this)).build()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}