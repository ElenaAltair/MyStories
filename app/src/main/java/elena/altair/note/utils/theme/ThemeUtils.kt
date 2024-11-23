package elena.altair.note.utils.theme

import android.content.SharedPreferences
import elena.altair.note.R


object ThemeUtils {

    fun getSelectedTheme(defPref: SharedPreferences): Int {
        return if (defPref.getString("theme_key", "blue") == "blue") {
            R.style.Base_Theme_BlueNoActionBar
        } else if (defPref.getString("theme_key", "blue") == "red") {
            R.style.Base_Theme_RedNoActionBar
        } else if (defPref.getString("theme_key", "blue") == "green") {
            R.style.Base_Theme_GreenNoActionBar
        } else if (defPref.getString("theme_key", "blue") == "orange") {
            R.style.Base_Theme_OrangeNoActionBar
        } else {
            R.style.Base_Theme_BlueNoActionBar
        }
    }

    fun getSelectedTheme2(defPref: SharedPreferences): Int {
        return if (defPref.getString("theme_key", "blue") == "blue") {
            R.style.Base_Theme_BlueWithActionBar
        } else if (defPref.getString("theme_key", "blue") == "red") {
            R.style.Base_Theme_RedWithActionBar
        } else if (defPref.getString("theme_key", "blue") == "green") {
            R.style.Base_Theme_GreenWithActionBar
        } else if (defPref.getString("theme_key", "blue") == "orange") {
            R.style.Base_Theme_OrangeWithActionBar
        } else {
            R.style.Base_Theme_BlueWithActionBar
        }
    }

    fun getSelectedTheme3(defPref: SharedPreferences): Int {
        return if (defPref.getString("theme_key", "blue") == "blue") {
            R.style.Base_Theme_BlueWithStatusBarAndNoActionBar
        } else if (defPref.getString("theme_key", "blue") == "red") {
            R.style.Base_Theme_RedWithStatusBarAndNoActionBar
        } else if (defPref.getString("theme_key", "blue") == "green") {
            R.style.Base_Theme_GreenWithStatusBarAndNoActionBar
        } else if (defPref.getString("theme_key", "blue") == "orange") {
            R.style.Base_Theme_OrangeWithStatusBarAndNoActionBar
        } else {
            R.style.Base_Theme_BlueWithStatusBarAndNoActionBar
        }
    }
}