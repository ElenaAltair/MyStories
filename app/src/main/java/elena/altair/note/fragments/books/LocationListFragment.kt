package elena.altair.note.fragments.books

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import elena.altair.note.R
import elena.altair.note.activities.MainActivity
import elena.altair.note.activities.books.NewLocationActivity
import elena.altair.note.adapters.books.LocationAdapter
import elena.altair.note.databinding.FragmentLocationListBinding
import elena.altair.note.dialoghelper.DialogDelete.createDialogDelete
import elena.altair.note.dialoghelper.DialogInfo.createDialogInfo
import elena.altair.note.dialoghelper.ProgressDialog
import elena.altair.note.etities.BookEntity7
import elena.altair.note.etities.LocationEntity2
import elena.altair.note.utils.file.PdfTxtLocationListUtils.saveDocx
import elena.altair.note.utils.file.PdfTxtLocationListUtils.savePdf
import elena.altair.note.utils.file.PdfTxtLocationListUtils.saveTxt
import elena.altair.note.utils.font.setTextSize
import elena.altair.note.utils.font.setTypeface
import elena.altair.note.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationListFragment : BaseFragment(), LocationAdapter.Listener, BackPressed {

    private lateinit var defPref: SharedPreferences
    private var pref: SharedPreferences? = null
    private lateinit var binding: FragmentLocationListBinding
    private var book: BookEntity7? = null
    private val STORAGE_CODE: Int = 100
    private lateinit var editLauncher: ActivityResultLauncher<Intent>
    private var job: Job? = null

    // переменная, в которую будем записывать наш adapter
    private lateinit var adapter: LocationAdapter
    private val mainViewModel: MainViewModel by activityViewModels()
    //private val mainViewModel: MainViewModel by activityViewModels {
    //MainViewModel.MainViewModalFactory((context?.applicationContext as MainApp).database)
    //}

    var alertDialog: AlertDialog? = null

    override fun onClickNew() {
        // передаем book на активити, чтобы знать заголовок книги и id книги
        val intent = Intent(activity, NewLocationActivity::class.java).apply {
            putExtra(TITLE_BOOK_KEY, book)
        }
        editLauncher.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onEditResult()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationListBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val act = activity as MainActivity
        act.findViewById<View>(R.id.add).visibility = View.VISIBLE
        act.findViewById<View>(R.id.mainlist).visibility = View.VISIBLE
        act.findViewById<View>(R.id.tb).visibility = View.VISIBLE

        pref = PreferenceManager.getDefaultSharedPreferences(activity as AppCompatActivity)
        setTextSize()
        setFontFamily()
        super.onViewCreated(view, savedInstanceState)


        mainViewModel.bookTr.observe(viewLifecycleOwner) {

            binding.imListBook.setOnClickListener {
                FragmentManager.setFragment(
                    MainListFragment.newInstance(),
                    activity as AppCompatActivity
                )
            }

            binding.imDocx.setOnClickListener {
                val list = adapter.currentList

                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(activity as MainActivity)
                    val strMessage =
                        saveDocx(
                            book?.titleBook ?: "book",
                            book?.nameAuthor ?: "author",
                            list,
                            activity as MainActivity
                        )
                    dialog.dismiss()
                    createDialogInfo(strMessage, activity as MainActivity)
                }
            }

            binding.imPdf.setOnClickListener {
                val list = adapter.currentList

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                    job = CoroutineScope(Dispatchers.Main).launch {
                        val dialog = ProgressDialog.createProgressDialog(activity as MainActivity)
                        val strMessage =
                            savePdf(
                                book?.titleBook ?: "book",
                                book?.nameAuthor ?: "author",
                                list,
                                activity as MainActivity
                            )
                        dialog.dismiss()
                        createDialogInfo(strMessage, activity as MainActivity)
                    }
                } else {

                    if ((activity as MainActivity).checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED
                    ) {
                        //permission was not granted, request it
                        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        requestPermissions(permissions, STORAGE_CODE)
                    } else {
                        //permission already granted, call savePdf() method
                        job = CoroutineScope(Dispatchers.Main).launch {
                            val dialog =
                                ProgressDialog.createProgressDialog(activity as MainActivity)
                            val strMessage =
                                savePdf(
                                    book?.titleBook ?: "book",
                                    book?.nameAuthor ?: "author",
                                    list,
                                    activity as MainActivity
                                )
                            dialog.dismiss()
                            createDialogInfo(strMessage, activity as MainActivity)
                        }
                    }
                }
            }

            binding.imTxt.setOnClickListener {
                val list = adapter.currentList

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                    job = CoroutineScope(Dispatchers.Main).launch {
                        val dialog = ProgressDialog.createProgressDialog(activity as MainActivity)
                        val strMessage =
                            saveTxt(
                                book?.titleBook ?: "book",
                                book?.nameAuthor ?: "author",
                                list,
                                activity as MainActivity
                            )
                        dialog.dismiss()
                        createDialogInfo(strMessage, activity as MainActivity)
                    }
                } else {
                    if ((activity as MainActivity).checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED
                    ) {
                        //permission was not granted, request it
                        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        requestPermissions(permissions, STORAGE_CODE)
                    } else {
                        //permission already granted, call saveTxt() method
                        job = CoroutineScope(Dispatchers.Main).launch {
                            val dialog =
                                ProgressDialog.createProgressDialog(activity as MainActivity)
                            val strMessage =
                                saveTxt(
                                    book?.titleBook ?: "book",
                                    book?.nameAuthor ?: "author",
                                    list,
                                    activity as MainActivity
                                )
                            dialog.dismiss()
                            createDialogInfo(strMessage, activity as MainActivity)
                        }
                    }
                }
            }


            book = it
            binding.titBook.text = it.titleBook

            initRcView()
            observer()
        }
    }


    // функция для инициализации нашего Recycle View
    // здесь же мы инициализируем наш adapter
    private fun initRcView() = with(binding) {

        defPref = PreferenceManager.getDefaultSharedPreferences(activity as AppCompatActivity)
        rcViewLocation.layoutManager = getLayoutManager()

        adapter = LocationAdapter(
            this@LocationListFragment,
            defPref,
            activity as MainActivity
        ) // инициализируем наш adapter
        // передадим adapter, который будет обновлять наш Recycle View, в Recycle View
        rcViewLocation.adapter = adapter

    }

    // получаем нужный LayoutManager, который будем передавать в наш RecyclerView
    // в зависимости от того, что выбрано на экране настроек
    private fun getLayoutManager(): RecyclerView.LayoutManager {
        return if (defPref.getString("note_style_key", "Linear") == "Linear") {
            LinearLayoutManager(activity)
        } else {
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    // добавим observer, который будет следить за изменениями и обновлять наш adapter
    // observer следит за изменениями в базе данных и присылает нам обновленный список
    private fun observer() {
        //Log.d("MyLog", "id_book: " +book?.id)
        if (book?.id != null) {
            mainViewModel.allLocations(book?.id!!).observe(viewLifecycleOwner) {
                adapter.submitList(it) // it - это обновлённый список
            }
        }
    }

    // активируем editLauncher
    // в этой функции мы будем ждать результата,
    // который придет нам с активити, которое мы запустили
    private fun onEditResult() {
        editLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                // делаем проверку
                val editState = it.data?.getStringExtra(EDIT_STATE_KEY)
                if (editState == "update") {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        book =
                            it.data?.getSerializableExtra(TITLE_BOOK_KEY, BookEntity7::class.java)
                        mainViewModel.updateLocation(
                            it.data?.getSerializableExtra(
                                NEW_NOTE_KEY,
                                LocationEntity2::class.java
                            )!!
                        )
                    } else {
                        book = it.data?.getSerializableExtra(TITLE_BOOK_KEY) as BookEntity7
                        mainViewModel.updateLocation(
                            it.data?.getSerializableExtra(NEW_NOTE_KEY) as LocationEntity2
                        )
                    }
                } else {
                    //Log.d("MyLog", "title: ${it.data?.getSerializableExtra(NEW_NOTE_KEY) as NoteItem}")
                    // записываем в базу данных нашу заметку
                    // Метод T getSerializableExtra(String, Class<T>)введен в Android 33.
                    // поэтому вам следует использовать блок if для устройств, использующих Android ниже 33.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        book =
                            it.data?.getSerializableExtra(TITLE_BOOK_KEY, BookEntity7::class.java)
                        mainViewModel.insertLocation(
                            it.data?.getSerializableExtra(
                                NEW_NOTE_KEY,
                                LocationEntity2::class.java
                            )!!
                        )
                    } else {
                        book = it.data?.getSerializableExtra(TITLE_BOOK_KEY) as BookEntity7
                        mainViewModel.insertLocation(
                            it.data?.getSerializableExtra(NEW_NOTE_KEY) as LocationEntity2
                        )
                    }
                }
            }
        }
    }

    override fun editItem(location: LocationEntity2) {
        val intent = Intent(activity, NewLocationActivity::class.java).apply {
            putExtra(NEW_NOTE_KEY, location)
            putExtra(TITLE_BOOK_KEY, book)
        }
        editLauncher.launch(intent)
    }

    override fun deleteItem(id: Long) {
        createDialogDelete(
            resources.getString(R.string.sure_delete_location),
            activity as MainActivity,
            id,
            mainViewModel
        )
    }

    override fun onClickItem(location: LocationEntity2) {
        val intent = Intent(activity, NewLocationActivity::class.java).apply {
            putExtra(NEW_NOTE_KEY, location)
            putExtra(TITLE_BOOK_KEY, book)
        }
        editLauncher.launch(intent)
    }

    // функция для выбора размера текста
    private fun setTextSize() = with(binding) {
        titBook.setTextSize(pref?.getString("title_size_key", "18"))
        tvCT.setTextSize(pref?.getString("title_size_key", "18"))
    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {

        titBook.setTypeface(
            pref?.getString("font_family_title_key", "sans-serif"),
            activity as MainActivity
        )
        tvCT.setTypeface(
            pref?.getString("font_family_title_key", "sans-serif"),
            activity as MainActivity
        )
        textView2.setTypeface(
            pref?.getString("font_family_comment_key", "sans-serif"),
            activity as MainActivity
        )

    }

    override fun handleOnBackPressed() {
        FragmentManager.setFragment(
            MainListFragment.newInstance(),
            activity as AppCompatActivity
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {

        const val NEW_NOTE_KEY = "new_book_key"
        const val EDIT_STATE_KEY = "edit_state_key"
        const val TITLE_BOOK_KEY = "title_book_key"

        @JvmStatic
        fun newInstance() = LocationListFragment()
    }
}