package elena.altair.note.settings

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences


open class SingletonHolder<out T: Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}
// Class
class AppPreferences private constructor(context: Context) {

    private val sharedPreferences : SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE)
    }

    companion object : SingletonHolder<AppPreferences, Context>(::AppPreferences) {
        private const val EMPTY_STRING = ""

        private const val FIRST_TIME_KEY = "firstTime"
        private const val PASSWORD_SETTINGS_KEY = "passwordSettings"
        private const val BACK_BUTTON_KEY = "backButtonEnabling"
        private const val BRIGHTNESS_CONTROL_KEY = "brightnessControlValue"

        private const val APP_SHARED_PREFS = "MyPrefs"

        private const val DEFAULT_BRIGHTNESS = 100
    }

    /**
     * Get or set a value if the application is starting for the first time ever
     */
    var firstTimeKey: Boolean
        get() = sharedPreferences.getBoolean(FIRST_TIME_KEY, true)
        set(firstTime) = sharedPreferences.edit().putBoolean(FIRST_TIME_KEY, firstTime).apply()

    /**
     * Get or set the encrypted password for hidden settings
     */
    var passwordSettingsKey: String?
        get() = sharedPreferences.getString(PASSWORD_SETTINGS_KEY, EMPTY_STRING)
        set(password) = sharedPreferences.edit().putString(PASSWORD_SETTINGS_KEY, password).apply()

    /**
     * If true, you can go back on evaluation screens, il false, you cannot modify the scores
     */
    var isBackButtonKeyEnabled: Boolean
        get() = sharedPreferences.getBoolean(BACK_BUTTON_KEY, false)
        set(mode) = sharedPreferences.edit().putBoolean(BACK_BUTTON_KEY, mode).apply()

    /**
     * Get or set the brightness value
     */
    var brightnessKey: Int
        get() = sharedPreferences.getInt(BRIGHTNESS_CONTROL_KEY, DEFAULT_BRIGHTNESS)
        set(value) = sharedPreferences.edit().putInt(BRIGHTNESS_CONTROL_KEY, value).apply()

    /**
     * Get or set the last Port value
     */
    /*
    var isWiFiDialogEnabled: Boolean
        get() = sharedPreferences.getBoolean(SHOW_WIFI_DIALOG, true)
        set(value) = sharedPreferences.edit().putBoolean(SHOW_WIFI_DIALOG, value).apply()
    */


}