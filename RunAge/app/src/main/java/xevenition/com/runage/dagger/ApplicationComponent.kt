package xevenition.com.runage.dagger

import dagger.Component
import xevenition.com.runage.fragment.appsettings.AppSettingsViewModelFactory
import xevenition.com.runage.fragment.history.HistoryFragment
import xevenition.com.runage.fragment.history.HistoryViewModelFactory
import xevenition.com.runage.fragment.historysummary.HistorySummaryFragment
import xevenition.com.runage.fragment.historysummary.HistorySummaryViewModelFactory
import xevenition.com.runage.fragment.historysummarypath.HistorySummaryPathFragment
import xevenition.com.runage.fragment.historysummarypath.HistorySummaryPathViewModelFactory
import xevenition.com.runage.fragment.login.LoginViewModelFactory
import xevenition.com.runage.fragment.main.MainFragment
import xevenition.com.runage.fragment.main.MainViewModelFactory
import xevenition.com.runage.fragment.map.MapFragment
import xevenition.com.runage.fragment.map.MapViewModelFactory
import xevenition.com.runage.fragment.path.PathFragment
import xevenition.com.runage.fragment.path.PathViewModelFactory
import xevenition.com.runage.fragment.permission.PermissionFragment
import xevenition.com.runage.fragment.permission.PermissionViewModelFactory
import xevenition.com.runage.fragment.rule.RuleViewModelFactory
import xevenition.com.runage.fragment.settings.SettingsFragment
import xevenition.com.runage.fragment.settings.SettingsViewModelFactory
import xevenition.com.runage.fragment.share.ShareFragment
import xevenition.com.runage.fragment.share.ShareViewModelFactory
import xevenition.com.runage.fragment.splash.SplashFragment
import xevenition.com.runage.fragment.splash.SplashViewModelFactory
import xevenition.com.runage.fragment.start.StartFragment
import xevenition.com.runage.fragment.start.StartViewModelFactory
import xevenition.com.runage.fragment.summary.SummaryFragment
import xevenition.com.runage.fragment.summary.SummaryViewModelFactory
import xevenition.com.runage.fragment.xp.XpViewModelFactory
import xevenition.com.runage.service.EventService
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface ApplicationComponent {
    fun inject(component: MapFragment)
    fun inject(component: PathFragment)
    fun inject(component: MainFragment)
    fun inject(component: EventService)
    fun inject(component: ShareFragment)
    fun inject(component: StartFragment)
    fun inject(component: SplashFragment)
    fun inject(component: HistoryFragment)
    fun inject(component: SummaryFragment)
    fun inject(component: SettingsFragment)
    fun inject(component: PermissionFragment)
    fun inject(component: XpViewModelFactory)
    fun inject(component: MapViewModelFactory)
    fun inject(component: PathViewModelFactory)
    fun inject(component: RuleViewModelFactory)
    fun inject(component: MainViewModelFactory)
    fun inject(component: StartViewModelFactory)
    fun inject(component: LoginViewModelFactory)
    fun inject(component: ShareViewModelFactory)
    fun inject(component: HistorySummaryFragment)
    fun inject(component: SplashViewModelFactory)
    fun inject(component: SummaryViewModelFactory)
    fun inject(component: HistoryViewModelFactory)
    fun inject(component: SettingsViewModelFactory)
    fun inject(component: PermissionViewModelFactory)
    fun inject(component: HistorySummaryPathFragment)
    fun inject(component: AppSettingsViewModelFactory)
    fun inject(component: HistorySummaryViewModelFactory)
    fun inject(component: HistorySummaryPathViewModelFactory)
}