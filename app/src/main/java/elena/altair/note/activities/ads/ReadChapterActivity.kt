package elena.altair.note.activities.ads

import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import elena.altair.note.R
import elena.altair.note.activities.ads.DescriptionActivity.Companion.CHAPTER_KEY
import elena.altair.note.databinding.ActivityReadChapterBinding
import elena.altair.note.fragments.ads.AllAdsFragment.Companion.AD_KEY
import elena.altair.note.model.Ad
import elena.altair.note.model.ChapterPublic
import elena.altair.note.utils.font.TypefaceUtils.setTitleActionBar
import elena.altair.note.utils.font.TypefaceUtils.typeface
import elena.altair.note.utils.font.setTextSize
import elena.altair.note.utils.font.setTypeface
import elena.altair.note.utils.theme.ThemeUtils.getSelectedTheme

class ReadChapterActivity : AppCompatActivity() {

    private lateinit var defPref: SharedPreferences
    private lateinit var binding: ActivityReadChapterBinding
    private var chapter: ChapterPublic? = null
    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme(defPref))

        binding = ActivityReadChapterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        // активируем стрелку на верхнем меню
        actionBarSetting()
        setTextSize()
        setFontFamily()


    }

    // активируем стрелку на верхнем меню
    private fun actionBarSetting() {
        val ab = supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { // нажимаем на кнопку стрелка на верхнем меню
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        fillViews()
        super.onStart()
    }

    private fun init() {
        getIntentFromDescriptionActivity()

        // подключим наш собсвенный Action Bar к нашему активити
        setSupportActionBar(binding.toolbar)
    }

    private fun getIntentFromDescriptionActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            chapter = intent.getSerializableExtra(CHAPTER_KEY, ChapterPublic::class.java)
            ad = intent.getSerializableExtra(AD_KEY, Ad::class.java)
        } else {
            chapter = intent.getSerializableExtra(CHAPTER_KEY) as ChapterPublic
            ad = intent.getSerializableExtra(AD_KEY) as Ad
        }
    }

    private fun fillViews() = with(binding) {
        val str = ad?.titleBook

        val k = str!!.indexOf("/")
        bookName.text = str.substring(k + 2, str.length).trim()
        //numberChapter.text = chapter?.number.toString()
        val str2 = "${chapter?.number.toString()}. ${chapter?.titleChapters.toString()}"
        chapterName.text = str2
        content.text = chapter?.context.toString()
    }

    // функция для выбора размера текста
    private fun setTextSize() = with(binding) {
        content.setTextSize(defPref.getString("content_size_key", "18"))
    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {
        bookName.setTypeface(
            defPref.getString("font_family_title_key", "sans-serif"),
            this@ReadChapterActivity
        )
        chapterName.setTypeface(
            defPref.getString("font_family_title_key", "sans-serif"),
            this@ReadChapterActivity
        )
        content.setTypeface(
            defPref.getString("font_family_content_key", "sans-serif"),
            this@ReadChapterActivity
        )

        val font: Typeface? = typeface(
            defPref.getString("font_family_title_key", "sans-serif"),
            this@ReadChapterActivity
        )
        setTitleActionBar(resources.getString(R.string.app_name), font, supportActionBar)

    }
}