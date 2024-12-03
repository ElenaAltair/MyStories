package elena.altair.note.activities.books

import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import elena.altair.note.R
import elena.altair.note.constants.MyConstants.COMMENT_SIZE_DEFAULT
import elena.altair.note.constants.MyConstants.COMMENT_SIZE_KEY
import elena.altair.note.constants.MyConstants.CONTENT_SIZE_DEFAULT
import elena.altair.note.constants.MyConstants.CONTENT_SIZE_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_BUTTON_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_COMMENT_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_CONTENT_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_DEFAULT
import elena.altair.note.constants.MyConstants.FONT_FAMILY_TITLE_KEY
import elena.altair.note.databinding.ActivityProfileBinding
import elena.altair.note.dialoghelper.DialogInfo.createDialogInfo
import elena.altair.note.etities.ProfileEntity2
import elena.altair.note.model.DbManager
import elena.altair.note.utils.font.TypefaceUtils.setTitleActionBar
import elena.altair.note.utils.font.TypefaceUtils.typeface
import elena.altair.note.utils.font.setTextSize
import elena.altair.note.utils.font.setTypeface
import elena.altair.note.utils.theme.ThemeUtils.getSelectedTheme
import elena.altair.note.viewmodel.MainViewModel

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var defPref: SharedPreferences
    private lateinit var binding: ActivityProfileBinding
    private val mainViewModel: MainViewModel by viewModels()
    private val dbManager = DbManager()
    private var profile: ProfileEntity2? = null

    override fun onCreate(savedInstanceState: Bundle?) {


        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme(defPref))

        super.onCreate(savedInstanceState)


        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // подключим наш собсвенный Action Bar к нашему активити
        setSupportActionBar(binding.toolbar)
        setTextSize()
        setFontFamily()
        observer()


        // активируем стрелку на верхнем меню
        actionBarSetting()

        binding.twAkk.text = dbManager.auth.currentUser?.email


        binding.bSave.setOnClickListener {

            mainViewModel.insertProfile(createNewProfile())

            createDialogInfo(resources.getString(R.string.changes_saved), this)

        }



    }

    private fun createNewProfile(): ProfileEntity2 {
        var login = dbManager.auth.currentUser?.email
        if (profile != null)
            login = profile!!.loginAuthor

        return ProfileEntity2(
            profile?.id,
            login!!,
            binding.edName1.text.toString().trim(),
            binding.edName2.text.toString().trim(),
            binding.edName3.text.toString().trim(),

            )
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

    private fun observer() {
        mainViewModel.getProfile(dbManager.auth.currentUser?.email.toString())
            .observe(this, Observer {
                if (it != null) {
                    profile = it
                    binding.edName1.setText(profile!!.nameAuthor1)
                    binding.edName2.setText(profile!!.nameAuthor2)
                    binding.edName3.setText(profile!!.nameAuthor3)
                }
            })
    }

    private fun setTextSize() = with(binding) {
        twAkk.setTextSize(defPref.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edName1.setTextSize(defPref.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edName2.setTextSize(defPref.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edName3.setTextSize(defPref.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))

        tw1.setTextSize(defPref.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        tw2.setTextSize(defPref.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        tw3.setTextSize(defPref.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        tw4.setTextSize(defPref.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        tw5.setTextSize(defPref.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
    }

    private fun setFontFamily() = with(binding) {
        twAkk.setTypeface(
            defPref.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@ProfileActivity
        )
        edName1.setTypeface(
            defPref.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@ProfileActivity
        )
        edName2.setTypeface(
            defPref.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@ProfileActivity
        )
        edName3.setTypeface(
            defPref.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@ProfileActivity
        )


        tw1.setTypeface(
            defPref.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@ProfileActivity
        )
        tw2.setTypeface(
            defPref.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@ProfileActivity
        )
        tw3.setTypeface(
            defPref.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@ProfileActivity
        )
        tw4.setTypeface(
            defPref.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@ProfileActivity
        )
        tw5.setTypeface(
            defPref.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@ProfileActivity
        )


        val font: Typeface? = typeface(
            defPref.getString(FONT_FAMILY_TITLE_KEY , FONT_FAMILY_DEFAULT),
            this@ProfileActivity
        )
        setTitleActionBar(resources.getString(R.string.app_name), font, supportActionBar)

        bSave.setTypeface(
            defPref.getString(FONT_FAMILY_BUTTON_KEY, FONT_FAMILY_DEFAULT),
            this@ProfileActivity
        )

    }
}