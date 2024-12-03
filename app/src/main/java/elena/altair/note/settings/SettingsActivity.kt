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
import elena.altair.note.constants.MyConstants.BUTTON_SIZE_DEFAULT
import elena.altair.note.constants.MyConstants.COMMENT_SIZE_DEFAULT
import elena.altair.note.constants.MyConstants.COMMENT_SIZE_KEY
import elena.altair.note.constants.MyConstants.CONTENT_SIZE_DEFAULT
import elena.altair.note.constants.MyConstants.CONTENT_SIZE_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_BUTTON_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_COMMENT_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_CONTENT_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_DEFAULT
import elena.altair.note.constants.MyConstants.FONT_FAMILY_LIST_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_TITLE_KEY
import elena.altair.note.constants.MyConstants.LIST_SIZE_DEFAULT
import elena.altair.note.constants.MyConstants.THEME_DEFAULT
import elena.altair.note.constants.MyConstants.THEME_KEY
import elena.altair.note.constants.MyConstants.TITLE_SIZE_DEFAULT
import elena.altair.note.constants.MyConstants.TITLE_SIZE_KEY
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
        currentTheme = defPref.getString(THEME_KEY, THEME_DEFAULT).toString()

        currentFontFamilyTitle =
            defPref.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT).toString()
        currentFontFamilyContent =
            defPref.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT).toString()
        currentFontFamilyComment =
            defPref.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT).toString()
        currentFontFamilyList =
            defPref.getString(FONT_FAMILY_LIST_KEY, FONT_FAMILY_DEFAULT).toString()
        currentFontFamilyButton =
            defPref.getString(FONT_FAMILY_BUTTON_KEY, FONT_FAMILY_DEFAULT).toString()
        currentTextSizeTitle = defPref.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT).toString()
        currentTextSizeContent =
            defPref.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT).toString()
        currentTextSizeComment =
            defPref.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT).toString()

        setTheme(getSelectedTheme(defPref))

        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root) //R.layout.activity_settings
        // подключим наш собсвенный Action Bar к нашему активити
        setSupportActionBar(binding.toolbar)

        val font: Typeface? = typeface(
            defPref.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@SettingsActivity
        )
        setTitleActionBar(resources.getString(R.string.settings), font, supportActionBar)

        binding.idTestTitle.setTypeface(
            defPref.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@SettingsActivity
        )
        binding.idTestTitle.setTextSize(defPref.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT))
        binding.idTestContent.setTypeface(
            defPref.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@SettingsActivity
        )
        binding.idTestContent.setTextSize(defPref.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        binding.idTestComment.setTypeface(
            defPref.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@SettingsActivity
        )
        binding.idTestComment.setTextSize(defPref.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        binding.idTestList.setTypeface(
            defPref.getString(FONT_FAMILY_LIST_KEY, FONT_FAMILY_DEFAULT),
            this@SettingsActivity
        )
        binding.idTestList.setTextSize(LIST_SIZE_DEFAULT)
        binding.idTestButton.setTypeface(
            defPref.getString(FONT_FAMILY_BUTTON_KEY, FONT_FAMILY_DEFAULT),
            this@SettingsActivity
        )
        binding.idTestButton.setTextSize(BUTTON_SIZE_DEFAULT)


        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.placeHolder, SettingsFragment())
                .commit()
        }



        listener = OnSharedPreferenceChangeListener { defValue, key ->
            if (defPref.getString(THEME_KEY, THEME_DEFAULT) != currentTheme
                || defPref.getString(
                    FONT_FAMILY_TITLE_KEY,
                    FONT_FAMILY_DEFAULT
                ) != currentFontFamilyTitle
                || defPref.getString(
                    FONT_FAMILY_CONTENT_KEY,
                    FONT_FAMILY_DEFAULT
                ) != currentFontFamilyContent
                || defPref.getString(
                    FONT_FAMILY_COMMENT_KEY,
                    FONT_FAMILY_DEFAULT
                ) != currentFontFamilyComment
                || defPref.getString(
                    FONT_FAMILY_LIST_KEY,
                    FONT_FAMILY_DEFAULT
                ) != currentFontFamilyList
                || defPref.getString(
                    FONT_FAMILY_BUTTON_KEY,
                    FONT_FAMILY_DEFAULT
                ) != currentFontFamilyButton
                || defPref.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT) != currentTextSizeTitle
                || defPref.getString(
                    CONTENT_SIZE_KEY,
                    CONTENT_SIZE_DEFAULT
                ) != currentTextSizeContent
                || defPref.getString(
                    COMMENT_SIZE_KEY,
                    COMMENT_SIZE_DEFAULT
                ) != currentTextSizeComment
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