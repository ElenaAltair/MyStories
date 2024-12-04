package elena.altair.note.fragments.books


import android.Manifest
import android.annotation.SuppressLint
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
import elena.altair.note.activities.MainActivity.Companion.CHAPTER_LIST_FRAGMENT
import elena.altair.note.activities.MainActivity.Companion.MAIN_LIST_FRAGMENT
import elena.altair.note.activities.MainActivity.Companion.currentFrag
import elena.altair.note.activities.books.NewChapterActivity
import elena.altair.note.adapters.books.ChapterAdapter
import elena.altair.note.constants.MyConstants.FONT_FAMILY_COMMENT_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_DEFAULT
import elena.altair.note.constants.MyConstants.FONT_FAMILY_TITLE_KEY
import elena.altair.note.constants.MyConstants.NOTE_STYLE_DEFAULT
import elena.altair.note.constants.MyConstants.NOTE_STYLE_KEY
import elena.altair.note.constants.MyConstants.NOTE_STYLE_LINEAR
import elena.altair.note.constants.MyConstants.TITLE_SIZE_DEFAULT
import elena.altair.note.constants.MyConstants.TITLE_SIZE_KEY
import elena.altair.note.databinding.FragmentChapterListBinding
import elena.altair.note.etities.BookEntity7
import elena.altair.note.etities.ChapterEntity2
import elena.altair.note.utils.font.setTextSize
import elena.altair.note.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ChapterListFragment : BaseFragment(), ChapterAdapter.Listener, BackPressed {

    private lateinit var defPref: SharedPreferences
    private var pref: SharedPreferences? = null
    private lateinit var binding: FragmentChapterListBinding
    private var book: BookEntity7? = null

    private lateinit var editLauncher: ActivityResultLauncher<Intent>

    // переменная, в которую будем записывать наш adapter
    private lateinit var adapter: ChapterAdapter
    private val mainViewModel: MainViewModel by activityViewModels()
    private val STORAGE_CODE: Int = 100

    // при нажатии на кнопку "добавить",
    // здесь будет запускаться логика, добавляющая новую запись(главу книги) в базу данных
    // нажимаем кнопку на нижнем меню "Add"
    override fun onClickNew() {
        // передаем book на активити, чтобы знать заголовок книги и id книги
        val intent = Intent(activity, NewChapterActivity::class.java).apply {
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
        binding = FragmentChapterListBinding.inflate(inflater, container, false)
        return binding.root


    }

    @SuppressLint("CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        (activity as? DialogsAndOtherFunctions)?.viewButtons(CHAPTER_LIST_FRAGMENT)

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

            }


            binding.imDocx.setOnClickListener {
                val list = adapter.currentList

                viewLifecycleOwner.lifecycleScope.launch {
                    val dialog = (activity as? DialogsAndOtherFunctions)?.progressDialog()
                    val strMessage = (activity as? DialogsAndOtherFunctions)?.saveDocxChapters(
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
                        val strMessage = (activity as? DialogsAndOtherFunctions)?.savePdfChapters(
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
                                (activity as? DialogsAndOtherFunctions)?.savePdfChapters(
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
                        val strMessage = (activity as? DialogsAndOtherFunctions)?.saveTxtChapters(
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
                                (activity as? DialogsAndOtherFunctions)?.saveTxtChapters(
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
        rcViewChapters.layoutManager = getLayoutManager()

        adapter = ChapterAdapter(
            this@ChapterListFragment,
            defPref,
            activity as MainActivity
        ) // инициализируем наш adapter
        // передадим adapter, который будет обновлять наш Recycle View, в Recycle View
        rcViewChapters.adapter = adapter

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
            val c = book?.uidAd

            mainViewModel.allChaptersById(book?.id!!).observe(viewLifecycleOwner) {
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
                        mainViewModel.updateChapter(
                            it.data?.getSerializableExtra(
                                NEW_NOTE_KEY,
                                ChapterEntity2::class.java
                            )!!
                        )
                    } else {
                        book = it.data?.getSerializableExtra(TITLE_BOOK_KEY) as BookEntity7
                        mainViewModel.updateChapter(it.data?.getSerializableExtra(NEW_NOTE_KEY) as ChapterEntity2)
                    }
                } else {
                    //Log.d("MyLog", "title: ${it.data?.getSerializableExtra(NEW_NOTE_KEY) as NoteItem}")
                    // записываем в базу данных нашу заметку
                    // Метод T getSerializableExtra(String, Class<T>)введен в Android 33.
                    // поэтому вам следует использовать блок if для устройств, использующих Android ниже 33.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        book =
                            it.data?.getSerializableExtra(TITLE_BOOK_KEY, BookEntity7::class.java)
                        mainViewModel.insertChapter(
                            it.data?.getSerializableExtra(
                                NEW_NOTE_KEY,
                                ChapterEntity2::class.java
                            )!!
                        )
                    } else {
                        book = it.data?.getSerializableExtra(TITLE_BOOK_KEY) as BookEntity7
                        mainViewModel.insertChapter(it.data?.getSerializableExtra(NEW_NOTE_KEY) as ChapterEntity2)
                    }
                }
            }
        }
    }


    override fun editItem(chapter: ChapterEntity2) {
        viewLifecycleOwner.lifecycleScope.launch {
            openChapter(chapter)
        }
    }

    override fun deleteItem(id: Long) {
        (activity as? DialogsAndOtherFunctions)?.createDialogDelete(
            resources.getString(R.string.sure_delete_chapter),
            id
        )
    }

    override fun onClickItem(chapter: ChapterEntity2) {
        viewLifecycleOwner.lifecycleScope.launch {
            openChapter(chapter)
        }
    }

    suspend fun openChapter(chapter: ChapterEntity2) = withContext(Dispatchers.IO) {
        val intent = Intent(activity, NewChapterActivity::class.java).apply {
            putExtra(NEW_NOTE_KEY, chapter)
            putExtra(TITLE_BOOK_KEY, book)
        }
        editLauncher.launch(intent)
    }

    override fun updatePublic(chapter: ChapterEntity2) {
        mainViewModel.updateChapter(chapter)
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
        //FragmentManager.setFragment(MainListFragment.newInstance(),activity as MainActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {

        const val NEW_NOTE_KEY = "new_book_key"
        const val EDIT_STATE_KEY = "edit_state_key"
        const val TITLE_BOOK_KEY = "title_book_key"

        @JvmStatic
        fun newInstance() = ChapterListFragment()
    }

}