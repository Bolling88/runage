package xevenition.com.runage

import android.app.Application
import dagger.Component
import xevenition.com.runage.architecture.AppModule
import xevenition.com.runage.fragment.main.MainFragment
import xevenition.com.runage.fragment.map.MapFragment
import xevenition.com.runage.fragment.start.StartFragment
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface ApplicationComponent {
    fun inject(activity: StartFragment)
    fun inject(activity: MapFragment)
    fun inject(activity: MainFragment)
}

class MainApplication: Application() {
    // Reference to the application graph that is used across the whole app
    val appComponent =  DaggerApplicationComponent.builder().appModule(AppModule(this)).build()
}