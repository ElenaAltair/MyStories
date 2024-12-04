package elena.altair.note.activities.books

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
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
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import elena.altair.note.R
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
import elena.altair.note.databinding.ActivityNewChapterBinding
import elena.altair.note.dialoghelper.DialogInfo.createDialogInfo
import elena.altair.note.dialoghelper.DialogSave.DialogSaveAndGetOut
import elena.altair.note.dialoghelper.DialogSave.dialogSaveChapter
import elena.altair.note.dialoghelper.ProgressDialog
import elena.altair.note.etities.BookEntity7
import elena.altair.note.etities.ChapterEntity2
import elena.altair.note.fragments.books.ChapterListFragment
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
import elena.altair.note.utils.share.ShareHelperChapter
import elena.altair.note.utils.share.ShareHelperChapter.makeShareText
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
import java.io.File

// Активити для редактирования глав книг и добавление новых глав в базу данных
@AndroidEntryPoint
class NewChapterActivity : AppCompatActivity() {

    private lateinit var defPref: SharedPreferences
    private var pref: SharedPreferences? = null
    private lateinit var binding: ActivityNewChapterBinding
    private var book: BookEntity7? = null
    private var chapter: ChapterEntity2? = null
    private var public = "0"
    private val STORAGE_CODE: Int = 100
    private val DSQLITE_MAX_LENGTH = 50000
    private var job: Job? = null
    private var oldChapter: ChapterEntity2? = null
    private var newChapter: ChapterEntity2? = null


    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme(defPref))

        super.onCreate(savedInstanceState)

        binding = ActivityNewChapterBinding.inflate(layoutInflater)
        setContentView(binding.root)



        getChapter()

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
                newChapter = createNewChapterForOldAndNew()
                if (newChapter != oldChapter) {
                    setMainResult()
                } else
                    finish()
            }
        })

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
            if (editContent.hasSelection()) {
                setStrikethroughForSelectedText(
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent
                )
            } else if (edDescription.hasSelection()) {
                setStrikethroughForSelectedText(
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription
                )
            }

        }
    }


    private fun onClickImageUnderlined() = with(binding) {
        imageUnderlined.setOnClickListener {
            if (editContent.hasSelection()) {
                setUnderlineForSelectedText(
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent
                )
            } else if (edDescription.hasSelection()) {
                setUnderlineForSelectedText(
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription
                )
            }

        }
    }


    private fun onClickImageItalic() = with(binding) {
        imageItalic.setOnClickListener {
            if (editContent.hasSelection()) {
                setItalicForSelectedText(
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent
                )
            } else if (edDescription.hasSelection()) {
                setItalicForSelectedText(
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription
                )
            }

        }
    }

    private fun onClickImageBold() = with(binding) {
        imageBold.setOnClickListener {
            if (editContent.hasSelection()) {
                setBoldForSelectedText(
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent
                )
            } else if (edDescription.hasSelection()) {
                setBoldForSelectedText(
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription
                )
            }

        }
    }

    // слушатель нажатий для палитры цветов
    private fun onClickColorPicker() = with(binding) {

        imRed.setOnClickListener {
            if (editContent.hasSelection())
                setColorForSelectedText(
                    R.color.picker_red,
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent,
                    this@NewChapterActivity
                )
            if (edDescription.hasSelection())
                setColorForSelectedText(
                    R.color.picker_red,
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription,
                    this@NewChapterActivity
                )
        }
        imOrangeDark.setOnClickListener {
            if (editContent.hasSelection())
                setColorForSelectedText(
                    R.color.picker_orange_dark,
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent,
                    this@NewChapterActivity
                )
            if (edDescription.hasSelection())
                setColorForSelectedText(
                    R.color.picker_orange_dark,
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription,
                    this@NewChapterActivity
                )
        }
        imOrange.setOnClickListener {
            if (editContent.hasSelection())
                setColorForSelectedText(
                    R.color.picker_orange,
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent,
                    this@NewChapterActivity
                )
            if (edDescription.hasSelection())
                setColorForSelectedText(
                    R.color.picker_orange,
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription,
                    this@NewChapterActivity
                )
        }
        imYellow.setOnClickListener {
            if (editContent.hasSelection())
                setColorForSelectedText(
                    R.color.picker_yellow,
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent,
                    this@NewChapterActivity
                )
            if (edDescription.hasSelection())
                setColorForSelectedText(
                    R.color.picker_yellow,
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription,
                    this@NewChapterActivity
                )
        }
        imPurple.setOnClickListener {
            if (editContent.hasSelection())
                setColorForSelectedText(
                    R.color.picker_purple,
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent,
                    this@NewChapterActivity
                )
            if (edDescription.hasSelection())
                setColorForSelectedText(
                    R.color.picker_purple,
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription,
                    this@NewChapterActivity
                )
        }
        imBlue.setOnClickListener {
            if (editContent.hasSelection())
                setColorForSelectedText(
                    R.color.picker_blue,
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent,
                    this@NewChapterActivity
                )
            if (edDescription.hasSelection())
                setColorForSelectedText(
                    R.color.picker_blue,
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription,
                    this@NewChapterActivity
                )
        }
        imGreenSee.setOnClickListener {
            if (editContent.hasSelection())
                setColorForSelectedText(
                    R.color.picker_green_see,
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent,
                    this@NewChapterActivity
                )
            if (edDescription.hasSelection())
                setColorForSelectedText(
                    R.color.picker_green_see,
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription,
                    this@NewChapterActivity
                )
        }
        imGreen.setOnClickListener {
            if (editContent.hasSelection())
                setColorForSelectedText(
                    R.color.picker_green,
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent,
                    this@NewChapterActivity
                )
            if (edDescription.hasSelection())
                setColorForSelectedText(
                    R.color.picker_green,
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription,
                    this@NewChapterActivity
                )
        }
        imBlack.setOnClickListener {
            if (editContent.hasSelection())
                setColorForSelectedText(
                    R.color.picker_black,
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent,
                    this@NewChapterActivity
                )
            if (edDescription.hasSelection())
                setColorForSelectedText(
                    R.color.picker_black,
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription,
                    this@NewChapterActivity
                )
        }
        imGrey.setOnClickListener {
            if (editContent.hasSelection())
                setColorForSelectedText(
                    R.color.picker_grey,
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent,
                    this@NewChapterActivity
                )
            if (edDescription.hasSelection())
                setColorForSelectedText(
                    R.color.picker_grey,
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription,
                    this@NewChapterActivity
                )
        }
        imGreyLight.setOnClickListener {
            if (editContent.hasSelection())
                setColorForSelectedText(
                    R.color.picker_grey_light,
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent,
                    this@NewChapterActivity
                )
            if (edDescription.hasSelection())
                setColorForSelectedText(
                    R.color.picker_grey_light,
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription,
                    this@NewChapterActivity
                )
        }
        imWhite.setOnClickListener {
            if (editContent.hasSelection())
                setColorForSelectedText(
                    R.color.picker_white,
                    editContent.selectionStart,
                    editContent.selectionEnd,
                    editContent,
                    this@NewChapterActivity
                )
            if (edDescription.hasSelection())
                setColorForSelectedText(
                    R.color.picker_white,
                    edDescription.selectionStart,
                    edDescription.selectionEnd,
                    edDescription,
                    this@NewChapterActivity
                )
        }

    }

    // активируем стрелку на верхнем меню
    private fun actionBarSetting() {
        val ab = supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    // заполнение полей в разметке при редактировании
    private fun fillChapter() = with(binding) {
        titleBook.text = book?.titleBook
        if (chapter != null) {
            public = chapter!!.public_
            edTitleChapter.setText(chapter?.titleChapters)
            edDescription.setText(HtmlManager.getFromHtml(chapter?.shotDescribe!!).trim())
            editContent.setText(HtmlManager.getFromHtml(chapter?.context!!).trim())
            edNumber.setText(chapter?.number.toString())
        }
        oldChapter = createNewChapterForOldAndNew()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu_save_share, menu)
        return super.onCreateOptionsMenu(menu)
    }


    @SuppressLint("ObsoleteSdkInt")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // когда нажимаем на кнопку save, передаём результат
        if (item.itemId == R.id.id_save) {
            oldChapter = createNewChapterForOldAndNew()
            if (chapter == null) {
                setMainResult()
            } else {
                val tempChapter = updateChapter() // иначе, редактируем существующую заметку
                dialogSaveChapter(
                    resources.getString(R.string.sure_save_chapter),
                    this@NewChapterActivity,
                    false,
                    mainViewModel,
                    tempChapter!!
                )
            }

        } else if (item.itemId == R.id.id_pdf) {
            // хочу сделать сохранение в PDF
            val string = makeShareText(
                createNewChapterForShare(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString(),
                this
            )
            var titleTemp = chapter?.titleChapters.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                //
                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(this@NewChapterActivity)
                    val strMessage = savePdf(title, string, this@NewChapterActivity)
                    dialog.dismiss()
                    createDialogInfo(strMessage, this@NewChapterActivity)
                }
                //
            } else {

                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED
                ) {
                    //permission was not granted, request it
                    val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permissions, STORAGE_CODE)
                } else {
                    //permission already granted, call savePdf() method
                    job = CoroutineScope(Dispatchers.Main).launch {
                        val dialog = ProgressDialog.createProgressDialog(this@NewChapterActivity)
                        val strMessage = savePdf(title, string, this@NewChapterActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewChapterActivity)
                    }
                }
            }

        } else if (item.itemId == R.id.id_share) {

            // Вызываем диалог выбора приложений для отправки текста поста в другое приложение
            val shareIntent = Intent.createChooser(
                ShareHelperChapter.shareChapter(
                    createNewChapterForShare(),
                    book?.titleBook.toString(),
                    book?.nameAuthor.toString(),
                    this
                ),
                "Share by"
            )
            startActivity(shareIntent)

        } else if (item.itemId == R.id.id_txt) {

            // хочу сделать сохранение в TXT
            val string = makeShareText(
                createNewChapterForShare(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString(),
                this
            )
            var titleTemp = chapter?.titleChapters.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(this@NewChapterActivity)
                    val strMessage = saveTxt(title, string, this@NewChapterActivity)
                    dialog.dismiss()
                    createDialogInfo(strMessage, this@NewChapterActivity)
                }
            } else {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED
                ) {
                    //permission was not granted, request it
                    val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permissions, STORAGE_CODE)
                } else {
                    //permission already granted, call saveTxt() method
                    job = CoroutineScope(Dispatchers.Main).launch {
                        val dialog = ProgressDialog.createProgressDialog(this@NewChapterActivity)
                        val strMessage = saveTxt(title, string, this@NewChapterActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewChapterActivity)
                    }
                }
            }
        } else if (item.itemId == R.id.id_docx) {
            val string = makeShareText(
                createNewChapterForShare(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString(),
                this
            )
            var titleTemp = chapter?.titleChapters.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            job = CoroutineScope(Dispatchers.Main).launch {
                val dialog = ProgressDialog.createProgressDialog(this@NewChapterActivity)
                val strMessage = saveDocx(title, string, this@NewChapterActivity)
                dialog.dismiss()
                createDialogInfo(strMessage, this@NewChapterActivity)
            }

        } else if (item.itemId == R.id.id_extract) {
            // извлечение текста из pdf
            launcherPDF.launch("application/pdf") // application/pdf

        } else if (item.itemId == R.id.id_extract_docx) {
            // извлечение текста из docx
            launcherDOCX.launch("application/docx") // application/pdf

        } else if (item.itemId == R.id.id_extract_txt) {
            // извлечение текста из текстового файла
            launcherTXT.launch("text/plain") //"text/plain"

        } else if (item.itemId == android.R.id.home) { // нажимаем на кнопку стрелка на верхнем меню
            newChapter = createNewChapterForOldAndNew()
            if (newChapter != oldChapter) {
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
                    val dialog = ProgressDialog.createProgressDialog(this@NewChapterActivity)

                    val text = extractDocx(
                        pathBuff,
                        binding.editContent,
                        DSQLITE_MAX_LENGTH,
                        this@NewChapterActivity,
                    )
                    binding.editContent.setText(text)
                    dialog.dismiss()
                    binding.editContent.setSelection(binding.editContent.length())

                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewChapterActivity)
            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewChapterActivity
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
                //Log.d("MyLog", "pathBuff ${pathBuff}")


                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(this@NewChapterActivity)

                    val text = extractTxt(
                        pathBuff,
                        binding.editContent,
                        DSQLITE_MAX_LENGTH,
                        this@NewChapterActivity,
                    )
                    binding.editContent.setText(text)
                    dialog.dismiss()
                    binding.editContent.setSelection(binding.editContent.length())

                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewChapterActivity)
            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewChapterActivity
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
                    val dialog = ProgressDialog.createProgressDialogExtPdf(this@NewChapterActivity)

                    val text = extractPdf(
                        pathBuff,
                        binding.editContent,
                        DSQLITE_MAX_LENGTH,
                        this@NewChapterActivity,
                    )
                    binding.editContent.setText(text)
                    dialog.dismiss()
                    binding.editContent.setSelection(binding.editContent.length())

                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewChapterActivity)
            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewChapterActivity
                )
            }
        }
    }


    // нажимаем на открыть главу во фрагменте со списком глав,
