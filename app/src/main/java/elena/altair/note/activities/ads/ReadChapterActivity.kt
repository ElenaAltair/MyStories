package elena.altair.note.activities.ads

import android.annotation.SuppressLint
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
import elena.altair.note.constants.MyConstants.CONTENT_SIZE_DEFAULT
import elena.altair.note.constants.MyConstants.CONTENT_SIZE_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_CONTENT_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_DEFAULT
import elena.altair.note.constants.MyConstants.FONT_FAMILY_TITLE_KEY
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
        menuInflater.inflate(R.menu.fon_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { // нажимаем на кнопку стрелка на верхнем меню
            finish()
        } else if (item.itemId == R.id.b_star) {
            binding.main.setBackgroundResource(R.color.black)
            binding.toolbar.setBackgroundResource(R.color.blue_dark_dark4)
            binding.chapterName.setBackgroundResource(R.color.blue_dark_dark4)
            binding.bookName.setBackgroundResource(R.color.blue_dark_dark4)
            binding.cvRead.setBackgroundResource(R.color.black)

            binding.chapterName.setTextColor(resources.getColor(R.color.blue_dark, resources.newTheme()))
            binding.bookName.setTextColor(resources.getColor(R.color.blue_dark, resources.newTheme()))
            binding.content.setTextColor(resources.getColor(R.color.blue_dark, resources.newTheme()))
        } else if (item.itemId == R.id.b_moon) {
            binding.main.setBackgroundResource(R.color.purple_500)
            binding.toolbar.setBackgroundResource(R.color.purple_700)
            binding.chapterName.setBackgroundResource(R.color.purple_700)
            binding.bookName.setBackgroundResource(R.color.purple_700)
            binding.cvRead.setBackgroundResource(R.color.purple_500)

            binding.chapterName.setTextColor(resources.getColor(R.color.blue_dark, resources.newTheme()))
            binding.bookName.setTextColor(resources.getColor(R.color.blue_dark, resources.newTheme()))
            binding.content.setTextColor(resources.getColor(R.color.blue_dark, resources.newTheme()))
        } else if (item.itemId == R.id.b_sun) {
            binding.main.setBackgroundResource(R.color.orange_light_bg)
            binding.toolbar.setBackgroundResource(R.color.orange_light_bg2)
            binding.chapterName.setBackgroundResource(R.color.orange_light_bg2)
            binding.bookName.setBackgroundResource(R.color.orange_light_bg2)
            binding.cvRead.setBackgroundResource(R.color.orange_light_bg)

            binding.chapterName.setTextColor(resources.getColor(R.color.red_dark_dark4, resources.newTheme()))
            binding.bookName.setTextColor(resources.getColor(R.color.red_dark_dark4, resources.newTheme()))
            binding.content.setTextColor(resources.getColor(R.color.black, resources.newTheme()))
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
        content.setTextSize(defPref.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {
        bookName.setTypeface(
            defPref.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@ReadChapterActivity
        )
        chapterName.setTypeface(
            defPref.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@ReadChapterActivity
        )
        content.setTypeface(
            defPref.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@ReadChapterActivity
        )

        val font: Typeface? = typeface(
            defPref.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@ReadChapterActivity
        )
        setTitleActionBar(resources.getString(R.string.app_name), font, supportActionBar)

    }
}