package elena.altair.note.activities.books

import android.Manifest
import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
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
import elena.altair.note.databinding.ActivityNewLocationBinding
import elena.altair.note.dialoghelper.DialogInfo.createDialogInfo
import elena.altair.note.dialoghelper.DialogSave.DialogSaveAndGetOut
import elena.altair.note.dialoghelper.DialogSave.dialogSaveLocation
import elena.altair.note.dialoghelper.ProgressDialog
import elena.altair.note.etities.BookEntity7
import elena.altair.note.etities.LocationEntity2
import elena.altair.note.fragments.books.LocationListFragment
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
import elena.altair.note.utils.share.ShareHelperLocation
import elena.altair.note.utils.share.ShareHelperLocation.makeShareText
import elena.altair.note.utils.theme.ThemeUtils.getSelectedTheme
import elena.altair.note.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewLocationActivity : AppCompatActivity() {

    private lateinit var defPref: SharedPreferences
    private var pref: SharedPreferences? = null
    private lateinit var binding: ActivityNewLocationBinding
    private var book: BookEntity7? = null
    private var location: LocationEntity2? = null
    private val mainViewModel: MainViewModel by viewModels()
    private val STORAGE_CODE: Int = 100
    private val DSQLITE_MAX_LENGTH = 2000
    private var job: Job? = null
    private var oldLocation: LocationEntity2? = null
    private var newLocation: LocationEntity2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme(defPref))

        super.onCreate(savedInstanceState)

        binding = ActivityNewLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)



        getLocation()

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
                newLocation = createNewLocation()
                if (newLocation != oldLocation) {
                    setMainResult()
                } else
                    finish()
            }
        })

        requestMultiplePermissions.launch(
            arrayOf(
                CAMERA,
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
    private fun fillLocation() = with(binding) {
        titleBook.text = book?.titleBook
        if (location != null) {
            edTitleLocation.setText(location?.titleLocation)
            edGeography.setText(location?.geography)
            edPopulation.setText(location?.population)
            edPolitics.setText(location?.politics)
            edEconomy.setText(location?.economy)
            edReligion.setText(location?.religion)
            edHistory.setText(location?.history)
            edFeature.setText(location?.feature)
        }
        oldLocation = createNewLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu_save_share, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // когда нажимаем на кнопку save, передаём результат
        if (item.itemId == R.id.id_save) {
            oldLocation = createNewLocation()
            if (location == null) {
                setMainResult()
            } else {
                val tempLocation = updateLocation() // иначе, редактируем существующую заметку
                dialogSaveLocation(
                    resources.getString(R.string.sure_save_location),
                    this@NewLocationActivity,
                    false,
                    mainViewModel,
                    tempLocation!!
                )
            }
        } else if (item.itemId == R.id.id_share) {

            // Вызываем диалог выбора приложений для отправки текста поста в другое приложение
            val shareIntent = Intent.createChooser(
                ShareHelperLocation.shareLocation(
                    createNewLocation(),
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
                createNewLocation(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString(),
                this
            )

            var titleTemp = location?.titleLocation.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(this@NewLocationActivity)
                    val strMessage = savePdf(title, string, this@NewLocationActivity)
                    dialog.dismiss()
                    createDialogInfo(strMessage, this@NewLocationActivity)
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
                        val dialog = ProgressDialog.createProgressDialog(this@NewLocationActivity)
                        val strMessage = savePdf(title, string, this@NewLocationActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewLocationActivity)
                    }
                }
            }
        } else if (item.itemId == R.id.id_txt) {

            // хочу сделать сохранение в TXT
            val string = makeShareText(
                createNewLocation(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString(),
                this
            )
            var titleTemp = location?.titleLocation.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(this@NewLocationActivity)
                    val strMessage = saveTxt(title, string, this@NewLocationActivity)
                    dialog.dismiss()
                    createDialogInfo(strMessage, this@NewLocationActivity)
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
                        val dialog = ProgressDialog.createProgressDialog(this@NewLocationActivity)
                        val strMessage = saveTxt(title, string, this@NewLocationActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, this@NewLocationActivity)
                    }
                }
            }
        } else if (item.itemId == R.id.id_docx) {
            val string = makeShareText(
                createNewLocation(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString(),
                this
            )
            var titleTemp = location?.titleLocation.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp

            job = CoroutineScope(Dispatchers.Main).launch {
                val dialog = ProgressDialog.createProgressDialog(this@NewLocationActivity)
                val strMessage = saveDocx(title, string, this@NewLocationActivity)
                dialog.dismiss()
                createDialogInfo(strMessage, this@NewLocationActivity)
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
            newLocation = createNewLocation()
            if (newLocation != oldLocation) {
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
                    val dialog = ProgressDialog.createProgressDialog(this@NewLocationActivity)

                    val text = extractDocx(
                        pathBuff,
                        binding.edGeography,
                        DSQLITE_MAX_LENGTH,
                        this@NewLocationActivity
                    )

                    binding.edGeography.setText(text)
                    dialog.dismiss()
                    binding.edGeography.setSelection(binding.edGeography.length())
                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewLocationActivity)
            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewLocationActivity
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
                    val dialog = ProgressDialog.createProgressDialog(this@NewLocationActivity)

                    val text = extractTxt(
                        pathBuff,
                        binding.edGeography,
                        DSQLITE_MAX_LENGTH,
                        this@NewLocationActivity
                    )

                    binding.edGeography.setText(text)
                    dialog.dismiss()
                    binding.edGeography.setSelection(binding.edGeography.length())
                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewLocationActivity)
            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewLocationActivity
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
                    val dialog = ProgressDialog.createProgressDialogExtPdf(this@NewLocationActivity)

                    val text = extractPdf(
                        pathBuff,
                        binding.edGeography,
                        DSQLITE_MAX_LENGTH,
                        this@NewLocationActivity
                    )

                    binding.edGeography.setText(text)
                    dialog.dismiss()
                    binding.edGeography.setSelection(binding.edGeography.length())
                }
                createDialogInfo(resources.getString(R.string.text_min), this@NewLocationActivity)
            } else {
                createDialogInfo(
                    resources.getString(R.string.different_format),
                    this@NewLocationActivity
                )
            }
        }
    }



    // нажимаем на открыть локацию во фрагменте со списком локаций,
    // если оттуда ничего не передали, то значит мы в этот момент создаем новую локацию,
    // если передали, то значит открываем локацию на редактирование и передаем данные по данной локации
    // и заполняем все поля в activity_new_location.xml
    private fun getLocation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            book = intent.getSerializableExtra(
                LocationListFragment.TITLE_BOOK_KEY,
                BookEntity7::class.java
            )
            location = intent.getSerializableExtra(
                LocationListFragment.NEW_NOTE_KEY,
                LocationEntity2::class.java
            )

            fillLocation()
        } else {
            val sBook = intent.getSerializableExtra(LocationListFragment.TITLE_BOOK_KEY)
            val sLocation = intent.getSerializableExtra(LocationListFragment.NEW_NOTE_KEY)

            book = sBook as BookEntity7
            if (sLocation != null) {
                location = sLocation as LocationEntity2
                fillLocation()
            }
        }

    }

    // для передаваемого результат сделаем отдельную функцию
    private fun setMainResult() {
        var editState = "new"
        val tempLocation = if (location == null) {
            createNewLocation() // то добавляем новую заметку в базу данных
        } else {
            editState = "update"
            updateLocation() // иначе, редактируем существующую заметку
        }


        val i = Intent().apply {
            putExtra(LocationListFragment.NEW_NOTE_KEY, tempLocation)
            putExtra(LocationListFragment.EDIT_STATE_KEY, editState)
            putExtra(LocationListFragment.TITLE_BOOK_KEY, book)
        }

        DialogSaveAndGetOut(this@NewLocationActivity, i)
    }

    private fun updateLocation(): LocationEntity2? = with(binding) {
        return location?.copy(
            titleLocation = edTitleLocation.text.toString(),
            geography = edGeography.text.toString(),
            population = edPopulation.text.toString(),
            politics = edPolitics.text.toString(),
            economy = edEconomy.text.toString(),
            religion = edReligion.text.toString(),
            history = edHistory.text.toString(),
            feature = edFeature.text.toString(),
        )
    }

    // функция заполняющая наш LocationEntity
    private fun createNewLocation(): LocationEntity2 {
        return LocationEntity2(
            null,
            book?.id!!,
            binding.edTitleLocation.text.toString(),
            binding.edGeography.text.toString(),
            binding.edPopulation.text.toString(),
            binding.edPolitics.text.toString(),
            binding.edEconomy.text.toString(),
            binding.edReligion.text.toString(),
            binding.edHistory.text.toString(),
            binding.edFeature.text.toString(),
            0,
            "0",
            null,
        )
    }


    // функция для выбора размера текста
    private fun setTextSize() = with(binding) {
        edTitleLocation.setTextSize(pref?.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT))
        edGeography.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edPopulation.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edPolitics.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edEconomy.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edReligion.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edHistory.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edFeature.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))

        nameLocation.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
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
        edGeography.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )
        edPopulation.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )
        edPolitics.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )
        edEconomy.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )
        edReligion.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )
        edHistory.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )
        edFeature.setTypeface(
            pref?.getString(FONT_FAMILY_CONTENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )

        edTitleLocation.setTypeface(
            pref?.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )

        titleBook.setTypeface(
            pref?.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )

        nameLocation.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )
        textView4.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )
        textView5.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )
        textView6.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )
        textView7.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )
        textView8.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )
        textView9.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )
        textView10.setTypeface(
            pref?.getString(FONT_FAMILY_COMMENT_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )

        val font: Typeface? = typeface(
            pref?.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@NewLocationActivity
        )
        setTitleActionBar(resources.getString(R.string.app_name), font, supportActionBar)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

}