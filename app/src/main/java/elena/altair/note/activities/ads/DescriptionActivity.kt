package elena.altair.note.activities.ads

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import elena.altair.note.R
import elena.altair.note.adapters.ads.DescriptionActivityChapterRsAdapter
import elena.altair.note.databinding.ActivityDescriptionBinding
import elena.altair.note.dialoghelper.DialogInfo
import elena.altair.note.fragments.ads.AllAdsFragment
import elena.altair.note.model.Ad
import elena.altair.note.model.ChapterPublic
import elena.altair.note.utils.font.TypefaceUtils.setTitleActionBar
import elena.altair.note.utils.font.TypefaceUtils.typeface
import elena.altair.note.utils.font.setTypeface
import elena.altair.note.utils.ViewExpandCollapse.collapse
import elena.altair.note.utils.ViewExpandCollapse.expand
import elena.altair.note.utils.theme.ThemeUtils.getSelectedTheme2
import elena.altair.note.viewmodel.FirebaseViewModel

@AndroidEntryPoint
class DescriptionActivity : AppCompatActivity(), DescriptionActivityChapterRsAdapter.ChapPListener {

    private lateinit var defPref: SharedPreferences
    lateinit var binding: ActivityDescriptionBinding
    private lateinit var adapterChapter: DescriptionActivityChapterRsAdapter
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private var listChaptersPublic = ArrayList<ChapterPublic>()
    private var ad: Ad? = null
    private var isCollapsed = true

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {

        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme2(defPref))
        super.onCreate(savedInstanceState)

        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTextSize()
        setFontFamily()
        init()
        initViewModel()

        // активируем стрелку на верхнем меню
        actionBarSetting()
        binding.apply {
            ibMail.setOnClickListener { sendEmail() }

           desciption.collapse(300)


            ibExpander.setOnClickListener {
                if(!isCollapsed) {
                    desciption.collapse(300)
                    desciption5.expand(300)
                    isCollapsed = true
                    ibExpander.setBackgroundResource(R.drawable.ic_expand_more)

                } else {
                    desciption5.collapse(300)
                    desciption.expand(300)
                    isCollapsed = false
                    ibExpander.setBackgroundResource(R.drawable.ic_expand_less)

                }

            }
        }
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

    private fun sendEmail() {
        val str = ad?.titleBook
        val k = str!!.indexOf("/")

        val iSendEmail = Intent(Intent.ACTION_SEND)
        iSendEmail.type = "message/rfc822"
        iSendEmail.apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ad?.email))
            putExtra(
                Intent.EXTRA_SUBJECT,
                resources.getString(R.string.app_name) + ". " + str.substring(k + 2, str.length)
            )
            putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.text_mail))
        }
        try {
            startActivity(Intent.createChooser(iSendEmail, resources.getString(R.string.open_with)))
        } catch (e: ActivityNotFoundException) {
            DialogInfo.createDialogInfo(
                resources.getString(R.string.not_application_sending_emails),
                this
            )
        }
    }

    override fun onStart() {

        firebaseViewModel.loadAllChapters(ad?.uidOwner.toString(), ad?.key.toString())
        initRecycleViewChapter()

        super.onStart()
    }

    private fun init() {
        getIntentFromAllAdsFrag()
    }

    private fun initViewModel() {

        firebaseViewModel.liveChapterData.observe(this, Observer {
            var list = it
            if (!list.isNullOrEmpty()) {
                val sortedList = list.sortedWith(compareBy(ChapterPublic::number)).toMutableList() as ArrayList<ChapterPublic>
                list = sortedList
                listChaptersPublic = list

            }
            adapterChapter.submitList(list)
            listChaptersPublic = list

            //Log.d("MyLog", " B listChaptersPublic ${listChaptersPublic}")
        })
    }

    private fun getIntentFromAllAdsFrag() {
        ad = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(AllAdsFragment.AD_KEY, Ad::class.java)
        } else {
            intent.getSerializableExtra(AllAdsFragment.AD_KEY) as Ad
        }
        ad?.let {
            fillViews(it)
        }
    }

    private fun fillViews(ad: Ad) = with(binding) {
        val str = ad.titleBook
        val k = str!!.indexOf("/")
        tvTitle.text = str.substring(k + 2, str.length)
        ageCat.text = ad.categoryAge.toString()
        literCat.text = ad.categoryLiter.toString()
        email.text = ad.email
        desciption.text = ad.description
        desciption5.text = ad.description
        alias.text = ad.nameOwner
    }


    private fun initRecycleViewChapter() = with(binding) {

        resViewChapterAd.layoutManager = LinearLayoutManager(this@DescriptionActivity)
        adapterChapter = DescriptionActivityChapterRsAdapter(
            this@DescriptionActivity, ad!!, defPref
        )
        resViewChapterAd.adapter = adapterChapter
    }


    // функция для выбора размера текста
    private fun setTextSize() = with(binding) {
        // не хочу менять размер текста
        //literCat.setTextSize(defPref.getString("content_size_key", "18"))
        //ageCat.setTextSize(defPref.getString("content_size_key", "18"))
        //tvTitle.setTextSize(defPref.getString("title_size_key", "18"))
        //desciption.setTextSize(defPref.getString("content_size_key", "18"))
    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {
        tvTitle.setTypeface(
            defPref.getString("font_family_title_key", "sans-serif"),
            this@DescriptionActivity
        )
        literCat.setTypeface(
            defPref.getString("font_family_content_key", "sans-serif"),
            this@DescriptionActivity
        )
        ageCat.setTypeface(
            defPref.getString("font_family_content_key", "sans-serif"),
            this@DescriptionActivity
        )
        email.setTypeface(
            defPref.getString("font_family_content_key", "sans-serif"),
            this@DescriptionActivity
        )
        alias.setTypeface(
            defPref.getString("font_family_content_key", "sans-serif"),
            this@DescriptionActivity
        )
        desciption.setTypeface(
            defPref.getString("font_family_content_key", "sans-serif"),
            this@DescriptionActivity
        )
        binding.textView31.setTypeface(
            defPref.getString("font_family_comment_key", "sans-serif"),
            this@DescriptionActivity
        )

        val font: Typeface? = typeface(
            defPref.getString("font_family_title_key", "sans-serif"),
            this@DescriptionActivity
        )
        setTitleActionBar(resources.getString(R.string.app_name), font, supportActionBar)

    }

    companion object {
        const val CHAPTER_KEY = "chapter"
    }

}