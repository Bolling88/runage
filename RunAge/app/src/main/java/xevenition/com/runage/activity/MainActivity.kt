package xevenition.com.runage.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.games.Games
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import xevenition.com.runage.R
import xevenition.com.runage.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val navController by lazy {
        Navigation.findNavController(this, R.id.nav_host_fragment)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

        MobileAds.initialize(this) { }

        firebaseAnalytics = Firebase.analytics

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.navigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            firebaseAnalytics.setCurrentScreen(this, destination.displayName, null /* class override */)
        }
        binding.navigation.setNavigationItemSelectedListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            when(it.itemId){
                R.id.leaderboard ->{
                    Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                        .allLeaderboardsIntent
                        .addOnSuccessListener { intent ->
                            startActivityForResult(
                                intent,
                                RC_LEADERBOARD_UI
                            )
                        }
                }
                R.id.achievementNavigation ->{
                    Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                        .achievementsIntent
                        .addOnSuccessListener { intent ->
                            startActivityForResult(
                                intent,
                                RC_ACHIEVEMENT_UI
                            )
                        }
                }
                R.id.mainNavigation ->{
                    this.findNavController(R.id.nav_host_fragment).navigate(R.id.mainNavigation)
                }
                R.id.appSettingsNavigation ->{
                    this.findNavController(R.id.nav_host_fragment).navigate(R.id.appSettingsNavigation)
                }
                R.id.appRulesNavigation ->{
                    this.findNavController(R.id.nav_host_fragment).navigate(R.id.appRulesNavigation)
                }
                R.id.appSupportNavigation ->{
                    this.findNavController(R.id.nav_host_fragment).navigate(R.id.appSupportNavigation)
                }
            }
            true
        }
    }

    fun openMenu() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    fun lockDrawer(){
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    fun unlockDrawer(){
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    fun setUpAchievementsLayout(googleSignInAccount: GoogleSignInAccount){
        Games.getGamesClient(this, googleSignInAccount)
            .setGravityForPopups(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
        val gamesClient = Games.getGamesClient(this@MainActivity, googleSignInAccount)
        gamesClient.setViewForPopups(findViewById(R.id.container_pop_up))
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    companion object{
        private const val RC_LEADERBOARD_UI = 9004
        const val RC_ACHIEVEMENT_UI = 9003
    }
}
