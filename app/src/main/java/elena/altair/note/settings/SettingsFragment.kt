package elena.altair.note.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import elena.altair.note.R
import elena.altair.note.activities.MainActivity.Companion.MAIN_LIST_FRAGMENT
import elena.altair.note.activities.MainActivity.Companion.currentFrag


class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {


        if (currentFrag.contains(MAIN_LIST_FRAGMENT)
        ) {
            setPreferencesFromResource(R.xml.settings_preference, rootKey)
        } else {
            setPreferencesFromResource(R.xml.settings_preference2, rootKey)
        }


    }
}
