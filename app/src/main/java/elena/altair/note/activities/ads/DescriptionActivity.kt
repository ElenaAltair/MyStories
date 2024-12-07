package elena.altair.note.activities.ads

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import elena.altair.note.R
import elena.altair.note.activities.MainActivity
import elena.altair.note.adapters.ads.DescriptionActivityChapterRsAdapter
import elena.altair.note.constants.MyConstants.BACKGROUND_CATS
import elena.altair.note.constants.MyConstants.BACKGROUND_DEFAULT
import elena.altair.note.constants.MyConstants.BACKGROUND_EAT
import elena.altair.note.constants.MyConstants.BACKGROUND_EMOJI
import elena.altair.note.constants.MyConstants.BACKGROUND_EMPTY
import elena.altair.note.constants.MyConstants.BACKGROUND_FLOWERS
import elena.altair.note.constants.MyConstants.BACKGROUND_HALLOWEEN
import elena.altair.note.constants.MyConstants.BACKGROUND_KEY
import elena.altair.note.constants.MyConstants.BACKGROUND_LANDSCAPE
import elena.altair.note.constants.MyConstants.BACKGROUND_LOVE
import elena.altair.note.constants.MyConstants.BACKGROUND_SCIENCE
import elena.altair.note.constants.MyConstants.BACKGROUND_SEA
import elena.altair.note.constants.MyConstants.BACKGROUND_SECRET
import elena.altair.note.constants.MyConstants.BACKGROUND_SNOW
import elena.altair.note.constants.MyConstants.BACKGROUND_STARS
import elena.altair.note.constants.MyConstants.BACKGROUND_TOYS
import elena.altair.note.constants.MyConstants.BACKGROUND_TREES
import elena.altair.note.constants.MyConstants.FONT_FAMILY_COMMENT_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_CONTENT_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_DEFAULT
import elena.altair.note.constants.MyConstants.FONT_FAMILY_TITLE_KEY
import elena.altair.note.databinding.ActivityDescriptionBinding
import elena.altair.note.databinding.ContinueDialogBinding
import elena.altair.note.dialoghelper.DialogInfo
import elena.altair.note.dialoghelper.DialogInfo.createDialogInfo
import elena.altair.note.etities.BookEntity7
import elena.altair.note.etities.ChapterEntity2
import elena.altair.note.fragments.ads.AllAdsFragment
import elena.altair.note.model.Ad
import elena.altair.note.model.ChapterPublic
import elena.altair.note.utils.ViewExpandCollapse.collapse
import elena.altair.note.utils.ViewExpandCollapse.expand
import elena.altair.note.utils.font.TypefaceUtils.setTitleActionBar
import elena.altair.note.utils.font.TypefaceUtils.typeface
import elena.altair.note.utils.font.setTypeface
import elena.altair.note.utils.settings.TimeManager.getCurrentTime
import elena.altair.note.utils.theme.ThemeUtils.getSelectedTheme
import elena.altair.note.viewmodel.FirebaseViewModel
import elena.altair.note.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DescriptionActivity : AppCompatActivity(), DescriptionActivityChapterRsAdapter.ChapPListener {

    private lateinit var defPref: SharedPreferences
    lateinit var binding: ActivityDescriptionBinding
    private lateinit var adapterChapter: DescriptionActivityChapterRsAdapter
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private var listChaptersPublic = ArrayList<ChapterPublic>()
    private var listAllChapters = ArrayList<ChapterPublic>()
    private var ad: Ad? = null
    private var isCollapsed = true
    private val mainViewModel: MainViewModel by viewModels()
    private var bookFromSqlite: BookEntity7? = null
    private var job: Job? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {

        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme(defPref))
        super.onCreate(savedInstanceState)

        val currentBackground = defPref.getString(BACKGROUND_KEY, BACKGROUND_DEFAULT).toString()

        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (currentBackground == BACKGROUND_STARS) {
            binding.llMain.setBackgroundResource(R.drawable.app_background_stars)
        } else if (currentBackground == BACKGROUND_SNOW) {
            binding.llMain.setBackgroundResource(R.drawable.app_background_snow)
        } else if (currentBackground == BACKGROUND_CATS) {
            binding.llMain.setBackgroundResource(R.drawable.app_background_cat)
        } else if (currentBackground == BACKGROUND_FLOWERS) {
            binding.llMain.setBackgroundResource(R.drawable.app_background_flowers)
        } else if (currentBackground == BACKGROUND_TREES) {
            binding.llMain.setBackgroundResource(R.drawable.app_background_trees)
        } else if (currentBackground == BACKGROUND_EMPTY) {
            binding.llMain.setBackgroundResource(R.drawable.app_background_empty)
        } else if (currentBackground == BACKGROUND_HALLOWEEN) {
            binding.llMain.setBackgroundResource(R.drawable.app_background_halloween)
        } else if (currentBackground == BACKGROUND_EMOJI) {
            binding.llMain.setBackgroundResource(R.drawable.app_background_emoji)
        } else if (currentBackground == BACKGROUND_LANDSCAPE) {
            binding.llMain.setBackgroundResource(R.drawable.app_background_landscape)
        } else if (currentBackground == BACKGROUND_EAT) {
            binding.llMain.setBackgroundResource(R.drawable.app_background_eat)
        } else if (currentBackground == BACKGROUND_TOYS) {
            binding.llMain.setBackgroundResource(R.drawable.app_background_toys)
        } else if (currentBackground == BACKGROUND_LOVE) {
            binding.llMain.setBackgroundResource(R.drawable.app_background_love)
        } else if (currentBackground == BACKGROUND_SCIENCE) {
            binding.llMain.setBackgroundResource(R.drawable.app_background_science)
        } else if (currentBackground == BACKGROUND_SEA) {
            binding.llMain.setBackgroundResource(R.drawable.app_background_sea)
        } else if (currentBackground == BACKGROUND_SECRET) {
            binding.llMain.setBackgroundResource(R.drawable.app_background_secret)
        }

        init()
        setTextSize()
        setFontFamily()
        initViewModel()

        // активируем стрелку на верхнем меню
        actionBarSetting()
        binding.apply {
            ibMail.setOnClickListener { sendEmail() }

            desciption.collapse(300)


            ibExpander.setOnClickListener {
                if (!isCollapsed) {
                    desciption.collapse(300)
                    //desciption5.expand(300)
                    desciption5.visibility = View.VISIBLE
                    isCollapsed = true
                    ibExpander.setBackgroundResource(R.drawable.ic_expand_more)

                } else {
                    //desciption5.collapse(300)
                    desciption5.visibility = View.GONE
                    desciption.expand(300)
                    isCollapsed = false
                    ibExpander.setBackgroundResource(R.drawable.ic_expand_less)

                }

            }

            if (ad?.loginOwner != MainActivity.currentUser)
                ibFromClaud.visibility = View.GONE

            ibFromClaud.setOnClickListener {
                // проверим есть ли книга на данном устройстве
                job = CoroutineScope(Dispatchers.Main).launch {
                    if (bookFromSqlite?.id == null) {
                        dialogSaveBookLocal(
                            resources.getString(R.string.downloading_from_cloud),

                            true,
                            bookFromSqlite!!
                        )

                    } else {
                        // иначе, редактируем существующую книгу
                        dialogSaveBookLocal(
                            resources.getString(R.string.downloading_from_cloud),
                            false,
                            bookFromSqlite!!
                        )
                    }
                }

            }
        }

        requestMultiplePermissions.launch(
            arrayOf(
                //CAMERA,
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE,
            )
        )
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            Log.d("MyLog", "${it.key} = ${it.value}")
        }
        if (permissions[READ_EXTERNAL_STORAGE] == true && permissions[WRITE_EXTERNAL_STORAGE] == true) {
            Log.d("MyLog", "Permission granted")

        } else {
            Log.d("MyLog", "Permission not granted")

        }
    }

    private fun dialogSaveBookLocal(
        message: String,
        new: Boolean,
        tempBook: BookEntity7
    ) {
        val builder = AlertDialog.Builder(this)
        val bindingDialog = ContinueDialogBinding.inflate(this.layoutInflater)
        val view = bindingDialog.root
        builder.setView(view)
        bindingDialog.tvMess.text = message
        bindingDialog.tvCounter.visibility = View.VISIBLE
        bindingDialog.bContinue.visibility = View.INVISIBLE
        val dialog = builder.create()

        object : CountDownTimer(10000, 1000) {

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                bindingDialog.tvCounter.text = "" + (millisUntilFinished / 1000)
            }

            override fun onFinish() {
                bindingDialog.bContinue.visibility = View.VISIBLE
            }
        }.start()


        bindingDialog.bNo.setOnClickListener {
            saveLocal = 0
            dialog?.dismiss()
        }

        bindingDialog.bContinue.setOnClickListener {
            saveLocal = 1
            if (new) {
                mainViewModel.insertBook(tempBook)
            } else {
                mainViewModel.updateBook(tempBook)
            }
            getBookByKeyFirebase(ad!!.key)


            createDialogInfo(resources.getString(R.string.book_downloaded), this)
            dialog?.dismiss()
        }
        dialog.show()
    }


    // активируем стрелку на верхнем меню
    private fun actionBarSetting() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
        // подключим наш собсвенный Action Bar к нашему активити
        setSupportActionBar(binding.toolbar)
    }

    private fun initViewModel() {

        firebaseViewModel.liveChapterData.observe(this, Observer {
            var list = it
            listChaptersPublic = list
            listAllChapters = list
            if (!list.isNullOrEmpty()) {
                val sortedList = list.sortedWith(compareBy(ChapterPublic::number))
                    .toMutableList() as ArrayList<ChapterPublic>
                list = sortedList

                listAllChapters = list

                val tempListPublic = ArrayList<ChapterPublic>()
                for (i in list.indices) {
                    if (list[i].public == "1")
                        tempListPublic.add(list[i])
                }
                listChaptersPublic = tempListPublic

            }
            adapterChapter.submitList(listChaptersPublic)

            getBookByKeyFirebase(ad!!.key)
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


    // проверим есть ли книга с данным ключом на локальном устройстве
    @SuppressLint("SuspiciousIndentation")
    private fun getBookByKeyFirebase(key: String?) {
        if (key != null)
            mainViewModel.getBookByKeyFirebase(key).observe(this, Observer {
                bookFromSqlite = createNewBookEntity(it)

                if (it != null && saveLocal == 1) {
                    for (i in listAllChapters.indices) {
                        val ch = createChapterEntity(listAllChapters[i])
                        mainViewModel.insertChapter(ch)
                    }
                    saveLocal = 0
                }
            })
    }

    // если хотим сохранить или перезаписать книгу на локальном устройстве
    private fun createNewBookEntity(book: BookEntity7?): BookEntity7 {

        var id: Long? = null
        if (book != null)
            id = book.id!!.toLong()

        var kindLiter = resources.getString(R.string.all_types)
        val epic = resources.getStringArray(R.array.epic).toMutableList() as ArrayList
        val lyrics = resources.getStringArray(R.array.lyrics).toMutableList() as ArrayList
        val drama = resources.getStringArray(R.array.drama).toMutableList() as ArrayList
        val lyricEpic = resources.getStringArray(R.array.lyric_epic).toMutableList() as ArrayList
        val genres = ad?.categoryLiter.toString()
        if (epic.contains(genres))
            kindLiter = resources.getString(R.string.epic)
        else if (lyrics.contains(genres))
            kindLiter = resources.getString(R.string.lyrics)
        else if (drama.contains(genres))
            kindLiter = resources.getString(R.string.drama)
        else if (lyricEpic.contains(genres))
            kindLiter = resources.getString(R.string.lyric_epic)

        var str = ad?.titleBook.toString()
        val k = str.indexOf("/")
        str = str.substring(k + 2, str.length)

        return BookEntity7(
            id,
            str,
            ad?.description.toString(),
            0L,
            ad?.loginOwner.toString(),
            getCurrentTime(),
            kindLiter,
            ad?.categoryLiter.toString(),
            ad?.categoryAge.toString(),
            "1",
            0L,
            "",
            0,
            ad?.key.toString(),
            ad?.nameOwner.toString(),
        )
    }

    private fun createChapterEntity(chapterPublic: ChapterPublic): ChapterEntity2 {
        return ChapterEntity2(
            chapterPublic.idLocal!!.toLong(),
            bookFromSqlite?.id!!.toLong(),
            chapterPublic.titleChapters.toString(),
            chapterPublic.shotDescribe.toString(),
            chapterPublic.context.toString(),
            getCurrentTime(),
            chapterPublic.number!!.toInt(),
            chapterPublic.public,
            0L,
            "",
            chapterPublic.key.toString(),
        )
    }


    // функция для выбора размера текста
    private fun setTextSize() = with(binding) {
        // не хочу менять размер текста
        //literCat.setTextSize(defPref.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        //ageCat.setTextSize(defPref.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        //tvTitle.setTextSize(defPref.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT))
        //desciption.setTextSize(defPref.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {
        tvTitle.setTypeface(
            defPref.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@DescriptionActivity
        )
        literCat.setTypeface(
            defPref.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@DescriptionActivity
        )
        ageCat.setTypeface(
            defPref.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@DescriptionActivity
        )
        email.setTypeface(
            defPref.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@DescriptionActivity
        )
        alias.setTypeface(
            defPref.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@DescriptionActivity
        )
        desciption.setTypeface(
            defPref.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@DescriptionActivity
        )
        binding.textView31.setTypeface(
            defPref.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@DescriptionActivity
        )

        val font: Typeface? = typeface(
            defPref.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@DescriptionActivity
        )
        setTitleActionBar(resources.getString(R.string.app_name), font, supportActionBar)

    }

    companion object {
        const val CHAPTER_KEY = "chapter"
        var saveLocal = 0
    }

}