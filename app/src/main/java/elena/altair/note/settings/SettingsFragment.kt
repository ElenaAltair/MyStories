package elena.altair.note.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import elena.altair.note.R
import elena.altair.note.fragments.books.FragmentManager.currentFlag


class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        if (currentFlag?.toString()!!
                .contains("MainListFragment")
        ) {
            setPreferencesFromResource(R.xml.settings_preference, rootKey)
        } else {
            setPreferencesFromResource(R.xml.settings_preference2, rootKey)
        }
    }
}