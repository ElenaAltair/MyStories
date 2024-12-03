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
import elena.altair.note.databinding.ActivityNewPeopleBinding
import elena.altair.note.dialoghelper.DialogInfo.createDialogInfo
import elena.altair.note.dialoghelper.DialogSave.DialogSaveAndGetOut
import elena.altair.note.dialoghelper.DialogSave.dialogSavePeople
import elena.altair.note.dialoghelper.ProgressDialog
import elena.altair.note.etities.BookEntity7
import elena.altair.note.etities.PeopleEntity2
import elena.altair.note.fragments.books.PeopleListFragment
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
import elena.altair.note.utils.share.ShareHelperPeople
import elena.altair.note.utils.share.ShareHelperPeople.makeShareText
import elena.altair.note.utils.theme.ThemeUtils.getSelectedTheme
import elena.altair.note.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewPeopleActivity : AppCompatActivity() {

    private lateinit var defPref: SharedPreferences
    private var pref: SharedPreferences? = null
    private lateinit var binding: ActivityNewPeopleBinding
    private var book: BookEntity7? = null
    private var people: PeopleEntity2? = null
    private val mainViewModel: MainViewModel by viewModels()
    private val STORAGE_CODE: Int = 100
    private val DSQLITE_MAX_LENGTH = 2000
    private var job: Job? = null
    private var oldPeople: PeopleEntity2? = null
    private var newPeople: PeopleEntity2? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme(defPref))

        super.onCreate(savedInstanceState)

        binding = ActivityNewPeopleBinding.inflate(layoutInflater)
        setContentView(binding.root)



        getPeople()

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
                newPeople = createNewPeople()
                if (newPeople != oldPeople) {
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
    private fun fillPeople() = with(binding) {
        titleBook.text = book?.titleBook
        if (people != null) {
            edTitlePeople.setText(people?.titlePeople)
            edTerritoryResidence.setText(people?.territoryResidence)
            edFeaturesAppearance.setText(people?.featuresAppearance)
            edLanguage.setText(people?.language)
            edReligion.setText(people?.religion)
            edFeatures.setText(people?.features)
            edArt.setText(people?.art)
            edRole.setText(people?.role)
        }
        oldPeople = createNewPeople()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu_save_share, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // когда нажимаем на кнопку save, передаём результат
        if (item.itemId == R.id.id_save) {
            oldPeople = createNewPeople()
            if (people == null) {
                setMainResult()
            } else {
                val tempPeople = updatePeople() // иначе, редактируем существующую заметку
                dialogSavePeople(
                    resources.getString(R.string.sure_save_people),
                    this@NewPeopleActivity,
                    false,
                    mainViewModel,
                    tempPeople!!
                )
            }
        } else if (item.itemId == R.id.id_share) {

            // Вызываем диалог выбора приложений для отправки текста поста в другое приложение
            val shareIntent = Intent.createChooser(
                ShareHelperPeople.sharePeople(
                    createNewPeople(),
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
                createNewPeople(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString(),
                this
            )

            var titleTemp = people?.titlePeople.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(this@NewPeopleActivity)
                    val strMessage = savePdf(title, string, this@NewPeopleActivity)
                    dialog.dismiss()
                    createDialogInfo(strMessage, this@NewPeopleActivity)
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
                        val dialog = ProgressDialog.createProgressDialog(this@NewPeopleActivity)
                        val strMessage = savePdf(title, string, this@NewPeopleActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewPeopleActivity)
                    }
                }
            }
        } else if (item.itemId == R.id.id_txt) {

            // хочу сделать сохранение в TXT
            val string = makeShareText(
                createNewPeople(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString(),
                this
            )
            var titleTemp = people?.titlePeople.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(this@NewPeopleActivity)
                    val strMessage = saveTxt(title, string, this@NewPeopleActivity)
                    dialog.dismiss()
                    createDialogInfo(strMessage, this@NewPeopleActivity)
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
                        val dialog = ProgressDialog.createProgressDialog(this@NewPeopleActivity)
                        val strMessage = saveTxt(title, string, this@NewPeopleActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewPeopleActivity)
                    }
                }
            }
        } else if (item.itemId == R.id.id_docx) {
            val string = makeShareText(
                createNewPeople(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString(),
                this
            )
            var titleTemp = people?.titlePeople.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            job = CoroutineScope(Dispatchers.Main).launch {
                val dialog = ProgressDialog.createProgressDialog(this@NewPeopleActivity)
                val strMessage = saveDocx(title, string, this@NewPeopleActivity)
                dialog.dismiss()
                createDialogInfo(strMessage, this@NewPeopleActivity)
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
            newPeople = createNewPeople()
            if (newPeople != oldPeople) {
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
                    val dialog = ProgressDialog.createProgressDialog(this@NewPeopleActivity)

                    val text = extractDocx(
                        pathBuff,
                        binding.edTerritoryResidence,
                        DSQLITE_MAX_LENGTH,
                        this@NewPeopleActivity
                    )

                    binding.edTerritoryResidence.setText(text)
                    dialog.dismiss()
                    binding.edTerritoryResidence.setSelection(binding.edTerritoryResidence.length())
                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewPeopleActivity)
            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewPeopleActivity
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
                    val dialog = ProgressDialog.createProgressDialog(this@NewPeopleActivity)

                    val text = extractTxt(
                        pathBuff,
                        binding.edTerritoryResidence,
                        DSQLITE_MAX_LENGTH,
                        this@NewPeopleActivity
                    )

                    binding.edTerritoryResidence.setText(text)
                    dialog.dismiss()
                    binding.edTerritoryResidence.setSelection(binding.edTerritoryResidence.length())
                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewPeopleActivity)
            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewPeopleActivity
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
                    val dialog = ProgressDialog.createProgressDialogExtPdf(this@NewPeopleActivity)

                    val text = extractPdf(
                        pathBuff,
                        binding.edTerritoryResidence,
                        DSQLITE_MAX_LENGTH,
                        this@NewPeopleActivity
                    )

                    binding.edTerritoryResidence.setText(text)
                    dialog.dismiss()
                    binding.edTerritoryResidence.setSelection(binding.edTerritoryResidence.length())
                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewPeopleActivity)
            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewPeopleActivity
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
                        createNewPeople(),
                        book?.titleBook.toString(),
                        book?.nameAuthor.toString(),
                        this
                    )
                    var titleTemp = people?.titlePeople.toString()
                    if (titleTemp.length > 10) {
                        titleTemp = titleTemp.substring(0, 10)
                    }
                    val title = titleTemp
                    //permission from popup was granted, call savePdf() method
                    job = CoroutineScope(Dispatchers.Main).launch {
                        val dialog = ProgressDialog.createProgressDialog(this@NewPeopleActivity)
                        val strMessage = savePdf(title, string, this@NewPeopleActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewPeopleActivity)
                    }
                    job = CoroutineScope(Dispatchers.Main).launch {
                        val dialog = ProgressDialog.createProgressDialog(this@NewPeopleActivity)
                        val strMessage = saveTxt(title, string, this@NewPeopleActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewPeopleActivity)
                    }
                } else {
                    //permission from popup was denied, show error message
                    createDialogInfo(
                        "NewPeopleActivity " + resources.getString(R.string.permission_denied),
                        this
                    )
                    //Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // нажимаем на открыть народ во фрагменте со списком народов,
    // если оттуда ничего не передали, то значит мы в этот момент создаем новый народ,
    // если передали, то значит открываем народ на редактирование и передаем данные по данному народу
    // и заполняем все поля в activity_new_people.xml
    private fun getPeople() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            book = intent.getSerializableExtra(
                PeopleListFragment.TITLE_BOOK_KEY,
                BookEntity7::class.java
            )
            people = intent.getSerializableExtra(
                PeopleListFragment.NEW_NOTE_KEY,
                PeopleEntity2::class.java
            )

            fillPeople()
        } else {
            val sBook = intent.getSerializableExtra(PeopleListFragment.TITLE_BOOK_KEY)
            val sPeople = intent.getSerializableExtra(PeopleListFragment.NEW_NOTE_KEY)

            book = sBook as BookEntity7
            if (sPeople != null) {
                people = sPeople as PeopleEntity2
                fillPeople()
            }
        }

    }


    // для передаваемого результат сделаем отдельную функцию
    private fun setMainResult() {
        var editState = "new"
        val tempPeople = if (people == null) {
            createNewPeople() // то добавляем новую заметку в базу данных
        } else {
            editState = "update"
            updatePeople() // иначе, редактируем существующую заметку
        }


        val i = Intent().apply {
            putExtra(PeopleListFragment.NEW_NOTE_KEY, tempPeople)
            putExtra(PeopleListFragment.EDIT_STATE_KEY, editState)
            putExtra(PeopleListFragment.TITLE_BOOK_KEY, book)
        }

        DialogSaveAndGetOut(this@NewPeopleActivity, i)
    }

    private fun updatePeople(): PeopleEntity2? = with(binding) {
        return people?.copy(
            titlePeople = edTitlePeople.text.toString(),
            territoryResidence = edTerritoryResidence.text.toString(),
            featuresAppearance = edFeaturesAppearance.text.toString(),
            language = edLanguage.text.toString(),
            religion = edReligion.text.toString(),
            features = edFeatures.text.toString(),
            art = edArt.text.toString(),
            role = edRole.text.toString(),
        )
    }

    // функция заполняющая наш PeopleEntity
    private fun createNewPeople(): PeopleEntity2 {
        return PeopleEntity2(
            null,
            book?.id!!,
            binding.edTitlePeople.text.toString(),
            binding.edTerritoryResidence.text.toString(),
            binding.edFeaturesAppearance.text.toString(),
            binding.edLanguage.text.toString(),
            binding.edReligion.text.toString(),
            binding.edFeatures.text.toString(),
            binding.edArt.text.toString(),
            binding.edRole.text.toString(),
            0,
            "0",
            null,
        )
    }


    // функция для выбора размера текста
    private fun setTextSize() = with(binding) {
        edTitlePeople.setTextSize(pref?.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT))
        edTerritoryResidence.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edFeaturesAppearance.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edLanguage.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edReligion.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edFeatures.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edArt.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edRole.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))

        namePeople.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        titleBook.setTextSize(pref?.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT))
        textView4.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView5.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView6.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView7.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView8.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView9.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView10.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))

    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {
        edTerritoryResidence.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )
        edFeaturesAppearance.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )
        edLanguage.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )
        edReligion.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )
        edFeatures.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )
        edArt.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )
        edRole.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )

        edTitlePeople.setTypeface(
            pref?.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )

        titleBook.setTypeface(
            pref?.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )

        namePeople.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )
        textView4.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )
        textView5.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )
        textView6.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )
        textView7.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )
        textView8.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )
        textView9.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )
        textView10.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )

        val font: Typeface? = typeface(
            pref?.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@NewPeopleActivity
        )
        setTitleActionBar(resources.getString(R.string.app_name), font, supportActionBar)


    }

    override fun onDestroy() {
        super.onDestroy()
    }

}