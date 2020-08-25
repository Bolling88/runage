package xevenition.com.runage.dagger

import dagger.Component
import xevenition.com.runage.fragment.appsettings.AppSettingsViewModelFactory
import xevenition.com.runage.fragment.challenges.QuestsFragment
import xevenition.com.runage.fragment.challenges.ChallengesViewModelFactory
import xevenition.com.runage.fragment.feed.FeedViewModelFactory
import xevenition.com.runage.fragment.gifted.GiftedViewModelFactory
import xevenition.com.runage.fragment.history.HistoryFragment
import xevenition.com.runage.fragment.history.HistoryViewModelFactory
import xevenition.com.runage.fragment.historysummary.HistorySummaryFragment
import xevenition.com.runage.fragment.historysummary.HistorySummaryViewModelFactory
import xevenition.com.runage.fragment.historysummarypath.HistorySummaryPathFragment
import xevenition.com.runage.fragment.historysummarypath.HistorySummaryPathViewModelFactory
import xevenition.com.runage.fragment.viewpage.ViewPageFragment
import xevenition.com.runage.fragment.viewpage.ViewPageViewModelFactory
import xevenition.com.runage.fragment.login.LoginViewModelFactory
import xevenition.com.runage.fragment.main.MainFragment
import xevenition.com.runage.fragment.main.MainViewModelFactory
import xevenition.com.runage.fragment.map.MapFragment
import xevenition.com.runage.fragment.map.MapViewModelFactory
import xevenition.com.runage.fragment.path.PathFragment
import xevenition.com.runage.fragment.path.PathViewModelFactory
import xevenition.com.runage.fragment.permission.PermissionFragment
import xevenition.com.runage.fragment.permission.PermissionViewModelFactory
import xevenition.com.runage.fragment.player.PlayerFragment
import xevenition.com.runage.fragment.player.PlayerViewModelFactory
import xevenition.com.runage.fragment.present.PresentViewModelFactory
import xevenition.com.runage.fragment.profile.ProfileFragment
import xevenition.com.runage.fragment.profile.ProfileViewModelFactory
import xevenition.com.runage.fragment.requirement.RequirementViewModelFactory
import xevenition.com.runage.fragment.rule.RuleViewModelFactory
import xevenition.com.runage.fragment.search.SearchFragment
import xevenition.com.runage.fragment.search.SearchViewModelFactory
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
import xevenition.com.runage.fragment.support.SupportFragment
import xevenition.com.runage.fragment.support.SupportViewModelFactory
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
    fun inject(component: QuestsFragment)
    fun inject(component: PlayerFragment)
    fun inject(component: SearchFragment)
    fun inject(component: SplashFragment)
    fun inject(component: SupportFragment)
    fun inject(component: HistoryFragment)
    fun inject(component: SummaryFragment)
    fun inject(component: ProfileFragment)
    fun inject(component: ViewPageFragment)
    fun inject(component: SettingsFragment)
    fun inject(component: PermissionFragment)
    fun inject(component: XpViewModelFactory)
    fun inject(component: MapViewModelFactory)
    fun inject(component: FeedViewModelFactory)
    fun inject(component: PathViewModelFactory)
    fun inject(component: RuleViewModelFactory)
    fun inject(component: MainViewModelFactory)
    fun inject(component: StartViewModelFactory)
    fun inject(component: LoginViewModelFactory)
    fun inject(component: ShareViewModelFactory)
    fun inject(component: SearchViewModelFactory)
    fun inject(component: GiftedViewModelFactory)
    fun inject(component: HistorySummaryFragment)
    fun inject(component: SplashViewModelFactory)
    fun inject(component: PlayerViewModelFactory)
    fun inject(component: PresentViewModelFactory)
    fun inject(component: ProfileViewModelFactory)
    fun inject(component: SummaryViewModelFactory)
    fun inject(component: SupportViewModelFactory)
    fun inject(component: HistoryViewModelFactory)
    fun inject(component: SettingsViewModelFactory)
    fun inject(component: ViewPageViewModelFactory)
    fun inject(component: PermissionViewModelFactory)
    fun inject(component: ChallengesViewModelFactory)
    fun inject(component: HistorySummaryPathFragment)
    fun inject(component: AppSettingsViewModelFactory)
    fun inject(component: RequirementViewModelFactory)
    fun inject(component: HistorySummaryViewModelFactory)
    fun inject(component: HistorySummaryPathViewModelFactory)
}