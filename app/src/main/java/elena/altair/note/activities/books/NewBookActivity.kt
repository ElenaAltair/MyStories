package elena.altair.note.activities.books

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_SETTINGS
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import elena.altair.note.R
import elena.altair.note.activities.MainActivity
import elena.altair.note.activities.MainActivity.Companion.USER_ANONYMOUS
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
import elena.altair.note.constants.MyConstants.COMMENT_SIZE_DEFAULT
import elena.altair.note.constants.MyConstants.COMMENT_SIZE_KEY
import elena.altair.note.constants.MyConstants.CONTENT_SIZE_DEFAULT
import elena.altair.note.constants.MyConstants.CONTENT_SIZE_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_COMMENT_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_CONTENT_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_DEFAULT
import elena.altair.note.constants.MyConstants.FONT_FAMILY_TITLE_KEY
import elena.altair.note.constants.MyConstants.TITLE_SIZE_DEFAULT
import elena.altair.note.constants.MyConstants.TITLE_SIZE_KEY
import elena.altair.note.databinding.ActivityNewBookBinding
import elena.altair.note.databinding.CreateDialogBinding
import elena.altair.note.dialoghelper.DialogInfo.createDialogInfo
import elena.altair.note.dialoghelper.DialogSave.DialogSaveAndGetOut
import elena.altair.note.dialoghelper.DialogSave.dialogSaveBook
import elena.altair.note.dialoghelper.DialogSpinnerHelper
import elena.altair.note.dialoghelper.ProgressDialog
import elena.altair.note.etities.BookEntity7
import elena.altair.note.etities.ProfileEntity2
import elena.altair.note.fragments.books.MainListFragment
import elena.altair.note.model.DbManager
import elena.altair.note.utils.LiterKindHelper
import elena.altair.note.utils.file.DOCXUtils.saveDocx
import elena.altair.note.utils.file.ExtDocx.extractDocx
import elena.altair.note.utils.file.ExtPdf.extractPdf
import elena.altair.note.utils.file.ExtTxt.extractTxt
import elena.altair.note.utils.file.FilePath.getDriveFilePath
import elena.altair.note.utils.file.PDFUtils.savePdf
import elena.altair.note.utils.file.TXTUtils.saveTxt
import elena.altair.note.utils.font.TypefaceUtils.setTitleActionBar
import elena.altair.note.utils.font.TypefaceUtils.typeface
import elena.altair.note.utils.font.setTextSize
import elena.altair.note.utils.font.setTypeface
import elena.altair.note.utils.settings.TimeManager.getCurrentTime
import elena.altair.note.utils.share.ShareHelperBook
import elena.altair.note.utils.share.ShareHelperBook.makeShareTextBook
import elena.altair.note.utils.text.TextStyle.setBoldForSelectedText
import elena.altair.note.utils.text.TextStyle.setColorForSelectedText
import elena.altair.note.utils.text.TextStyle.setItalicForSelectedText
import elena.altair.note.utils.text.TextStyle.setStrikethroughForSelectedText
import elena.altair.note.utils.text.TextStyle.setUnderlineForSelectedText
import elena.altair.note.utils.text.textRedactor.HtmlManager
import elena.altair.note.utils.text.textRedactor.MyTouchListener
import elena.altair.note.utils.theme.ThemeUtils.getSelectedTheme
import elena.altair.note.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


// Активити для редактирования заголовка книг и добавление новых книг в базу данных
@AndroidEntryPoint
class NewBookActivity : AppCompatActivity() {

