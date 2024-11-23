package elena.altair.note.activities.books

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import elena.altair.note.R
import elena.altair.note.databinding.ActivityNewTermBinding
import elena.altair.note.viewmodel.MainViewModel
import elena.altair.note.dialoghelper.DialogInfo.createDialogInfo
import elena.altair.note.dialoghelper.DialogSave.DialogSaveAndGetOut
import elena.altair.note.dialoghelper.DialogSave.dialogSaveTerm
import elena.altair.note.dialoghelper.ProgressDialog
import elena.altair.note.etities.BookEntity4
import elena.altair.note.etities.TermEntity2
import elena.altair.note.fragments.books.TermListFragment
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
import elena.altair.note.utils.share.ShareHelperTerm
import elena.altair.note.utils.share.ShareHelperTerm.makeShareText
import elena.altair.note.utils.text.TextStyle.setBoldForSelectedText
import elena.altair.note.utils.text.TextStyle.setColorForSelectedText
import elena.altair.note.utils.text.TextStyle.setItalicForSelectedText
import elena.altair.note.utils.text.TextStyle.setStrikethroughForSelectedText
import elena.altair.note.utils.text.TextStyle.setUnderlineForSelectedText
import elena.altair.note.utils.text.textRedactor.HtmlManager
import elena.altair.note.utils.text.textRedactor.MyTouchListener
import elena.altair.note.utils.theme.ThemeUtils.getSelectedTheme2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewTermActivity : AppCompatActivity() {

    private lateinit var defPref: SharedPreferences
    private var pref: SharedPreferences? = null
    private lateinit var binding: ActivityNewTermBinding
    private var book: BookEntity4? = null
    private var term: TermEntity2? = null
    private val mainViewModel: MainViewModel by viewModels()
    private val STORAGE_CODE: Int = 100
    private val DSQLITE_MAX_LENGTH = 5000
    private var job: Job? = null
    private var oldTerm: TermEntity2? = null
    private var newTerm: TermEntity2? = null
    //private val mainViewModel: MainViewModel by viewModels {
    //MainViewModel.MainViewModalFactory((this.applicationContext as MainApp).database)
    //}

    override fun onCreate(savedInstanceState: Bundle?) {
        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme2(defPref))

        super.onCreate(savedInstanceState)

        binding = ActivityNewTermBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // активируем стрелку на верхнем меню
        actionBarSetting()

        getTerm()

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

        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            PackageManager.PERMISSION_GRANTED
        )

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                newTerm = createNewTerm()
                if (newTerm != oldTerm) {
                    setMainResult()
                } else
                    finish()
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        // инициализируем слушатель нажатий для перетаскивания элементов
        // в нашем случае для перетаскивания панельки с палитрой
        binding.colorPicker.setOnTouchListener(MyTouchListener())
        pref = PreferenceManager.getDefaultSharedPreferences(this)
    }

    // активируем стрелку на верхнем меню
    private fun actionBarSetting() {
        val ab = supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // заполнение полей в разметке при редактировании
    private fun fillTerm() = with(binding) {
        titleBook.text = book?.titleBook
        if (term != null) {
            edTitleTerm.setText(term?.titleTerm)
            edTermContent.setText(HtmlManager.getFromHtml(term?.interpretationTerm).trim())
        }
        oldTerm = createNewTerm()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu_save_share, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // когда нажимаем на кнопку save, передаём результат
        if (item.itemId == R.id.id_save) {
            oldTerm = createNewTerm()
            if (term == null) {
                setMainResult()
            } else {
                val tempTerm = updateTerm() // иначе, редактируем существующую заметку
                dialogSaveTerm(
                    resources.getString(R.string.sure_save_term),
                    this@NewTermActivity,
                    false,
                    mainViewModel,
                    tempTerm!!
                )
            }
        } else if (item.itemId == R.id.id_share) {

            // Вызываем диалог выбора приложений для отправки текста поста в другое приложение
            val shareIntent = Intent.createChooser(
                ShareHelperTerm.shareTerm(
                    createNewTermForShare(),
                    book?.titleBook.toString(),
                    this
                ),
                "Share by"
            )
            startActivity(shareIntent)
        } else if (item.itemId == R.id.id_pdf) {
            // хочу сделать сохранение в PDF
            val string = makeShareText(
                createNewTermForShare(),
                book?.titleBook.toString(),
                this
            )

            var titleTemp = term?.titleTerm.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(this@NewTermActivity)
                    val strMessage = savePdf(title, string, this@NewTermActivity)
                    dialog.dismiss()
                    createDialogInfo(strMessage, this@NewTermActivity)
                }
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
                        val dialog = ProgressDialog.createProgressDialog(this@NewTermActivity)
                        val strMessage = savePdf(title, string, this@NewTermActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewTermActivity)
                    }
                }
            }
        } else if (item.itemId == R.id.id_txt) {

            // хочу сделать сохранение в TXT
            val string = makeShareText(
                createNewTermForShare(),
                book?.titleBook.toString(),
                this
            )
            var titleTemp = term?.titleTerm.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(this@NewTermActivity)
                    val strMessage = saveTxt(title, string, this@NewTermActivity)
                    dialog.dismiss()
                    createDialogInfo(strMessage, this@NewTermActivity)
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
                        val dialog = ProgressDialog.createProgressDialog(this@NewTermActivity)
                        val strMessage = saveTxt(title, string, this@NewTermActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewTermActivity)
                    }
                }
            }
        } else if (item.itemId == R.id.id_docx) {
            val string = makeShareText(
                createNewTermForShare(),
                book?.titleBook.toString(),
                this
            )
            var titleTemp = term?.titleTerm.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            job = CoroutineScope(Dispatchers.Main).launch {
                val dialog = ProgressDialog.createProgressDialog(this@NewTermActivity)
                val strMessage = saveDocx(title, string, this@NewTermActivity)
                dialog.dismiss()
                createDialogInfo(strMessage, this@NewTermActivity)
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
            newTerm = createNewTerm()
            if (newTerm != oldTerm) {
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
                    val dialog = ProgressDialog.createProgressDialog(this@NewTermActivity)

                    val text = extractDocx(
                        pathBuff,
                        binding.edTermContent,
                        DSQLITE_MAX_LENGTH,
                        this@NewTermActivity
                    )

                    binding.edTermContent.setText(text)
                    dialog.dismiss()
                    binding.edTermContent.setSelection(binding.edTermContent.length())
                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewTermActivity)
            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewTermActivity
                )
            }
        }
    }


    // извлечение текст из txt файла
    private val launcherTXT = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val cursor = contentResolver.query(uri!!, null, null, null, null)
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
                    val dialog = ProgressDialog.createProgressDialog(this@NewTermActivity)

                    val text = extractTxt(
                        pathBuff,
                        binding.edTermContent,
                        DSQLITE_MAX_LENGTH,
                        this@NewTermActivity
                    )

                    binding.edTermContent.setText(text)
                    dialog.dismiss()
                    binding.edTermContent.setSelection(binding.edTermContent.length())
                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewTermActivity)
            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewTermActivity
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

                if (nameFile.contains(".pdf")) {
                    // поместим выбранный файл в кеш приложения и считаем текст из него
                    val pathBuff = getDriveFilePath(uri, this, nameFile)

                    job = CoroutineScope(Dispatchers.Main).launch {
                        val dialog = ProgressDialog.createProgressDialogExtPdf(this@NewTermActivity)

                        val text = extractPdf(
                            pathBuff,
                            binding.edTermContent,
                            DSQLITE_MAX_LENGTH,
                            this@NewTermActivity
                        )

                        binding.edTermContent.setText(text)
                        dialog.dismiss()
                        binding.edTermContent.setSelection(binding.edTermContent.length())
                    }
                    createDialogInfo(resources.getString(R.string.text_min), this@NewTermActivity)
                } else {
                    createDialogInfo(
                        resources.getString(R.string.different_format),
                        this@NewTermActivity
                    )
                }
            }
        }
    }


    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            STORAGE_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    val string = makeShareText(
                        createNewTermForShare(),
                        book?.titleBook.toString(),
                        this
                    )
                    var titleTemp = term?.titleTerm.toString()
                    if (titleTemp.length > 10) {
                        titleTemp = titleTemp.substring(0, 10)
                    }
                    val title = titleTemp
                    //permission from popup was granted, call savePdf() method
                    job = CoroutineScope(Dispatchers.Main).launch {
                        val dialog = ProgressDialog.createProgressDialog(this@NewTermActivity)
                        val strMessage = savePdf(title, string, this@NewTermActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewTermActivity)
                    }
                    job = CoroutineScope(Dispatchers.Main).launch {
                        val dialog = ProgressDialog.createProgressDialog(this@NewTermActivity)
                        val strMessage = saveTxt(title, string, this@NewTermActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewTermActivity)
                    }
                } else {
                    //permission from popup was denied, show error message
                    createDialogInfo(
                        "NewTermActivity " + resources.getString(R.string.permission_denied),
                        this
                    )
                    //Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // нажимаем на открыть термин во фрагменте со списком терминов,
    // если оттуда ничего не передали, то значит мы в этот момент создаем новый термин,
    // если передали, то значит открываем термин на редактирование и передаем данные по данному термину
    // и заполняем все поля в activity_new_term.xml
    private fun getTerm() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            book = intent.getSerializableExtra(
                TermListFragment.TITLE_BOOK_KEY,
                BookEntity4::class.java
            )
            term = intent.getSerializableExtra(
                TermListFragment.NEW_NOTE_KEY,
                TermEntity2::class.java
            )

            fillTerm()
        } else {
            val sBook = intent.getSerializableExtra(TermListFragment.TITLE_BOOK_KEY)
            val sTerm = intent.getSerializableExtra(TermListFragment.NEW_NOTE_KEY)

            book = sBook as BookEntity4
            if (sTerm != null) {
                term = sTerm as TermEntity2
                fillTerm()
            }
        }

    }

    // для передаваемого результат сделаем отдельную функцию
    private fun setMainResult() {
        var editState = "new"
        val tempTerm = if (term == null) {
            createNewTerm() // то добавляем новую заметку в базу данных
        } else {
            editState = "update"
            updateTerm() // иначе, редактируем существующую заметку
        }


        val i = Intent().apply {
            putExtra(TermListFragment.NEW_NOTE_KEY, tempTerm)
            putExtra(TermListFragment.EDIT_STATE_KEY, editState)
            putExtra(TermListFragment.TITLE_BOOK_KEY, book)
        }

        DialogSaveAndGetOut(this@NewTermActivity, i)
    }

    private fun updateTerm(): TermEntity2? = with(binding) {

        var text = HtmlManager.toHtml(binding.edTermContent.text)
        if (text.length > 70000)
            text = text.substring(0, 70000)


        return term?.copy(
            titleTerm = edTitleTerm.text.toString(),
            interpretationTerm = text,
        )
    }

    // функция заполняющая наш TermEntity
    private fun createNewTerm(): TermEntity2 {

        var text = HtmlManager.toHtml(binding.edTermContent.text)
        if (text.length > 70000)
            text = text.substring(0, 70000)

        return TermEntity2(
            null,
            book?.id!!,
            binding.edTitleTerm.text.toString(),
            text,
            0,
            "0",
            null,
        )
    }

    // функция заполняющая наш TermEntity
    private fun createNewTermForShare(): TermEntity2 {
        return TermEntity2(
            null,
            book?.id!!,
            binding.edTitleTerm.text.toString(),
            binding.edTermContent.text.toString(),
            0,
            "0",
            null,
        )
    }


    fun onClickImageColor() = with(binding) {
        imageColor.setOnClickListener {
            // проверяем видимость нашего layout с палитрой
            if (binding.colorPicker.isShown) {
                closeColorPicker() // закрываем нашу палитру
            } else {
                openColorPicker() // открываем нашу палитру
            }
        }
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

    private fun onClickImageStrikethrough() = with(binding) {
        imageStrikethrough.setOnClickListener {
            setStrikethroughForSelectedText(
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent
            )
        }
    }

    private fun onClickImageUnderlined() = with(binding) {
        imageUnderlined.setOnClickListener {
            setUnderlineForSelectedText(
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent
            )
        }
    }

    private fun onClickImageItalic() = with(binding) {
        imageItalic.setOnClickListener {
            setItalicForSelectedText(
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent
            )
        }
    }

    private fun onClickImageBold() = with(binding) {
        imageBold.setOnClickListener {
            setBoldForSelectedText(
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent
            )
        }
    }

    // слушатель нажатий для палитры цветов
    private fun onClickColorPicker() = with(binding) {
        imRed.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_red,
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent,
                this@NewTermActivity
            )
        }
        imOrangeDark.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_orange_dark,
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent,
                this@NewTermActivity
            )
        }
        imOrange.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_orange,
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent,
                this@NewTermActivity
            )
        }
        imYellow.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_yellow,
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent,
                this@NewTermActivity
            )
        }
        imPurple.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_purple,
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent,
                this@NewTermActivity
            )
        }
        imBlue.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_blue,
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent,
                this@NewTermActivity
            )
        }
        imGreenSee.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_green_see,
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent,
                this@NewTermActivity
            )
        }
        imGreen.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_green,
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent,
                this@NewTermActivity
            )
        }
        imBlack.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_black,
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent,
                this@NewTermActivity
            )
        }
        imGrey.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_grey,
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent,
                this@NewTermActivity
            )
        }
        imGreyLight.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_grey_light,
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent,
                this@NewTermActivity
            )
        }
        imWhite.setOnClickListener {
            setColorForSelectedText(
                R.color.picker_white,
                edTermContent.selectionStart,
                edTermContent.selectionEnd,
                edTermContent,
                this@NewTermActivity
            )
        }
    }


    // функция для выбора размера текста
    private fun setTextSize() = with(binding) {
        edTitleTerm.setTextSize(pref?.getString("title_size_key", "18"))
        edTermContent.setTextSize(pref?.getString("content_size_key", "18"))

        titleBook.setTextSize(pref?.getString("title_size_key", "18"))

        tw1.setTextSize(pref?.getString("comments_size_key", "16"))
        tw2.setTextSize(pref?.getString("comments_size_key", "16"))
    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {
        edTermContent.setTypeface(
            pref?.getString("font_family_content_key", "sans-serif"),
            this@NewTermActivity
        )
        titleBook.setTypeface(
            pref?.getString("font_family_title_key", "sans-serif"),
            this@NewTermActivity
        )
        edTitleTerm.setTypeface(
            pref?.getString("font_family_title_key", "sans-serif"),
            this@NewTermActivity
        )

        tw1.setTypeface(
            pref?.getString("font_family_comment_key", "sans-serif"),
            this@NewTermActivity
        )
        tw2.setTypeface(
            pref?.getString("font_family_comment_key", "sans-serif"),
            this@NewTermActivity
        )

        val font: Typeface? = typeface(
            pref?.getString("font_family_title_key", "sans-serif"),
            this@NewTermActivity
        )
        setTitleActionBar(resources.getString(R.string.app_name), font, supportActionBar)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

}