// если оттуда ничего не передали, то значит мы в этот момент создаем новую главу,
// если передали, то значит открываем главу на редактирование и передаем данные по данной главе
// и заполняем все поля в activity_new_chapter.xml
    private fun getChapter() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            book = intent.getSerializableExtra(
                ChapterListFragment.TITLE_BOOK_KEY,
                BookEntity7::class.java
            )
            chapter = intent.getSerializableExtra(
                ChapterListFragment.NEW_NOTE_KEY,
                ChapterEntity2::class.java
            )
            if (chapter != null) {
                public = chapter!!.public_
            }
            //Log.d("MyLog", book.toString())
            fillChapter()
        } else {
            val sBook = intent.getSerializableExtra(ChapterListFragment.TITLE_BOOK_KEY)
            val sChapter = intent.getSerializableExtra(ChapterListFragment.NEW_NOTE_KEY)

            book = sBook as BookEntity7
            if (sChapter != null) {
                chapter = sChapter as ChapterEntity2
                public = chapter!!.public_
                fillChapter()
            }
        }

    }

    // для передаваемого результат сделаем отдельную функцию
    private fun setMainResult() {
        var editState = "new"
        val tempChapter = if (chapter == null) {
            createNewChapter() // то добавляем новую заметку в базу данных
        } else {
            editState = "update"
            updateChapter() // иначе, редактируем существующую заметку
        }


        val i = Intent().apply {
            putExtra(ChapterListFragment.NEW_NOTE_KEY, tempChapter)
            putExtra(ChapterListFragment.EDIT_STATE_KEY, editState)
            putExtra(ChapterListFragment.TITLE_BOOK_KEY, book)
        }

        DialogSaveAndGetOut(this@NewChapterActivity, i)
    }


    private fun updateChapter(): ChapterEntity2? = with(binding) {

        var textDesc = HtmlManager.toHtml(binding.edDescription.text)
        if (textDesc.length > 35000)
            textDesc = textDesc.substring(0, 35000)

        var textCont = HtmlManager.toHtml(binding.editContent.text)
        if (textCont.length > 500000)
            textCont = textCont.substring(0, 500000)

        return chapter?.copy(
            titleChapters = edTitleChapter.text.toString(),
            shotDescribe = textDesc,
            context = textCont,
            number = edNumber.text.toString().toInt(),
        )
    }

    // функция заполняющая наш ChaptersEntity
    private fun createNewChapter(): ChapterEntity2 {
        var number = 0
        if (binding.edNumber.text.toString() != "") number =
            binding.edNumber.text.toString().toInt()

        var textDesc = HtmlManager.toHtml(binding.edDescription.text)
        if (textDesc.length > 35000)
            textDesc = textDesc.substring(0, 35000)

        var textCont = HtmlManager.toHtml(binding.editContent.text)
        if (textCont.length > 500000)
            textCont = textCont.substring(0, 500000)

        return ChapterEntity2(
            null,
            book?.id!!,
            binding.edTitleChapter.text.toString(),
            textDesc,
            textCont,
            getCurrentTime(),
            number,
            public,
            0L,
            "",
            null,
        )
    }

    // функция заполняющая наш ChaptersEntity
    private fun createNewChapterForOldAndNew(): ChapterEntity2 {
        var number = 0
        if (binding.edNumber.text.toString() != "") number =
            binding.edNumber.text.toString().toInt()

        var textDesc = HtmlManager.toHtml(binding.edDescription.text)
        if (textDesc.length > 35000)
            textDesc = textDesc.substring(0, 35000)

        var textCont = HtmlManager.toHtml(binding.editContent.text)
        if (textCont.length > 500000)
            textCont = textCont.substring(0, 500000)

        return ChapterEntity2(
            null,
            book?.id!!,
            binding.edTitleChapter.text.toString(),
            textDesc,
            textCont,
            "01:01:01 - 01/01/2024",
            number,
            public,
            0L,
            "",
            null,
        )
    }

    // функция заполняющая наш ChaptersEntity для поделиться
    private fun createNewChapterForShare(): ChapterEntity2 {
        var number = 0
        if (binding.edNumber.text.toString() != "") number =
            binding.edNumber.text.toString().toInt()

        return ChapterEntity2(
            chapter?.id,
            book?.id!!,
            binding.edTitleChapter.text.toString(),
            binding.edDescription.text.toString(),
            binding.editContent.text.toString(),
            getCurrentTime(),
            number,
            public,
            0L,
            "",
            null,
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
        edTitleChapter.setTextSize(pref?.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT))
        edDescription.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        editContent.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        titleBook.setTextSize(pref?.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT))

        tw1.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        tw2.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        tw3.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        tw4.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textComm0.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textComm1.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textComm2.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textComm3.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textComm4.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textComm5.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {
        editContent.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )
        edDescription.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )
        edTitleChapter.setTypeface(
            pref?.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )
        titleBook.setTypeface(
            pref?.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )
        edNumber.setTypeface(
            pref?.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )

        tw1.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )
        tw2.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )
        tw3.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )
        tw4.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )
        textComm0.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )
        textComm1.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )
        textComm2.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )
        textComm3.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )
        textComm4.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )
        textComm5.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )

        val font: Typeface? = typeface(
            pref?.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@NewChapterActivity
        )
        setTitleActionBar(resources.getString(R.string.app_name), font, supportActionBar)
    }

    private fun openDocxFile(file: File) {
        val fileUri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".provider", file
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            fileUri,
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            // Handle the error - no application can handle the .docx file
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}


