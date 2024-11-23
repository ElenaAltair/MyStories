package elena.altair.note.activities.ads

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
//import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import elena.altair.note.R
import elena.altair.note.activities.MainActivity.Companion.ADS_DATA
import elena.altair.note.activities.MainActivity.Companion.EDIT_STATE_AD
import elena.altair.note.activities.MainActivity.Companion.currentUser
import elena.altair.note.adapters.ads.EditAdsActivityChapterRsAdapter
import elena.altair.note.adapters.ads.EditAdsActivityImageAdapter
import elena.altair.note.databinding.ActivityEditAdsBinding
import elena.altair.note.dialoghelper.DialogInfo.createDialogInfo
import elena.altair.note.dialoghelper.DialogSpinnerHelper
import elena.altair.note.etities.BookEntity4
import elena.altair.note.etities.ChapterEntity2
import elena.altair.note.fragments.ads.FragmentCloseInterface
import elena.altair.note.fragments.ads.ImageListFragment
import elena.altair.note.model.Ad
import elena.altair.note.model.ChapterPublic
import elena.altair.note.model.DbManager
import elena.altair.note.utils.InternetConnection.isOnline
import elena.altair.note.utils.ads.ImagePicker
import elena.altair.note.utils.font.setTextSize
import elena.altair.note.utils.font.setTypeface
import elena.altair.note.utils.text.textRedactor.HtmlManager
import elena.altair.note.utils.theme.ThemeUtils.getSelectedTheme
import elena.altair.note.viewmodel.MainViewModel


