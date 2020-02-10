package xevenition.com.runage.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import xevenition.com.runage.R
import javax.inject.Inject

class SaveUtil @Inject constructor(app: Application) {

    private var sharedPref: SharedPreferences

    init {
        sharedPref = app.getSharedPreferences(
            KEY, Context.MODE_PRIVATE
        )
    }

    fun save(key: String, value: Boolean){
        with (sharedPref.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    companion object {
        const val KEY_INITIAL_SETTINGS_COMPLETED = "key_initial_settings_completed"

        private const val KEY = "evenition.com.runage.SAVE_KEY"
    }
}