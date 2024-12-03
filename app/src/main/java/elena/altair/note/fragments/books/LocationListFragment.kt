package elena.altair.note.fragments.books

import android.Manifest
import android.app.Activity
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
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import elena.altair.note.R
import elena.altair.note.activities.MainActivity
import elena.altair.note.activities.MainActivity.Companion.LOCATION_LIST_FRAGMENT
import elena.altair.note.activities.MainActivity.Companion.MAIN_LIST_FRAGMENT
import elena.altair.note.activities.MainActivity.Companion.currentFrag
import elena.altair.note.activities.books.NewLocationActivity
import elena.altair.note.adapters.books.LocationAdapter
import elena.altair.note.constants.MyConstants.FONT_FAMILY_COMMENT_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_DEFAULT
import elena.altair.note.constants.MyConstants.FONT_FAMILY_TITLE_KEY
import elena.altair.note.constants.MyConstants.NOTE_STYLE_DEFAULT
import elena.altair.note.constants.MyConstants.NOTE_STYLE_KEY
import elena.altair.note.constants.MyConstants.NOTE_STYLE_LINEAR
import elena.altair.note.constants.MyConstants.TITLE_SIZE_DEFAULT
import elena.altair.note.constants.MyConstants.TITLE_SIZE_KEY
import elena.altair.note.databinding.FragmentLocationListBinding
import elena.altair.note.etities.BookEntity7
import elena.altair.note.etities.LocationEntity2
import elena.altair.note.utils.font.setTextSize
import elena.altair.note.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationListFragment : BaseFragment(), LocationAdapter.Listener, BackPressed {

    private lateinit var defPref: SharedPreferences
    private var pref: SharedPreferences? = null
    private lateinit var binding: FragmentLocationListBinding
    private var book: BookEntity7? = null
    private val STORAGE_CODE: Int = 100
    private lateinit var editLauncher: ActivityResultLauncher<Intent>

    // переменная, в которую будем записывать наш adapter
    private lateinit var adapter: LocationAdapter
    private val mainViewModel: MainViewModel by activityViewModels()

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

        (activity as? DialogsAndOtherFunctions)?.viewButtons(LOCATION_LIST_FRAGMENT)

        pref = PreferenceManager.getDefaultSharedPreferences(activity as AppCompatActivity)
        setTextSize()
        setFontFamily()
        super.onViewCreated(view, savedInstanceState)


        mainViewModel.bookTr.observe(viewLifecycleOwner) {

            binding.imListBook.setOnClickListener {
                currentFrag = MAIN_LIST_FRAGMENT
                requireActivity().supportFragmentManager.commit {
                    replace(R.id.placeHolder, MainListFragment.newInstance())
                }
                //FragmentManager.setFragment(MainListFragment.newInstance(),activity as AppCompatActivity)
            }

            binding.imDocx.setOnClickListener {
                val list = adapter.currentList

                viewLifecycleOwner.lifecycleScope.launch {
                    val dialog = (activity as? DialogsAndOtherFunctions)?.progressDialog()
                    val strMessage =
                        (activity as? DialogsAndOtherFunctions)?.saveDocxLocation(
                            book?.titleBook ?: "book",
                            book?.nameAuthor ?: "author",
                            list
                        )
                    dialog?.dismiss()
                    (activity as? DialogsAndOtherFunctions)?.createDialogI(strMessage!!)
                }
            }

            binding.imPdf.setOnClickListener {
                val list = adapter.currentList

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                    viewLifecycleOwner.lifecycleScope.launch {
                        val dialog = (activity as? DialogsAndOtherFunctions)?.progressDialog()
                        val strMessage =
                            (activity as? DialogsAndOtherFunctions)?.savePdfLocation(
                                book?.titleBook ?: "book",
                                book?.nameAuthor ?: "author",
                                list
                            )
                        dialog?.dismiss()
                        (activity as? DialogsAndOtherFunctions)?.createDialogI(strMessage!!)
                    }
                } else {

                    if ((activity as AppCompatActivity).checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED
                    ) {
                        //permission was not granted, request it
                        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        requestPermissions(permissions, STORAGE_CODE)
                    } else {
                        //permission already granted, call savePdf() method
                        viewLifecycleOwner.lifecycleScope.launch {
                            val dialog = (activity as? DialogsAndOtherFunctions)?.progressDialog()
                            val strMessage =
                                (activity as? DialogsAndOtherFunctions)?.savePdfLocation(
                                    book?.titleBook ?: "book",
                                    book?.nameAuthor ?: "author",
                                    list
                                )
                            dialog?.dismiss()
                            (activity as? DialogsAndOtherFunctions)?.createDialogI(strMessage!!)
                        }
                    }
                }
            }

            binding.imTxt.setOnClickListener {
                val list = adapter.currentList

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                    viewLifecycleOwner.lifecycleScope.launch {
                        val dialog = (activity as? DialogsAndOtherFunctions)?.progressDialog()
                        val strMessage =
                            (activity as? DialogsAndOtherFunctions)?.saveTxtLocation(
                                book?.titleBook ?: "book",
                                book?.nameAuthor ?: "author",
                                list
                            )
                        dialog?.dismiss()
                        (activity as? DialogsAndOtherFunctions)?.createDialogI(strMessage!!)
                    }
                } else {
                    if ((activity as AppCompatActivity).checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED
                    ) {
                        //permission was not granted, request it
                        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        requestPermissions(permissions, STORAGE_CODE)
                    } else {
                        //permission already granted, call saveTxt() method
                        viewLifecycleOwner.lifecycleScope.launch {
                            val dialog = (activity as? DialogsAndOtherFunctions)?.progressDialog()
                            val strMessage =
                                (activity as? DialogsAndOtherFunctions)?.saveTxtLocation(
                                    book?.titleBook ?: "book",
                                    book?.nameAuthor ?: "author",
                                    list
                                )
                            dialog?.dismiss()
                            (activity as? DialogsAndOtherFunctions)?.createDialogI(strMessage!!)
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
        return if (defPref.getString(NOTE_STYLE_KEY, NOTE_STYLE_DEFAULT) == NOTE_STYLE_LINEAR) {
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
        (activity as? DialogsAndOtherFunctions)?.createDialogDelete(
            resources.getString(R.string.sure_delete_location),
            id,
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
        titBook.setTextSize(pref?.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT))
        tvCT.setTextSize(pref?.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT))
    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_TITLE_KEY,
                FONT_FAMILY_DEFAULT
            ), titBook
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_TITLE_KEY,
                FONT_FAMILY_DEFAULT
            ), tvCT
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_COMMENT_KEY,
                FONT_FAMILY_DEFAULT
            ), textView2
        )

    }

    override fun handleOnBackPressed() {
        currentFrag = MAIN_LIST_FRAGMENT
        requireActivity().supportFragmentManager.commit {
            replace(R.id.placeHolder, MainListFragment.newInstance())
        }
        //FragmentManager.setFragment(MainListFragment.newInstance(),activity as AppCompatActivity)
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