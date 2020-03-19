package xevenition.com.runage

import android.app.Application
import com.google.firebase.FirebaseApp
import timber.log.Timber
import xevenition.com.runage.dagger.AppModule
import xevenition.com.runage.dagger.ApplicationComponent
import xevenition.com.runage.dagger.DaggerApplicationComponent

class MainApplication: Application() {
    // Reference to the application graph that is used across the whole app
    lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        FirebaseApp.initializeApp(this)
        appComponent =  DaggerApplicationComponent.builder().appModule(
            AppModule(
                this
            )
        ).build()
    }
}