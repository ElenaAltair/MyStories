package elena.altair.note.utils.theme

import android.content.SharedPreferences
import elena.altair.note.R
import elena.altair.note.constants.MyConstants.THEME_BLUE
import elena.altair.note.constants.MyConstants.THEME_DEFAULT
import elena.altair.note.constants.MyConstants.THEME_GREEN
import elena.altair.note.constants.MyConstants.THEME_KEY
import elena.altair.note.constants.MyConstants.THEME_ORANGE
import elena.altair.note.constants.MyConstants.THEME_RED


object ThemeUtils {

    fun getSelectedTheme(defPref: SharedPreferences): Int {
        return if (defPref.getString(THEME_KEY, THEME_DEFAULT) == THEME_BLUE) {
            R.style.Base_Theme_BlueNoActionBar
        } else if (defPref.getString(THEME_KEY, THEME_DEFAULT) == THEME_RED) {
            R.style.Base_Theme_RedNoActionBar
        } else if (defPref.getString(THEME_KEY, THEME_DEFAULT) == THEME_GREEN) {
            R.style.Base_Theme_GreenNoActionBar
        } else if (defPref.getString(THEME_KEY, THEME_DEFAULT) == THEME_ORANGE) {
            R.style.Base_Theme_OrangeNoActionBar
        } else {
            R.style.Base_Theme_BlueNoActionBar
        }
    }

    fun getSelectedTheme2(defPref: SharedPreferences): Int {
        return if (defPref.getString(THEME_KEY, THEME_DEFAULT) == THEME_BLUE) {
            R.style.Base_Theme_BlueWithActionBar
        } else if (defPref.getString(THEME_KEY, THEME_DEFAULT) == THEME_RED) {
            R.style.Base_Theme_RedWithActionBar
        } else if (defPref.getString(THEME_KEY, THEME_DEFAULT) == THEME_GREEN) {
            R.style.Base_Theme_GreenWithActionBar
        } else if (defPref.getString(THEME_KEY, THEME_DEFAULT) == THEME_ORANGE) {
            R.style.Base_Theme_OrangeWithActionBar
        } else {
            R.style.Base_Theme_BlueWithActionBar
        }
    }

    fun getSelectedTheme3(defPref: SharedPreferences): Int {
        return if (defPref.getString(THEME_KEY, THEME_DEFAULT) == THEME_BLUE) {
            R.style.Base_Theme_BlueWithStatusBarAndNoActionBar
        } else if (defPref.getString(THEME_KEY, THEME_DEFAULT) == THEME_RED) {
            R.style.Base_Theme_RedWithStatusBarAndNoActionBar
        } else if (defPref.getString(THEME_KEY, THEME_DEFAULT) == THEME_GREEN) {
            R.style.Base_Theme_GreenWithStatusBarAndNoActionBar
        } else if (defPref.getString(THEME_KEY, THEME_DEFAULT) == THEME_ORANGE) {
            R.style.Base_Theme_OrangeWithStatusBarAndNoActionBar
        } else {
            R.style.Base_Theme_BlueWithStatusBarAndNoActionBar
        }
    }
}