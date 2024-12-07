package elena.altair.note.activities.ads

import android.Manifest.permission.WRITE_SETTINGS
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import elena.altair.note.settings.AppPreferences
import elena.altair.note.utils.ViewExpandCollapse.collapse
import elena.altair.note.utils.ViewExpandCollapse.expand
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
    private var isCollapsed = false

    @SuppressLint("ResourceType")
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


        AppPreferences.getInstance(this).firstTimeKey = false
        binding.apply {

            ivVisible.setOnClickListener {
                if (!isCollapsed) {
                    bookName.collapse(300)
                    cardCh.collapse(300)

                    isCollapsed = true

                    ivVisible.setBackgroundResource(R.drawable.ic_expand_more_white)

                } else {
                    bookName.expand(300)
                    cardCh.expand(300)

                    isCollapsed = false
                    ivVisible.setBackgroundResource(R.drawable.ic_expand_less_white)

                }
            }

            val hasPermissions_t = hasPermissionsToWriteSettings(this@ReadChapterActivity)
            if (hasPermissions_t) btnSettingsGrantWritePermission.visibility = View.GONE

            btnSettingsGrantWritePermission.setOnClickListener {
                val hasPermissions = hasPermissionsToWriteSettings(this@ReadChapterActivity)

                setBrightnessRegulation(hasPermissions)
                if (!hasPermissions) askForPermissionsToWriteSettings(this@ReadChapterActivity)
            }
            //skbSettingsBrightness.visibility = View.GONE
            skbSettingsBrightness.max = MAX_BRIGHTNESS
            skbSettingsBrightness.progress =
                AppPreferences.getInstance(this@ReadChapterActivity).brightnessKey
            skbSettingsBrightness.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                var brightnessProgress = 0

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    brightnessProgress =
                        map(progress, 0, MAX_BRIGHTNESS, MIN_BRIGHTNESS, MAX_BRIGHTNESS)
                    if (hasPermissionsToWriteSettings(this@ReadChapterActivity)) {
                        Settings.System.putInt(
                            this@ReadChapterActivity.contentResolver,
                            Settings.System.SCREEN_BRIGHTNESS,
                            brightnessProgress
                        )
                        // Do not write this in one-line, won't work
                        val lp = this@ReadChapterActivity.window.attributes
                        lp.screenBrightness = brightnessProgress.toFloat() / 255
                        this@ReadChapterActivity.window.attributes = lp
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    AppPreferences.getInstance(this@ReadChapterActivity).brightnessKey =
                        brightnessProgress
                }
            })
        }


    }

    private fun setBrightnessRegulation(isEnabled: Boolean) = with(binding) {
        if (isEnabled) {
            btnSettingsGrantWritePermission.visibility = View.GONE
            skbSettingsBrightness.visibility = View.VISIBLE
            Settings.System.putInt(
                this@ReadChapterActivity.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
        } else {
            btnSettingsGrantWritePermission.visibility = View.VISIBLE
            //skbSettingsBrightness.visibility = View.GONE
        }
    }

    private fun askForPermissionsToWriteSettings(context: Activity) {
        if (!hasPermissionsToWriteSettings(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + context.packageName)
                startActivityForResult(intent, WRITE_SETTINGS_PERMISSION)
            } else {
                this.requestPermissions(arrayOf(WRITE_SETTINGS), WRITE_SETTINGS_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            WRITE_SETTINGS_PERMISSION -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, resources.getString(R.string.permission_denied_2), Toast.LENGTH_LONG).show()
                } else {
                    binding.btnSettingsGrantWritePermission.visibility = View.GONE
                    binding.skbSettingsBrightness.visibility = View.VISIBLE
                }
            }
        }
    }

    // Map fun
    private fun map(
        currentValue: Int,
        inputMin: Int,
        inputMax: Int,
        outputMin: Int,
        outputMax: Int
    ): Int {
        return (currentValue - inputMin) * (outputMax - outputMin) / (inputMax - inputMin) + outputMin
    }

    private fun hasPermissionsToWriteSettings(context: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            ContextCompat.checkSelfPermission(
                context,
                WRITE_SETTINGS
            ) == PackageManager.PERMISSION_GRANTED
        }
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
            binding.cvRead.setCardBackgroundColor(resources.getColor(R.color.black, null))
            binding.toolbar.setBackgroundResource(R.color.picker_grey_n)
            binding.chapterName.setBackgroundResource(R.color.picker_grey_n)
            binding.clTitle.setBackgroundResource(R.color.picker_grey_nd)
            binding.btnSettingsGrantWritePermission.setBackgroundResource(R.drawable.edit_button_bg_grey)

            binding.chapterName.setTextColor(resources.getColor(R.color.grey, resources.newTheme()))
            binding.bookName.setTextColor(resources.getColor(R.color.grey, resources.newTheme()))
            binding.content.setTextColor(
                resources.getColor(
                    R.color.picker_grey,
                    resources.newTheme()
                )
            )
        } else if (item.itemId == R.id.b_moon) {
            binding.main.setBackgroundResource(R.color.black)
            binding.cvRead.setCardBackgroundColor(resources.getColor(R.color.purple_500, null))
            binding.toolbar.setBackgroundResource(R.color.purple_700)
            binding.chapterName.setBackgroundResource(R.color.purple_700)
            binding.clTitle.setBackgroundResource(R.color.purple_500)
            binding.btnSettingsGrantWritePermission.setBackgroundResource(R.drawable.edit_button_bg_purple)
            //binding.cvRead.setBackgroundResource(R.color.purple_500)

            binding.chapterName.setTextColor(resources.getColor(R.color.grey, resources.newTheme()))
            binding.bookName.setTextColor(resources.getColor(R.color.grey, resources.newTheme()))
            binding.content.setTextColor(
                resources.getColor(
                    R.color.purple_200,
                    resources.newTheme()
                )
            )
        } else if (item.itemId == R.id.b_sun) {
            binding.main.setBackgroundResource(R.color.orange_light_bg2)
            binding.cvRead.setCardBackgroundColor(resources.getColor(R.color.orange_light_bg, null))
            binding.toolbar.setBackgroundResource(R.color.orange_dark_new)
            binding.chapterName.setBackgroundResource(R.color.orange_light_bg2)
            binding.clTitle.setBackgroundResource(R.color.orange_dark_new)
            binding.btnSettingsGrantWritePermission.setBackgroundResource(R.drawable.edit_button_bg_orange_light)


            binding.chapterName.setTextColor(
                resources.getColor(
                    R.color.red_dark_dark4,
                    resources.newTheme()
                )
            )
            binding.bookName.setTextColor(
                resources.getColor(
                    R.color.orange_light_bg,
                    resources.newTheme()
                )
            )
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

    companion object {
        private const val WRITE_SETTINGS_PERMISSION = 100
        private const val MAX_BRIGHTNESS = 255
        private const val MIN_BRIGHTNESS = 20
    }
}