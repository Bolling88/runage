package xevenition.com.runage

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import xevenition.com.runage.dagger.AppModule
import xevenition.com.runage.dagger.ApplicationComponent
import xevenition.com.runage.dagger.DaggerApplicationComponent

class MainApplication: Application() {
    // Reference to the application graph that is used across the whole app
    lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }else{
            Timber.plant(CrashReportingTree())
        }
        FirebaseApp.initializeApp(this)
        appComponent =  DaggerApplicationComponent.builder().appModule(
            AppModule(
                this
            )
        ).build()
    }

    companion object{
        var serviceIsRunning = false
        var welcomeMessagePlayed = false
    }

    private inner class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
            if (priority == Log.ERROR) {
                if (throwable != null) {
                    FirebaseCrashlytics.getInstance().recordException(throwable)
                }
            } else return
        }
    }
}

