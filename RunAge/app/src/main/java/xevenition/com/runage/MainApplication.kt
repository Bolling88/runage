package xevenition.com.runage

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import xevenition.com.runage.dagger.AppModule
import xevenition.com.runage.dagger.ApplicationComponent
import xevenition.com.runage.dagger.DaggerApplicationComponent

class MainApplication : Application() {
    // Reference to the application graph that is used across the whole app
    lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
        FirebaseApp.initializeApp(this)
        appComponent = DaggerApplicationComponent.builder().appModule(
            AppModule(
                this
            )
        ).build()
    }

    companion object {
        var serviceIsRunning = false
        var welcomeMessagePlayed = false
    }

    @Suppress("PrivatePropertyName")
    class CrashlyticsTree : Timber.Tree() {

        private val KEY_PRIORITY = "priority"
        private val KEY_TAG = "tag"
        private val KEY_MESSAGE = "message"

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            when (priority) {
                Log.VERBOSE, Log.DEBUG, Log.INFO -> return

                else -> {
                    FirebaseCrashlytics.getInstance().setCustomKey(KEY_PRIORITY, priority)
                    FirebaseCrashlytics.getInstance().setCustomKey(KEY_TAG, tag ?: "")
                    FirebaseCrashlytics.getInstance().setCustomKey(KEY_MESSAGE, message)

                    if (t == null) {
                        FirebaseCrashlytics.getInstance().recordException(Exception(message))
                    } else {
                        FirebaseCrashlytics.getInstance().recordException(t)
                    }
                }
            }
        }
    }
}