@AndroidEntryPoint
class EditAdsActivity : AppCompatActivity(), FragmentCloseInterface,
    EditAdsActivityChapterRsAdapter.ChapPListener {

    var chooseImageFrag: ImageListFragment? = null
    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: EditAdsActivityImageAdapter
    private lateinit var adapterChapter: EditAdsActivityChapterRsAdapter
    private lateinit var defPref: SharedPreferences
    private var currentTheme = ""
    private var isEditState = false // новое объявление или редактирование существующего
    private val dbManager = DbManager()
    var editImagePos = 0
    private var ad: Ad? = null
    private val mainViewModel: MainViewModel by viewModels()
    private var listBooks = ArrayList<BookEntity4>()
    private var listChapters = ArrayList<ChapterEntity2>()
    private var selectBook: BookEntity4? = null

    private var key: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        currentTheme = defPref.getString("theme_key", "blue").toString()
        setTheme(getSelectedTheme(defPref))

        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // зададим настройки текста
        setTextSize()
        setFontFamily()

        init()


        checkEditState()
        imageChangeCounter()
        isEditState = isEditState()
        getAllBooksNotPublic()


        // отслеживаем момент, когда пользователь выберет книгу для публикации
        binding.edTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

                if (isEditState) {

                } else {
                    val id = getIdSelectBook()
                    getSelectedBook(id)
                    getAllChapters()
                    binding.tvLiter.text = selectBook?.genreLiterature.toString().trim()
                    binding.tvAge.text = selectBook?.ageCat.toString().trim()
                    binding.edDescription.text =
                        HtmlManager.getFromHtml(selectBook?.shotDescribe).trim()
                    initRecycleViewChapter()
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }
        })

    }


    // функция на запрос разрешений у пользователя для доступа к памяти и камере
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // Функция проверки зашли мы для редактирования или создаем новое объявление
    private fun checkEditState() {
        isEditState = isEditState()
        if (isEditState) {
            ad = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(ADS_DATA, Ad::class.java)!!
            } else {
                intent.getSerializableExtra(ADS_DATA) as Ad
            }

            if (ad != null) {
                ad!!.idBookLocal?.let { getBookById(it.toLong(), ad!!.key) }
                key = ad!!.key

            }
        }
    }


    private fun isEditState(): Boolean {
        return intent.getBooleanExtra(EDIT_STATE_AD, false)
    }

    //Заполнение View при редактировании
    private fun fillViews(ad: Ad, book: BookEntity4?) = with(binding) {
        editTel.setText(ad.tel)
        editEmail.setText(ad.email)

        var title: String? = null
        var categoryLiter: String? = null
        var categoryAge: String? = null
        var description: String? = null
        //Log.d("MyLog", "selectBook $book")
        if (book != null) {
            title = "${ad.idBookLocal}/ ${book.titleBook}"
            categoryLiter = book.genreLiterature
            categoryAge = book.ageCat
            description = HtmlManager.getFromHtml(book.shotDescribe).toString().trim()
        } else {
            btPublish.visibility = View.GONE
            mess.visibility = View.VISIBLE
        }
        tvLiter.text = categoryLiter ?: ad.categoryLiter
        tvAge.text = categoryAge ?: ad.categoryAge
        edTitle.text = title ?: ad.titleBook

        edTitle.visibility = View.GONE

        edTitle2.text = title ?: ad.titleBook

        cv2.visibility = View.VISIBLE
        edTitle2.visibility = View.VISIBLE
        edDescription.text = description ?: ad.description

        getAllChapters()
        initRecycleViewChapter()
    }

    private fun init() {
        imageAdapter = EditAdsActivityImageAdapter()
        binding.vpImages.adapter = imageAdapter
    }

    //onClicks
    fun onClickBackListener(view: View) {
        finish()
    }


    // реализовала автозаполнение при выборе книги, поэтому закомментировала
    //fun onClickSelectAgeCat(view: View) {
    // список возрастных ограничений
    //val listGenresLiter = LiterKindHelper.getAgeCat("age", this)
    //dialog.showSpinnerDialog(this, listGenresLiter, binding.tvAge)
    //}

    /*
    // реализовала автозаполнение при выборе книги, поэтому закомментировала
    fun onClickSelectGenresLiter(view: View) {
        // список жанров литературы
        //val listGenresLiter = LiterKindHelper.getGenresLiterFromAll("genres", this)
        val listGenresLiter =
            resources.getStringArray(R.array.genres_of_literature).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, listGenresLiter, binding.tvLiter)
    } */

    // получим список всех неопубликованных книг данного пользователя
    private fun getAllBooksNotPublic() {
        mainViewModel.allBooksNotPublic(currentUser).observe(this, Observer {
            val list = it // it - это обновлённый список
            if (!list.isNullOrEmpty())
                listBooks = list.toMutableList() as ArrayList
        })
    }

    private fun getBookById(id: Long, uidAd: String?) {
        mainViewModel.getBook(id, uidAd).observe(this, Observer {
            selectBook = it
            //Log.d("MyLog", "it $it")
            fillViews(ad!!, it)
        })
    }

    // получим список глав для данной книги
    fun getAllChapters() {
        val id = getIdSelectBook()
        if (selectBook?.uidAd == ad?.key)
            if (!id.isNullOrEmpty()) {
                val idBook = id.toLong()
                mainViewModel.allChaptersById(idBook).observe(this, Observer {
                    val list = it // it - это обновлённый список
                    if (!list.isNullOrEmpty())
                        listChapters = list.toMutableList() as ArrayList

                    adapterChapter.submitList(it)
                    //Log.d("MyLog", "listChapters ${listChapters.toString()}")
                })
            }
    }

    private fun initRecycleViewChapter() = with(binding) {

        rvListChapters.layoutManager = LinearLayoutManager(this@EditAdsActivity)
        adapterChapter = EditAdsActivityChapterRsAdapter(
            this@EditAdsActivity,
            defPref, this@EditAdsActivity
        )
        rvListChapters.adapter = adapterChapter
    }

    // найдем id выбранной книги, если она новая
    private fun getIdSelectBook(): String? {
        if (binding.edTitle.text.isNotEmpty()) {
            val str = binding.edTitle.text.toString()
            val k = str.indexOf("/")
            return str.substring(0, k)
        }
        return null
    }

    // получим BookEntity выбранной книги, если она публикуется в первый раз
    private fun getSelectedBook(id: String?): BookEntity4? {

        if (!id.isNullOrEmpty()) {
            for (item in listBooks.indices) {
                if (listBooks[item].id == id.toLong())
                    selectBook = listBooks[item]
                return selectBook
            }
        }
        return null
    }


    fun onClickSelectTitleBook(view: View) {

        // составим список id + заголовков книг
        val listNameBook = ArrayList<String>()

        if (listBooks.isNotEmpty()) {
            for (i in listBooks.indices) {
                val str = listBooks[i].id.toString() + "/ " + listBooks[i].titleBook.toString()
                listNameBook.add(str)
            }
            // создаем диалог список из заголовков книг этого автора
            dialog.showSpinnerDialog(this, listNameBook, binding.edTitle)
        } else {
            createDialogInfo(resources.getString(R.string.selection_section_empty), this)
        }


    }


    // нажать на кнопку, получить картинку
    fun onClickGetImages(view: View) {

        if (imageAdapter.mainArray.size == 0) {
            ImagePicker.getMultiImages(this, 3)
        } else {
            openChooseImageFrag(null, ArrayList<Bitmap>(), true)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scroolViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null
        updateImageCounter(binding.vpImages.currentItem)
    }

    // открываем фрагмент для выбора картинок
    fun openChooseImageFrag(newList: ArrayList<Uri>?, array: ArrayList<Bitmap>, boolean: Boolean) {

        chooseImageFrag = ImageListFragment(this)
        if (newList != null) chooseImageFrag?.resizeSelectedImages(newList, boolean, this, array)
        binding.scroolViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
            .replace(R.id.place__holder_ads, chooseImageFrag!!)
            .commit()

    }

    fun openChooseImageFrag2() {

        chooseImageFrag = ImageListFragment(this)
        binding.scroolViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
            .replace(R.id.place__holder_ads, chooseImageFrag!!)
            .commit()

    }


    fun onClickPublish(view: View) {

        if(!isOnline(this)) {
            createDialogInfo(resources.getString(R.string.network_exception), this)
            return
        }

        val adTemp = fillAd()
        val listChaps = fillListChaptersPublic(adTemp.key.toString())

        if (isFieldsEmpty(listChaps)) {
            createDialogInfo(resources.getString(R.string.edit_publish), this)
            return
        }

        binding.progressLayout.visibility = View.VISIBLE
        Log.d("MyLog", "ad!!.key.toString() ${adTemp.key}")
        //Log.d("MyLog", "id_ ${selectBook?.id.toString()}")
        if (isEditState) {

            //selectBook?.id?.let { updateListChapters(listChapters) }
            dbManager.publishAd(
                adTemp.copy(key = ad?.key),
                onPublishFinish(),
                listChaps
            ) // редактирование существующего обновления

        } else {

            selectBook?.let { updatePublicBook(it, adTemp.key) }
            //selectBook?.id?.let { updateListChapters(listChapters) }


            dbManager.publishAd(
                adTemp,
                onPublishFinish(),
                listChaps
            ) // публикация нового объявления
        }
    }

    private fun onPublishFinish(): DbManager.FinishWorkListener {

        return object : DbManager.FinishWorkListener {
            override fun onFinish(isDone: Boolean) { // закрываем активити, когда данные записаны на firebase
                binding.progressLayout.visibility = View.GONE
                if (isDone) finish()
            }

        }
    }

    // проверка все ли поля заполнены
    private fun isFieldsEmpty(listChaps: ArrayList<ChapterPublic>): Boolean = with(binding) {
        return editEmail.text.toString().trim().isEmpty()
                || edTitle.text.toString().trim().isEmpty()
                || tvLiter.text.toString().trim().isEmpty()
                || tvAge.text.toString().trim().isEmpty()
                || edDescription.text.toString().trim().isEmpty()
                || listChaps.isEmpty()
    }


    private fun fillAd(): Ad {
        //Log.d("MyLog", "id_ ${selectBook?.id.toString()}")
        val adTemp: Ad
        val time = System.currentTimeMillis().toString()
        binding.apply {
            adTemp = Ad(
                ad?.key ?: dbManager.db.push().key,
                editTel.text.toString(),
                editEmail.text.toString(),
                tvLiter.text.toString(),
                tvAge.text.toString(),
                edTitle.text.toString(),
                edDescription.text.toString(),
                ad?.mainImage ?: "empty",
                ad?.image2 ?: "empty",
                ad?.image3 ?: "empty",
                ad?.loginOwner ?: dbManager.auth.currentUser?.email,
                ad?.uidOwner ?: dbManager.auth.uid,
                //ad?.time ?: System.currentTimeMillis().toString(),
                time,
                "${tvLiter.text}_$time",
                ad?.idBookLocal ?: selectBook?.id.toString(),
                "0",
            )
        }
        return adTemp
    }

    private fun fillListChaptersPublic(key: String): ArrayList<ChapterPublic> {
        val newListChaptersPublic = ArrayList<ChapterPublic>()
        //if (listChaptersPublic.isNotEmpty())
        if (listChapters.isNotEmpty())
        //for (i in listChaptersPublic.indices) {
            for (i in listChapters.indices) {
                val chaptersPublic = ChapterPublic(

                    listChapters[i].id.toString(),
                    listChapters[i].idBook.toString(),
                    listChapters[i].titleChapters.toString(),
                    HtmlManager.getFromHtml(listChapters[i].shotDescribe).toString().trim(),
                    HtmlManager.getFromHtml(listChapters[i].context).toString().trim(),
                    System.currentTimeMillis().toString(),
                    listChapters[i].number.toString(),
                    listChapters[i].public_.toString(),
                    key, //ad?.key,
                    dbManager.auth.currentUser?.email,
                    dbManager.auth.uid,
                    "0"
                )
                newListChaptersPublic.add(chaptersPublic)
            }
        return newListChaptersPublic
    }


    private fun imageChangeCounter() {
        binding.vpImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageCounter(position)
            }
        })
    }


    private fun updateImageCounter(counter: Int) {
        var index = 1
        val itemCount = binding.vpImages.adapter?.itemCount
        if (itemCount == 0) index = 0
        val imageCounter = "${counter + index}/$itemCount"
        binding.tvImageCounter.text = imageCounter
    }

    // функция для выбора размера текста
    private fun setTextSize() = with(binding) {
        editTel.setTextSize(defPref.getString("content_size_key", "18"))
        editEmail.setTextSize(defPref.getString("content_size_key", "18"))
        tvLiter.setTextSize(defPref.getString("content_size_key", "18"))
        tvAge.setTextSize(defPref.getString("content_size_key", "18"))
        edTitle.setTextSize(defPref.getString("content_size_key", "18"))
        edDescription.setTextSize(defPref.getString("content_size_key", "18"))

        tvTitleTel.setTextSize(defPref.getString("comments_size_key", "16"))
        tvTitleEmail.setTextSize(defPref.getString("comments_size_key", "16"))
        tvLiterCat.setTextSize(defPref.getString("comments_size_key", "16"))
        tvAgeCat.setTextSize(defPref.getString("comments_size_key", "16"))
        tvTitle.setTextSize(defPref.getString("comments_size_key", "16"))
        tvTitleDescription.setTextSize(defPref.getString("comments_size_key", "16"))

        tvListChapters.setTextSize(defPref.getString("comments_size_key", "16"))
        textView24.setTextSize(defPref.getString("comments_size_key", "16"))
        comm1.setTextSize(defPref.getString("comments_size_key", "16"))
        comm2.setTextSize(defPref.getString("comments_size_key", "16"))

    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {
        editTel.setTypeface(
            defPref.getString("font_family_content_key", "sans-serif"),
            this@EditAdsActivity
        )
        editEmail.setTypeface(
            defPref.getString("font_family_content_key", "sans-serif"),
            this@EditAdsActivity
        )
        tvLiter.setTypeface(
            defPref.getString("font_family_content_key", "sans-serif"),
            this@EditAdsActivity
        )
        tvAge.setTypeface(
            defPref.getString("font_family_content_key", "sans-serif"),
            this@EditAdsActivity
        )
        edTitle.setTypeface(
            defPref.getString("font_family_content_key", "sans-serif"),
            this@EditAdsActivity
        )
        edDescription.setTypeface(
            defPref.getString("font_family_content_key", "sans-serif"),
            this@EditAdsActivity
        )

        tvListChapters.setTypeface(
            defPref.getString("font_family_comment_key", "sans-serif"),
            this@EditAdsActivity
        )

        textView24.setTypeface(
            defPref.getString("font_family_comment_key", "sans-serif"),
            this@EditAdsActivity
        )

        comm1.setTypeface(
            defPref.getString("font_family_comment_key", "sans-serif"),
            this@EditAdsActivity
        )

        comm2.setTypeface(
            defPref.getString("font_family_comment_key", "sans-serif"),
            this@EditAdsActivity
        )

        tvTitleTel.setTypeface(
            defPref.getString("font_family_comment_key", "sans-serif"),
            this@EditAdsActivity
        )
        tvTitleEmail.setTypeface(
            defPref.getString("font_family_comment_key", "sans-serif"),
            this@EditAdsActivity
        )
        tvLiterCat.setTypeface(
            defPref.getString("font_family_comment_key", "sans-serif"),
            this@EditAdsActivity
        )
        tvAgeCat.setTypeface(
            defPref.getString("font_family_comment_key", "sans-serif"),
            this@EditAdsActivity
        )
        tvTitle.setTypeface(
            defPref.getString("font_family_comment_key", "sans-serif"),
            this@EditAdsActivity
        )
        tvTitleDescription.setTypeface(
            defPref.getString("font_family_comment_key", "sans-serif"),
            this@EditAdsActivity
        )


        titBook.setTypeface(
            defPref.getString("font_family_title_key", "sans-serif"),
            this@EditAdsActivity
        )

        btPublish.setTypeface(
            defPref.getString("font_family_button_key", "sans-serif"),
            this@EditAdsActivity
        )

    }

    override fun updatePublic(chapter: ChapterEntity2) {
        mainViewModel.updateChapter(chapter)
    }

    private fun updatePublicBook(book: BookEntity4, key: String?) {
        Log.d("MyLog", "ad!!.key.toString() $key")
        //book.copy(public = "1")
        book.public = "1"
        book.uidAd = key
        mainViewModel.updateBook(book)

    }

    companion object {

    }
}

