package xevenition.com.runage.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import xevenition.com.runage.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)
        val navMenu = findViewById<NavigationView>(R.id.nav_view)
        navMenu.setupWithNavController(navController)
        navMenu.setNavigationItemSelectedListener {
            when (it.itemId) {

            }
            true
        }
    }

    companion object {
        const val MY_PERMISSIONS_REQUEST = 3242
    }
}
