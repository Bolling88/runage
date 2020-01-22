package xevenition.com.runage

import dagger.Component
import xevenition.com.runage.architecture.AppModule
import xevenition.com.runage.fragment.main.MainFragment
import xevenition.com.runage.fragment.map.MapFragment
import xevenition.com.runage.fragment.permission.PermissionFragment
import xevenition.com.runage.fragment.splash.SplashFragment
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
    fun inject(component: SplashFragment)
    fun inject(component: PermissionFragment)
}