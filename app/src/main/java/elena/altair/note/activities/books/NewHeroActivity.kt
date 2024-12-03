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
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import elena.altair.note.databinding.ActivityNewHeroBinding
import elena.altair.note.dialoghelper.DialogInfo.createDialogInfo
import elena.altair.note.dialoghelper.DialogSave.DialogSaveAndGetOut
import elena.altair.note.dialoghelper.DialogSave.dialogSaveHero
import elena.altair.note.dialoghelper.ProgressDialog
import elena.altair.note.etities.BookEntity7
import elena.altair.note.etities.HeroEntity2
import elena.altair.note.fragments.books.HeroListFragment
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
import elena.altair.note.utils.share.ShareHelperHero
import elena.altair.note.utils.share.ShareHelperHero.makeShareText
import elena.altair.note.utils.theme.ThemeUtils.getSelectedTheme
import elena.altair.note.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewHeroActivity : AppCompatActivity() {

    private lateinit var defPref: SharedPreferences
    private var pref: SharedPreferences? = null
    private lateinit var binding: ActivityNewHeroBinding
    private var book: BookEntity7? = null
    private var hero: HeroEntity2? = null
    private val mainViewModel: MainViewModel by viewModels()
    private val STORAGE_CODE: Int = 100
    private val DSQLITE_MAX_LENGTH = 2000
    private var job: Job? = null
    private var oldHero: HeroEntity2? = null
    private var newHero: HeroEntity2? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme(defPref))

        super.onCreate(savedInstanceState)

        binding = ActivityNewHeroBinding.inflate(layoutInflater)
        setContentView(binding.root)



        getHero()

        init()
        // зададим настройки текста
        setTextSize()
        setFontFamily()
        // активируем стрелку на верхнем меню
        actionBarSetting()
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            PackageManager.PERMISSION_GRANTED
        )

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                newHero = createNewHero()
                if (newHero != oldHero) {
                    setMainResult()
                } else
                    finish()
            }
        })
    }

    private fun init() {
        pref = PreferenceManager.getDefaultSharedPreferences(this)

        // подключим наш собсвенный Action Bar к нашему активити
        setSupportActionBar(binding.toolbar)
    }

    // активируем стрелку на верхнем меню
    private fun actionBarSetting() {
        val ab = supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // заполнение полей в разметке при редактировании
    private fun fillHero() = with(binding) {
        titleBook.text = book?.titleBook
        if (hero != null) {
            edNameHero.setText(hero?.desc1)
            edText2.setText(hero?.desc2)
            edText3.setText(hero?.desc3)
            edText4.setText(hero?.desc4)
            edText5.setText(hero?.desc5)
            edText6.setText(hero?.desc6)
            edText7.setText(hero?.desc7)
            edText8.setText(hero?.desc8)
            edText9.setText(hero?.desc9)
            edText10.setText(hero?.desc10)
            edText11.setText(hero?.desc11)
            edText12.setText(hero?.desc12)
            edText13.setText(hero?.desc13)
            edText14.setText(hero?.desc14)
            edText15.setText(hero?.desc15)
            edText16.setText(hero?.desc16)
            edText17.setText(hero?.desc17)
            edText18.setText(hero?.desc18)
            edText19.setText(hero?.desc19)
            edText20.setText(hero?.desc20)
            edText21.setText(hero?.desc21)
        }
        oldHero = createNewHero()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu_save_share, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // когда нажимаем на кнопку save, передаём результат
        if (item.itemId == R.id.id_save) {
            oldHero = createNewHero()
            if (hero == null) {
                setMainResult()
            } else {
                val tempHero = updateHero() // иначе, редактируем существующую заметку
                dialogSaveHero(
                    resources.getString(R.string.sure_save_hero),
                    this@NewHeroActivity,
                    false,
                    mainViewModel,
                    tempHero!!
                )
            }
        } else if (item.itemId == R.id.id_share) {

            // Вызываем диалог выбора приложений для отправки текста поста в другое приложение
            val shareIntent = Intent.createChooser(
                ShareHelperHero.shareHero(
                    createNewHero(),
                    book?.titleBook.toString(),
                    book?.nameAuthor.toString(),
                    this
                ),
                "Share by"
            )
            startActivity(shareIntent)

        } else if (item.itemId == R.id.id_pdf) {
            // хочу сделать сохранение в PDF
            val string = makeShareText(
                createNewHero(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString(),
                this
            )
            var titleTemp = hero?.desc1.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(this@NewHeroActivity)
                    val strMessage = savePdf(title, string, this@NewHeroActivity)
                    dialog.dismiss()
                    createDialogInfo(strMessage, this@NewHeroActivity)
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
                        val dialog = ProgressDialog.createProgressDialog(this@NewHeroActivity)
                        val strMessage = savePdf(title, string, this@NewHeroActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewHeroActivity)
                    }
                }
            }
        } else if (item.itemId == R.id.id_txt) {

            // хочу сделать сохранение в TXT
            val string = makeShareText(
                createNewHero(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString(),
                this
            )
            var titleTemp = hero?.desc1.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(this@NewHeroActivity)
                    val strMessage = saveTxt(title, string, this@NewHeroActivity)
                    dialog.dismiss()
                    createDialogInfo(strMessage, this@NewHeroActivity)
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
                        val dialog = ProgressDialog.createProgressDialog(this@NewHeroActivity)
                        val strMessage = saveTxt(title, string, this@NewHeroActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewHeroActivity)
                    }
                }
            }
        } else if (item.itemId == R.id.id_docx) {
            val string = makeShareText(
                createNewHero(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString(),
                this
            )
            var titleTemp = hero?.desc1.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            job = CoroutineScope(Dispatchers.Main).launch {
                val dialog = ProgressDialog.createProgressDialog(this@NewHeroActivity)
                val strMessage = saveDocx(title, string, this@NewHeroActivity)
                dialog.dismiss()
                createDialogInfo(strMessage, this@NewHeroActivity)
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
            newHero = createNewHero()
            if (newHero != oldHero) {
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
                    val dialog = ProgressDialog.createProgressDialog(this@NewHeroActivity)

                    val text =
                        extractDocx(
                            pathBuff,
                            binding.edText2,
                            DSQLITE_MAX_LENGTH,
                            this@NewHeroActivity
                        )


                    binding.edText2.setText(text)
                    dialog.dismiss()
                    binding.edText2.setSelection(binding.edText2.length())

                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewHeroActivity)

            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewHeroActivity
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
                    val dialog = ProgressDialog.createProgressDialog(this@NewHeroActivity)

                    val text =
                        extractTxt(
                            pathBuff,
                            binding.edText2,
                            DSQLITE_MAX_LENGTH,
                            this@NewHeroActivity
                        )


                    binding.edText2.setText(text)
                    dialog.dismiss()
                    binding.edText2.setSelection(binding.edText2.length())

                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewHeroActivity)

            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewHeroActivity
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
                    val dialog = ProgressDialog.createProgressDialogExtPdf(this@NewHeroActivity)

                    val text =
                        extractPdf(
                            pathBuff,
                            binding.edText2,
                            DSQLITE_MAX_LENGTH,
                            this@NewHeroActivity
                        )


                    binding.edText2.setText(text)
                    dialog.dismiss()
                    binding.edText2.setSelection(binding.edText2.length())

                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewHeroActivity)
            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewHeroActivity
                )
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
                        createNewHero(),
                        book?.titleBook.toString(),
                        book?.nameAuthor.toString(),
                        this
                    )

                    var titleTemp = hero?.desc1.toString()
                    if (titleTemp.length > 10) {
                        titleTemp = titleTemp.substring(0, 10)
                    }
                    val title = titleTemp

                    //permission from popup was granted, call savePdf() method
                    job = CoroutineScope(Dispatchers.Main).launch {
                        val dialog = ProgressDialog.createProgressDialog(this@NewHeroActivity)
                        val strMessage = savePdf(title, string, this@NewHeroActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewHeroActivity)
                    }
                    job = CoroutineScope(Dispatchers.Main).launch {
                        val dialog = ProgressDialog.createProgressDialog(this@NewHeroActivity)
                        val strMessage = saveTxt(title, string, this@NewHeroActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewHeroActivity)
                    }
                } else {
                    //permission from popup was denied, show error message
                    createDialogInfo(
                        "NewHeroActivity " + resources.getString(R.string.permission_denied),
                        this
                    )
                    //Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // нажимаем на открыть героя во фрагменте со списком героев,
    // если оттуда ничего не передали, то значит мы в этот момент создаем нового героя,
    // если передали, то значит открываем героя на редактирование и передаем данные по данному герою
    // и заполняем все поля в activity_new_hero.xml
    private fun getHero() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            book = intent.getSerializableExtra(
                HeroListFragment.TITLE_BOOK_KEY,
                BookEntity7::class.java
            )
            hero = intent.getSerializableExtra(
                HeroListFragment.NEW_NOTE_KEY,
                HeroEntity2::class.java
            )

            fillHero()
        } else {
            val sBook = intent.getSerializableExtra(HeroListFragment.TITLE_BOOK_KEY)
            val sHero = intent.getSerializableExtra(HeroListFragment.NEW_NOTE_KEY)

            book = sBook as BookEntity7
            if (sHero != null) {
                hero = sHero as HeroEntity2
                fillHero()
            }
        }

    }

    // для передаваемого результат сделаем отдельную функцию
    private fun setMainResult() {
        var editState = "new"
        val tempHero = if (hero == null) {
            createNewHero() // то добавляем новую заметку в базу данных
        } else {
            editState = "update"
            updateHero() // иначе, редактируем существующую заметку
        }


        val i = Intent().apply {
            putExtra(HeroListFragment.NEW_NOTE_KEY, tempHero)
            putExtra(HeroListFragment.EDIT_STATE_KEY, editState)
            putExtra(HeroListFragment.TITLE_BOOK_KEY, book)
        }

        DialogSaveAndGetOut(this@NewHeroActivity, i)
    }

    private fun updateHero(): HeroEntity2? = with(binding) {
        return hero?.copy(
            desc1 = edNameHero.text.toString(),
            desc2 = edText2.text.toString(),
            desc3 = edText3.text.toString(),
            desc4 = edText4.text.toString(),
            desc5 = edText5.text.toString(),
            desc6 = edText6.text.toString(),
            desc7 = edText7.text.toString(),
            desc8 = edText8.text.toString(),
            desc9 = edText9.text.toString(),
            desc10 = edText10.text.toString(),
            desc11 = edText11.text.toString(),
            desc12 = edText12.text.toString(),
            desc13 = edText13.text.toString(),
            desc14 = edText14.text.toString(),
            desc15 = edText15.text.toString(),
            desc16 = edText16.text.toString(),
            desc17 = edText17.text.toString(),
            desc18 = edText18.text.toString(),
            desc19 = edText19.text.toString(),
            desc20 = edText20.text.toString(),
            desc21 = edText21.text.toString(),
        )
    }

    // функция заполняющая наш HeroEntity
    private fun createNewHero(): HeroEntity2 {
        return HeroEntity2(
            null,
            book?.id!!,
            binding.edNameHero.text.toString(),
            binding.edText2.text.toString(),
            binding.edText3.text.toString(),
            binding.edText4.text.toString(),
            binding.edText5.text.toString(),
            binding.edText6.text.toString(),
            binding.edText7.text.toString(),
            binding.edText8.text.toString(),
            binding.edText9.text.toString(),
            binding.edText10.text.toString(),
            binding.edText11.text.toString(),
            binding.edText12.text.toString(),
            binding.edText13.text.toString(),
            binding.edText14.text.toString(),
            binding.edText15.text.toString(),
            binding.edText16.text.toString(),
            binding.edText17.text.toString(),
            binding.edText18.text.toString(),
            binding.edText19.text.toString(),
            binding.edText20.text.toString(),
            binding.edText21.text.toString(),
            0,
            "0",
            null,
        )
    }


    // функция для выбора размера текста
    private fun setTextSize() = with(binding) {
        edNameHero.setTextSize(pref?.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT))
        edText2.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText3.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText4.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText5.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText6.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText7.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText8.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText9.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText10.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText11.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText12.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText13.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText14.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText15.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText16.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText17.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText18.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText19.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText20.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText21.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))

        nameHero.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        titleBook.setTextSize(pref?.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT))
        textView4.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView5.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView6.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView7.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView8.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView9.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView10.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView11.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView12.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView13.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView14.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView15.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView16.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView17.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView18.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView19.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView20.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView21.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView22.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView23.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {
        edText2.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText3.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText4.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText5.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText6.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText7.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText8.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText9.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText10.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText11.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText12.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText13.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText14.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText15.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText16.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText17.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText18.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText19.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText20.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edText21.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        edNameHero.setTypeface(
            pref?.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )

        titleBook.setTypeface(
            pref?.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )

        nameHero.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView4.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView5.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView6.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView7.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView8.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView9.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView10.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView11.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView12.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView13.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView14.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView15.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView16.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView17.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView18.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView19.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView20.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView21.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView22.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        textView23.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )

        val font: Typeface? = typeface(
            pref?.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@NewHeroActivity
        )
        setTitleActionBar(resources.getString(R.string.app_name), font, supportActionBar)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

}