    private lateinit var defPref: SharedPreferences
    private var pref: SharedPreferences? = null
    private lateinit var binding: ActivityNewBookBinding
    private var book: BookEntity7? = null
    private val mainViewModel: MainViewModel by viewModels()
    private var currentUser = ""
    private val dialog = DialogSpinnerHelper()
    private val STORAGE_CODE: Int = 100
    private val DSQLITE_MAX_LENGTH = 2000
    private var job: Job? = null
    private var oldBook: BookEntity7? = null
    private var newBook: BookEntity7? = null
    private val dbManager = DbManager()
    private var profile: ProfileEntity2? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme(defPref))
        val currentBackground = defPref.getString(BACKGROUND_KEY, BACKGROUND_DEFAULT).toString()
        super.onCreate(savedInstanceState)

        binding = ActivityNewBookBinding.inflate(layoutInflater)
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


        getBook()
        observer()
        init()
        // зададим настройки текста
        setTextSize()
        setFontFamily()
        onClickImageColor()
        onClickImageBold()
        onClickImageItalic()
        onClickImageUnderlined()
        onClickImageStrikethrough()
        onClickColorPicker()
        // активируем стрелку на верхнем меню
        actionBarSetting()
        ActivityCompat.requestPermissions(
            this, arrayOf(
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE
            ),
            PackageManager.PERMISSION_GRANTED
        )


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                newBook = createNewBookForOldAndNew()
                if (newBook != oldBook) {
                    setMainResult()
                } else
                    finish()
            }
        })


        requestMultiplePermissions.launch(
            arrayOf(
                //CAMERA,
                WRITE_SETTINGS,
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


    @SuppressLint("SuspiciousIndentation")
    fun onClickSelectKindLiter(view: View) {
        // список родов литературы
        //val listKindLiter = LiterKindHelper.getAllKindLiter(this)
        val listKindLiter =
            resources.getStringArray(R.array.kinds_of_literature).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, listKindLiter, binding.edCatLiter1)
        if (binding.edCatLiter2.text.toString() != resources.getString(R.string.select_genre_liter) ||
            binding.edCatLiter2.text.toString() == resources.getString(R.string.all_genres)
        ) {
            binding.edCatLiter2.text = resources.getString(R.string.select_genre_liter)
        }
    }


    private fun observer() {
        mainViewModel.getProfile(dbManager.auth.currentUser?.email.toString())
            .observe(this, Observer {
                if (it != null) {
                    profile = it
                }
            })
    }


    fun onClickSelectAlias(view: View) {
        if (MainActivity.currentUser == USER_ANONYMOUS || MainActivity.currentUser == "" || MainActivity.currentUser == "null") {
            createDialogInfo(resources.getString(R.string.can_create_profile), this)
            return
        }

        val list = ArrayList<String>()
        if (profile != null) {
            if (profile!!.nameAuthor1 != "")
                list.add(profile!!.nameAuthor1)
            if (profile!!.nameAuthor2 != "")
                list.add(profile!!.nameAuthor2)
            if (profile!!.nameAuthor3 != "")
                list.add(profile!!.nameAuthor3)
        }

        if (list.isEmpty()) {
            createDialogCreate(resources.getString(R.string.create_signature))
        } else {
            dialog.showSpinnerDialog(this, list, binding.edAlias)
        }

    }

    private fun createDialogCreate(
        message: String,
    ) {
        val builder = AlertDialog.Builder(this)
        val bindingDialog = CreateDialogBinding.inflate(this.layoutInflater)
        val view = bindingDialog.root
        builder.setView(view)
        bindingDialog.tvMess.text = message
        val dialog = builder.create()
        bindingDialog.bNo.setOnClickListener {
            dialog?.dismiss()
        }

        bindingDialog.bCreate.setOnClickListener {
            val i = Intent(this, ProfileActivity::class.java)
            startActivity(i)

            dialog?.dismiss()
        }
        dialog.show()
    }

    fun onClickSelectGenresLiter(view: View) {
        val selKindLiter = binding.edCatLiter1.text.toString()
        if (selKindLiter != resources.getString(R.string.select_kind_liter)) {
            // список жанров литературы
            //val listGenresLiter = LiterKindHelper.getGenresLiter(selKindLiter, this)
            var listGenresLiter =
                resources.getStringArray(R.array.genres_of_literature).toMutableList() as ArrayList
            if (selKindLiter == "Epic" || selKindLiter == "Эпос") {
                listGenresLiter =
                    resources.getStringArray(R.array.epic).toMutableList() as ArrayList
            } else if (selKindLiter == "Lyrics" || selKindLiter == "Лирика") {
                listGenresLiter =
                    resources.getStringArray(R.array.lyrics).toMutableList() as ArrayList
            } else if (selKindLiter == "Drama" || selKindLiter == "Драма") {
                listGenresLiter =
                    resources.getStringArray(R.array.drama).toMutableList() as ArrayList
            } else if (selKindLiter == "Lyric-epic genres" || selKindLiter == "Лиро-эпические жанры") {
                listGenresLiter =
                    resources.getStringArray(R.array.lyric_epic).toMutableList() as ArrayList
            }

            dialog.showSpinnerDialog(this, listGenresLiter, binding.edCatLiter2)
        } else {
            createDialogInfo(resources.getString(R.string.first_select_kind_liter), this)
            //Toast.makeText(this, "First, select the type of literature", Toast.LENGTH_LONG).show()
        }
    }

    fun onClickSelectAgeCat(view: View) {
        // список возрастных ограничений
        val listGenresLiter = LiterKindHelper.getAgeCat("age", this)
        dialog.showSpinnerDialog(this, listGenresLiter, binding.edCatAge)
    }

    // активируем стрелку на верхнем меню
    private fun actionBarSetting() {
        val ab = supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // нажимаем на открыть книгу во фрагменте со списком книг,
    // если оттуда ничего не передали, то значит мы в этот момент создаем новую книгу,
    // если передали, то значит открываем книгу на редактирование и передаем данные по данной книге
    // и заполняем все поля в activity_new_book.xml
    private fun getBook() {
        currentUser = intent.getStringExtra(MainListFragment.CURRENT_USER).toString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            book =
                intent.getSerializableExtra(MainListFragment.NEW_NOTE_KEY, BookEntity7::class.java)

            fillBook()
        } else {
            val sBook = intent.getSerializableExtra(MainListFragment.NEW_NOTE_KEY)

            if (sBook != null) {
                book = sBook as BookEntity7
                fillBook()
            }
        }

    }

    // слушатель нажатий для палитры цветов
    private fun onClickColorPicker() = with(binding) {
        imRed.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_red,
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription,
                this@NewBookActivity
            )
        }
        imOrangeDark.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_orange_dark,
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription,
                this@NewBookActivity
            )
        }
        imOrange.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_orange,
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription,
                this@NewBookActivity
            )
        }
        imYellow.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_yellow,
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription,
                this@NewBookActivity
            )
        }
        imPurple.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_purple,
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription,
                this@NewBookActivity
            )
        }
        imBlue.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_blue,
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription,
                this@NewBookActivity
            )
        }
        imGreenSee.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_green_see,
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription,
                this@NewBookActivity
            )
        }
        imGreen.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_green,
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription,
                this@NewBookActivity
            )
        }
        imBlack.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_black,
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription,
                this@NewBookActivity
            )
        }
        imGrey.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_grey,
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription,
                this@NewBookActivity
            )
        }
        imGreyLight.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_grey_light,
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription,
                this@NewBookActivity
            )
        }
        imWhite.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_white,
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription,
                this@NewBookActivity
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        // инициализируем слушатель нажатий для перетаскивания элементов
        // в нашем случае для перетаскивания панельки с палитрой
        binding.colorPicker.setOnTouchListener(MyTouchListener())
        pref = PreferenceManager.getDefaultSharedPreferences(this)


        // подключим наш собсвенный Action Bar к нашему активити
        setSupportActionBar(binding.toolbar)
    }

    private fun onClickImageColor() = with(binding) {
        imageColor.setOnClickListener {
            // проверяем видимость нашего layout с палитрой
            if (binding.colorPicker.isShown) {
                closeColorPicker() // закрываем нашу палитру
            } else {
                openColorPicker() // открываем нашу палитру
            }
        }
    }

    private fun onClickImageStrikethrough() = with(binding) {
        imageStrikethrough.setOnClickListener {
            setStrikethroughForSelectedText(
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription
            )
        }
    }

    private fun onClickImageUnderlined() = with(binding) {
        imageUnderlined.setOnClickListener {
            setUnderlineForSelectedText(
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription
            )
        }
    }

    private fun onClickImageItalic() = with(binding) {
        imageItalic.setOnClickListener {
            setItalicForSelectedText(
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription
            )
        }
    }

    private fun onClickImageBold() = with(binding) {
        imageBold.setOnClickListener {
            setBoldForSelectedText(
                edDescription.selectionStart,
                edDescription.selectionEnd,
                edDescription
            )
        }
    }


    // заполнение полей в разметке при редактировании
    private fun fillBook() = with(binding) {
        if (book != null) {
            edTitle.setText(book?.titleBook)
            edDescription.setText(HtmlManager.getFromHtml(book?.shotDescribe!!).trim())
            edCatLiter1.text = book?.kindLiterature
            edCatLiter2.text = book?.genreLiterature
            edCatAge.text = book?.ageCat
            edAlias.text = book?.nameAuthor
        }

        oldBook = createNewBookForOldAndNew()

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu_save_share, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // когда нажимаем на кнопку save, передаём результат
        if (item.itemId == R.id.id_save) {
            oldBook = createNewBookForOldAndNew()
            job = CoroutineScope(Dispatchers.Main).launch {
                if (book == null) {
                    setMainResult()

                } else {
                    val tempBook = updateBook() // иначе, редактируем существующую заметку
                    dialogSaveBook(
                        resources.getString(R.string.sure_save_book),
                        this@NewBookActivity,
                        false,
                        mainViewModel,
                        tempBook!!
                    )
                }
            }

        } else if (item.itemId == R.id.id_share) { // кнопка поделиться
            // Вызываем диалог выбора приложений для отправки текста поста в другое приложение
            val shareIntent = Intent.createChooser(
                ShareHelperBook.shareBook(
                    createNewBookForShare(),
                    book?.titleBook.toString(),
                    this
                ),
                "Share by"
            )
            startActivity(shareIntent)
        } else if (item.itemId == R.id.id_pdf) {
            // хочу сделать сохранение в PDF
            val string = makeShareTextBook(
                createNewBookForShare(),
                book?.titleBook.toString(),
                this
            )

            var titleTemp = book?.titleBook.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(this@NewBookActivity)
                    val strMessage = savePdf(title, string, this@NewBookActivity)
                    dialog.dismiss()
                    createDialogInfo(strMessage, this@NewBookActivity)
                }
            } else {

                if (checkSelfPermission(WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED
                ) {
                    //permission was not granted, request it
                    val permissions = arrayOf(WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permissions, STORAGE_CODE)
                } else {
                    //permission already granted, call savePdf() method
                    job = CoroutineScope(Dispatchers.Main).launch {
                        val dialog = ProgressDialog.createProgressDialog(this@NewBookActivity)
                        val strMessage = savePdf(title, string, this@NewBookActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewBookActivity)
                    }
                }
            }
        } else if (item.itemId == R.id.id_txt) {

            // хочу сделать сохранение в TXT
            val string = makeShareTextBook(
                createNewBookForShare(),
                book?.titleBook.toString(),
                this
            )
            var titleTemp = book?.titleBook.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(this@NewBookActivity)
                    val strMessage = saveTxt(title, string, this@NewBookActivity)
                    dialog.dismiss()
                    createDialogInfo(strMessage, this@NewBookActivity)
                }
            } else {
                if (checkSelfPermission(WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED
                ) {
                    //permission was not granted, request it
                    val permissions = arrayOf(WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permissions, STORAGE_CODE)
                } else {
                    //permission already granted, call saveTxt() method
                    job = CoroutineScope(Dispatchers.Main).launch {
                        val dialog = ProgressDialog.createProgressDialog(this@NewBookActivity)
                        val strMessage = saveTxt(title, string, this@NewBookActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewBookActivity)
                    }
                }
            }
        } else if (item.itemId == R.id.id_docx) {
            val string = makeShareTextBook(
                createNewBookForShare(),
                book?.titleBook.toString(),
                this
            )
            var titleTemp = book?.titleBook.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            job = CoroutineScope(Dispatchers.Main).launch {
                val dialog = ProgressDialog.createProgressDialog(this@NewBookActivity)
                val strMessage = saveDocx(title, string, this@NewBookActivity)
                dialog.dismiss()
                createDialogInfo(strMessage, this@NewBookActivity)
            }

        } else if (item.itemId == R.id.id_extract) {
            // извлечение текста из pdf
            launcherPDF.launch("application/pdf") // application/pdf

        } else if (item.itemId == R.id.id_extract_txt) {
            // извлечение текста из текстового файла
            launcherTXT.launch("text/plain") //"text/plain"
        } else if (item.itemId == R.id.id_extract_docx) {
            // извлечение текста из docx
            launcherDOCX.launch("application/docx")
        } else if (item.itemId == android.R.id.home) { // нажимаем на кнопку стрелка на верхнем меню

            newBook = createNewBookForOldAndNew()
            if (newBook != oldBook) {
                setMainResult()
            } else
                finish()
        }
        return super.onOptionsItemSelected(item)
    }


    // извлечение текст из docx файла
    private val launcherDOCX = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            //Log.d("MyLog", "uri $uri")
            val cursor = contentResolver.query(uri, null, null, null, null)
            var nameFile = ""
            if (cursor != null && cursor.moveToFirst()) {
                // получим имя файла по индексу
                val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                nameFile = cursor.getString(nameIndex)
            }
            if (nameFile.contains(".docx")) {
                // поместим выбранный файл в кеш приложения и считаем текст из него
                val pathBuff = getDriveFilePath(uri, this, nameFile)

                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(this@NewBookActivity)

                    val text = extractDocx(
                        pathBuff,
                        binding.edDescription,
                        DSQLITE_MAX_LENGTH,
                        this@NewBookActivity,
                    )

                    binding.edDescription.setText(text)
                    dialog.dismiss()
                    binding.edDescription.setSelection(binding.edDescription.length())
                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewBookActivity)
            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewBookActivity
                )
            }
        }
    }


    // извлечение текст из txt файла
    private val launcherTXT = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            //Log.d("MyLog", "uri $uri")
            val cursor = contentResolver.query(uri, null, null, null, null)
            var nameFile = ""
            if (cursor != null && cursor.moveToFirst()) {
                // получим имя файла по индексу
                val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                nameFile = cursor.getString(nameIndex)
            }

            if (nameFile.contains(".txt")) {
                // поместим выбранный файл в кеш приложения и считаем текст из него
                val pathBuff = getDriveFilePath(uri, this, nameFile)

                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(this@NewBookActivity)

                    val text = extractTxt(
                        pathBuff,
                        binding.edDescription,
                        DSQLITE_MAX_LENGTH,
                        this@NewBookActivity,
                    )

                    binding.edDescription.setText(text)
                    dialog.dismiss()
                    binding.edDescription.setSelection(binding.edDescription.length())
                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewBookActivity)
            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewBookActivity
                )
            }
        }
    }

    // извлечение текст из pdf файла
    private val launcherPDF = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            //Log.d("MyLog", "uri $uri")
            val cursor = contentResolver.query(uri, null, null, null, null)
            var nameFile = ""
            if (cursor != null && cursor.moveToFirst()) {
                // получим имя файла по индексу
                val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                nameFile = cursor.getString(nameIndex)
            }
            if (nameFile.contains(".pdf")) {
                // поместим выбранный файл в кеш приложения и считаем текст из него
                val pathBuff = getDriveFilePath(uri, this, nameFile)
                //Log.d("MyLog", "pathBuff ${pathBuff}")


                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialogExtPdf(this@NewBookActivity)

                    val text = extractPdf(
                        pathBuff,
                        binding.edDescription,
                        DSQLITE_MAX_LENGTH,
                        this@NewBookActivity,
                    )

                    binding.edDescription.setText(text)
                    dialog.dismiss()
                    binding.edDescription.setSelection(binding.edDescription.length())
                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewBookActivity)
            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewBookActivity
                )
            }
        }
    }


    // для передаваемого результат сделаем отдельную функцию
    private fun setMainResult() {

        var editState = "new"
        val tempBook = if (book == null) {
            createNewBook() // то добавляем новую заметку в базу данных
        } else {
            editState = "update"
            updateBook() // иначе, редактируем существующую заметку
        }

        val i = Intent().apply {
            putExtra(MainListFragment.NEW_NOTE_KEY, tempBook)
            putExtra(MainListFragment.EDIT_STATE_KEY, editState)
        }

        DialogSaveAndGetOut(this@NewBookActivity, i)

    }

    private fun updateBook(): BookEntity7? = with(binding) {

        var typeLiter = edCatLiter1.text.toString()
        var genreLiter = edCatLiter2.text.toString()
        var ageCategory = edCatAge.text.toString()
        var nameA = edAlias.text.toString()
        if (edCatLiter1.text.toString() == resources.getString(R.string.select_kind_liter)) {
            typeLiter = resources.getString(R.string.all_types)
        }
        if (edCatLiter2.text.toString() == resources.getString(R.string.select_genre_liter)) {
            genreLiter = resources.getString(R.string.all_genres)
        }
        if (edCatAge.text.toString() == resources.getString(R.string.select_age_cat)) {
            ageCategory = "12+"
        }
        if (binding.edAlias.text.toString() == resources.getString(R.string.сhoose_alias)) {
            val nameTemp = currentUser
            if (MainActivity.currentUser == USER_ANONYMOUS || MainActivity.currentUser == "" || MainActivity.currentUser == "null") {
                nameA = USER_ANONYMOUS
            } else {
                val k = nameTemp.indexOf("@")
                //Log.d("MyLog", "str.substring(0, k) ${str.substring(0, k)}")
                nameA = nameTemp.substring(0, k)
            }
        }

        var text = HtmlManager.toHtml(binding.edDescription.text)
        if (text.length > 35000)
            text = text.substring(0, 35000)

        return book?.copy(
            titleBook = edTitle.text.toString(),
            shotDescribe = text,
            kindLiterature = typeLiter,
            genreLiterature = genreLiter,
            ageCat = ageCategory,
            nameAuthor = nameA,
        )

    }


    // функция заполняющая наш BookEntity
    private fun createNewBook(): BookEntity7 {
        var typeLiter = binding.edCatLiter1.text.toString()
        var genreLiter = binding.edCatLiter2.text.toString()
        var ageCat = binding.edCatAge.text.toString()
        var nameA = binding.edAlias.text.toString()
        if (binding.edCatLiter1.text.toString() == resources.getString(R.string.select_kind_liter)) {
            typeLiter = resources.getString(R.string.all_types)
        }
        if (binding.edCatLiter2.text.toString() == resources.getString(R.string.select_genre_liter)) {
            genreLiter = resources.getString(R.string.all_genres)
        }
        if (binding.edCatAge.text.toString() == resources.getString(R.string.select_age_cat)) {
            ageCat = "12+"
        }
        if (binding.edAlias.text.toString() == resources.getString(R.string.сhoose_alias)) {
            val nameTemp = currentUser
            if (MainActivity.currentUser == USER_ANONYMOUS || MainActivity.currentUser == "" || MainActivity.currentUser == "null") {
                nameA = USER_ANONYMOUS
            } else {
                val k = nameTemp.indexOf("@")
                //Log.d("MyLog", "str.substring(0, k) ${str.substring(0, k)}")
                nameA = nameTemp.substring(0, k)
            }
        }

        var text = HtmlManager.toHtml(binding.edDescription.text)
        if (text.length > 35000)
            text = text.substring(0, 35000)

        return BookEntity7(
            null,
            binding.edTitle.text.toString(),
            text,
            0L,
            currentUser,
            getCurrentTime(),
            typeLiter,
            genreLiter,
            ageCat,
            "0",
            0L,
            "",
            0,
            null,
            nameA,
        )
    }

    private fun createNewBookForOldAndNew(): BookEntity7 {
        var typeLiter = binding.edCatLiter1.text.toString()
        var genreLiter = binding.edCatLiter2.text.toString()
        var ageCat = binding.edCatAge.text.toString()
        var nameA = binding.edAlias.text.toString()
        if (binding.edCatLiter1.text.toString() == resources.getString(R.string.select_kind_liter)) {
            typeLiter = resources.getString(R.string.all_types)
        }
        if (binding.edCatLiter2.text.toString() == resources.getString(R.string.select_genre_liter)) {
            genreLiter = resources.getString(R.string.all_genres)
        }
        if (binding.edCatAge.text.toString() == resources.getString(R.string.select_age_cat)) {
            ageCat = "12+"
        }
        if (binding.edAlias.text.toString() == resources.getString(R.string.сhoose_alias)) {
            val nameTemp = currentUser
            if (MainActivity.currentUser == USER_ANONYMOUS || MainActivity.currentUser == "" || MainActivity.currentUser == "null") {
                nameA = USER_ANONYMOUS
            } else {
                val k = nameTemp.indexOf("@")
                //Log.d("MyLog", "str.substring(0, k) ${str.substring(0, k)}")
                nameA = nameTemp.substring(0, k)
            }
        }

        var text = HtmlManager.toHtml(binding.edDescription.text)
        if (text.length > 35000)
            text = text.substring(0, 35000)

        return BookEntity7(
            null,
            binding.edTitle.text.toString(),
            text,
            0L,
            currentUser,
            "14:00:00 - 01/01/2024",
            typeLiter,
            genreLiter,
            ageCat,
            "0",
            0L,
            "",
            0,
            null,
            nameA,
        )
    }

    // функция заполняющая наш BookEntity
    private fun createNewBookForShare(): BookEntity7 {
        var typeLiter = binding.edCatLiter1.text.toString()
        var genreLiter = binding.edCatLiter2.text.toString()
        var ageCat = binding.edCatAge.text.toString()
        var nameA = binding.edAlias.text.toString()


        if (binding.edCatLiter1.text.toString() == resources.getString(R.string.select_kind_liter)) {
            typeLiter = resources.getString(R.string.all_types)
        }
        if (binding.edCatLiter2.text.toString() == resources.getString(R.string.select_genre_liter)) {
            genreLiter = resources.getString(R.string.all_genres)
        }
        if (binding.edCatAge.text.toString() == resources.getString(R.string.select_age_cat)) {
            ageCat = "12+"
        }

        if (binding.edAlias.text.toString() == resources.getString(R.string.сhoose_alias)) {
            val nameTemp = currentUser
            if (MainActivity.currentUser == USER_ANONYMOUS || MainActivity.currentUser == "" || MainActivity.currentUser == "null") {
                nameA = USER_ANONYMOUS
            } else {
                val k = nameTemp.indexOf("@")
                //Log.d("MyLog", "str.substring(0, k) ${str.substring(0, k)}")
                nameA = nameTemp.substring(0, k)
            }
        }

        return BookEntity7(
            null,
            binding.edTitle.text.toString(),
            binding.edDescription.text.toString(),
            0L,
            currentUser,
            getCurrentTime(),
            typeLiter,
            genreLiter,
            ageCat,
            "0",
            0L,
            "",
            0,
            null,
            nameA,
        )
    }

    // функция для открытия панели цветов
    private fun openColorPicker() {
        binding.colorPicker.visibility = View.VISIBLE
        val openAnim = AnimationUtils.loadAnimation(this, R.anim.open_color)
        binding.colorPicker.startAnimation(openAnim)
    }

    // функция для закрытия панели цветов
    private fun closeColorPicker() {
        val openAnim = AnimationUtils.loadAnimation(this, R.anim.close_color)
        openAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                binding.colorPicker.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        binding.colorPicker.startAnimation(openAnim)
    }

    // функция для выбора размера текста
    private fun setTextSize() = with(binding) {
        edTitle.setTextSize(pref?.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT))
        edDescription.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edCatLiter1.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edCatLiter2.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edCatAge.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edAlias.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))

        tw1.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        tw2.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        tw3.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        tw4.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        tw5.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        tw6.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textComm0.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textComm1.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textComm2.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textComm3.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textComm5.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {
        edDescription.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        edCatLiter1.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        edCatLiter2.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        edCatAge.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        edAlias.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        edTitle.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )


        tw1.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        tw2.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        tw3.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        tw4.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        tw5.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        tw6.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        textComm0.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        textComm1.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        textComm2.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        textComm3.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        textComm5.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )


        val font: Typeface? = typeface(
            pref?.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@NewBookActivity
        )
        setTitleActionBar(resources.getString(R.string.app_name), font, supportActionBar)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
