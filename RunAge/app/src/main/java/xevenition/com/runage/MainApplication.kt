package xevenition.com.runage

import android.app.Application
import dagger.Component
import timber.log.Timber
import xevenition.com.runage.architecture.AppModule
import xevenition.com.runage.fragment.main.MainFragment
import xevenition.com.runage.fragment.map.MapFragment
import xevenition.com.runage.fragment.start.StartFragment
import xevenition.com.runage.service.EventService
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface ApplicationComponent {
    fun inject(component: StartFragment)
    fun inject(component: MapFragment)
    fun inject(component: MainFragment)
    fun inject(component: EventService)
}

class MainApplication: Application() {
    // Reference to the application graph that is used across the whole app
    val appComponent =  DaggerApplicationComponent.builder().appModule(AppModule(this)).build()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}