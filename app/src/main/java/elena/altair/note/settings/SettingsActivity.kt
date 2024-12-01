package elena.altair.note.settings

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import elena.altair.note.R
import elena.altair.note.databinding.ActivitySettingsBinding
import elena.altair.note.utils.font.TypefaceUtils.setTitleActionBar
import elena.altair.note.utils.font.TypefaceUtils.typeface
import elena.altair.note.utils.font.setTextSize
import elena.altair.note.utils.font.setTypeface
import elena.altair.note.utils.theme.ThemeUtils.getSelectedTheme

class SettingsActivity : AppCompatActivity() {

    private lateinit var defPref: SharedPreferences
    private var currentTheme = ""
    private var currentFontFamilyTitle = ""
    private var currentFontFamilyContent = ""
    private var currentFontFamilyComment = ""
    private var currentFontFamilyList = ""
    private var currentFontFamilyButton = ""
    private var currentTextSizeTitle = ""
    private var currentTextSizeContent = ""
    private var currentTextSizeComment = ""

    private lateinit var listener: OnSharedPreferenceChangeListener
    private lateinit var binding: ActivitySettingsBinding

    @SuppressLint("CommitTransaction", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        currentTheme = defPref.getString("theme_key", "blue").toString()

        currentFontFamilyTitle = defPref.getString("font_family_title_key", "sans-serif").toString()
        currentFontFamilyContent =
            defPref.getString("font_family_content_key", "sans-serif").toString()
        currentFontFamilyComment =
            defPref.getString("font_family_comment_key", "sans-serif").toString()
        currentFontFamilyList = defPref.getString("font_family_list_key", "sans-serif").toString()
        currentFontFamilyButton =
            defPref.getString("font_family_button_key", "sans-serif").toString()
        currentTextSizeTitle = defPref.getString("title_size_key", "18").toString()
        currentTextSizeContent = defPref.getString("content_size_key", "18").toString()
        currentTextSizeComment = defPref.getString("comments_size_key", "16").toString()

        setTheme(getSelectedTheme(defPref))

        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root) //R.layout.activity_settings
        // подключим наш собсвенный Action Bar к нашему активити
        setSupportActionBar(binding.toolbar)

        val font: Typeface? = typeface(
            defPref.getString("font_family_title_key", "sans-serif"),
            this@SettingsActivity
        )
        setTitleActionBar(resources.getString(R.string.settings), font, supportActionBar)

        binding.idTestTitle.setTypeface(
            defPref.getString("font_family_title_key", "sans-serif"),
            this@SettingsActivity
        )
        binding.idTestTitle.setTextSize(defPref.getString("title_size_key", "18"))
        binding.idTestContent.setTypeface(
            defPref.getString("font_family_content_key", "sans-serif"),
            this@SettingsActivity
        )
        binding.idTestContent.setTextSize(defPref.getString("content_size_key", "18"))
        binding.idTestComment.setTypeface(
            defPref.getString("font_family_comment_key", "sans-serif"),
            this@SettingsActivity
        )
        binding.idTestComment.setTextSize(defPref.getString("comments_size_key", "16"))
        binding.idTestList.setTypeface(
            defPref.getString("font_family_list_key", "sans-serif"),
            this@SettingsActivity
        )
        binding.idTestList.setTextSize("16")
        binding.idTestButton.setTypeface(
            defPref.getString("font_family_button_key", "sans-serif"),
            this@SettingsActivity
        )
        binding.idTestButton.setTextSize("16")


        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.placeHolder, SettingsFragment())
                .commit()
        }



        listener = OnSharedPreferenceChangeListener { defValue, key ->
            if (defPref.getString("theme_key", "blue") != currentTheme
                || defPref.getString(
                    "font_family_title_key",
                    "sans-serif"
                ) != currentFontFamilyTitle
                || defPref.getString(
                    "font_family_content_key",
                    "sans-serif"
                ) != currentFontFamilyContent
                || defPref.getString(
                    "font_family_comment_key",
                    "sans-serif"
                ) != currentFontFamilyComment
                || defPref.getString("font_family_list_key", "sans-serif") != currentFontFamilyList
                || defPref.getString(
                    "font_family_button_key",
                    "sans-serif"
                ) != currentFontFamilyButton
                || defPref.getString("title_size_key", "18") != currentTextSizeTitle
                || defPref.getString("content_size_key", "18") != currentTextSizeContent
                || defPref.getString("comments_size_key", "14") != currentTextSizeComment
            ) recreate() // эта функция пересоздает активити
        }

        defPref.registerOnSharedPreferenceChangeListener(listener)

        // покажем кнопку стрелку в верхнм меню
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // если нажали кнопку стрелку в верхнем меню
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)

    }


}