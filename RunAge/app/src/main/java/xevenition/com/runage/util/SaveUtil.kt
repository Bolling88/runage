package xevenition.com.runage.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class SaveUtil @Inject constructor(app: Application) {

    private var sharedPref: SharedPreferences

    init {
        sharedPref = app.getSharedPreferences(
            KEY, Context.MODE_PRIVATE
        )
    }

    fun saveBoolean(key: String, value: Boolean){
        with (sharedPref.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    fun saveInt(key: String, value: Int){
        with (sharedPref.edit()) {
            putInt(key, value)
            apply()
        }
    }

    fun saveLong(key: String, value: Long){
        with (sharedPref.edit()) {
            putLong(key, value)
            apply()
        }
    }


    fun saveFloat(key: String, value: Float){
        with (sharedPref.edit()) {
            putFloat(key, value)
            apply()
        }
    }

    fun getInt(key: String, default: Int = 0): Int {
        return sharedPref.getInt(key, 0)
    }

    fun getLong(key: String, default: Long = 0): Long {
        return sharedPref.getLong(key, 0)
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return sharedPref.getBoolean(key, default)
    }

    fun getFloat(key: String, default: Float = 0f): Float {
        return sharedPref.getFloat(key, default)
    }

    companion object {
        const val KEY_INITIAL_SETTINGS_COMPLETED = "key_initial_settings_completed"
        const val KEY_IS_USING_METRIC = "key_is_using_metric"
        const val KEY_WEIGHT = "key_weight"
        const val KEY_SYNC_GOOGLE_FIT = "key_sync_google_fit"
        const val KEY_APP_OPENINGS = "key_app_openings"
        const val KEY_REWARD_CLAIMED_DATE = "key_reward_claimed_date"
        const val KEY_RATED = "key_rated"
        const val KEY_CURRENT_LEVEL = "key_current_level"

        private const val KEY = "xevenition.com.runage.SAVE_KEY"
    }
}