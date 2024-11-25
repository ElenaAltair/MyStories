package elena.altair.note.fragments.books

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import elena.altair.note.R
import elena.altair.note.activities.MainActivity
import elena.altair.note.activities.MainActivity.Companion.currentUser
import elena.altair.note.activities.books.NewBookActivity
import elena.altair.note.adapters.books.BookAdapter
import elena.altair.note.databinding.FragmentMainListBinding
import elena.altair.note.dialoghelper.DialogDelete.createDialogDelete
import elena.altair.note.etities.BookEntity7
import elena.altair.note.utils.font.setTextSize
import elena.altair.note.utils.font.setTypeface
import elena.altair.note.viewmodel.MainViewModel

@AndroidEntryPoint
class MainListFragment() : BaseFragment(), BookAdapter.Listener {//, BackPressed


    private lateinit var binding: FragmentMainListBinding
    private lateinit var defPref: SharedPreferences
    private var pref: SharedPreferences? = null

    private lateinit var editLauncher: ActivityResultLauncher<Intent>

    // переменная, в которую будем записывать наш adapter
    private lateinit var adapter: BookAdapter
    private val mainViewModel: MainViewModel by activityViewModels()


    //private val mainViewModel: MainViewModel by activityViewModels {
    //MainViewModel.MainViewModalFactory((context?.applicationContext as MainApp).database)
    //}

    // при нажатии на кнопку "добавить",
    // здесь будет запускаться логика, добавляющая новую запись(книги) в базу данных, для каждого фрагмента своя логика
    // нажимаем кнопку на нижнем меню "Add"
    override fun onClickNew() {

        val intent = Intent(activity, NewBookActivity::class.java).apply {
            putExtra(CURRENT_USER, currentUser)
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
        binding = FragmentMainListBinding.inflate(inflater, container, false)
        return binding.root
    }

    // функция запускается, когда все View уже созданы
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val act = activity as MainActivity

        act.findViewById<View>(R.id.settings).visibility = View.VISIBLE
        act.findViewById<View>(R.id.add).visibility = View.VISIBLE
        act.findViewById<View>(R.id.mainlist).visibility = View.VISIBLE
        act.findViewById<View>(R.id.tb).visibility = View.GONE
        val toolbar =
            act.findViewById<Toolbar>(R.id.toolbar) // import androidx.appcompat.widget.Toolbar
        val tv: TextView = toolbar.getChildAt(0) as TextView
        tv.text = act.resources.getString(R.string.app_name)


        pref = PreferenceManager.getDefaultSharedPreferences(activity as AppCompatActivity)
        setTextSize()
        setFontFamily()
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        scrollListener()
        observer()
    }

    // функция для инициализации нашего Recycle View
    // здесь же мы инициализируем наш adapter
    private fun initRcView() = with(binding) {
        defPref = PreferenceManager.getDefaultSharedPreferences(activity as MainActivity)
        rcViewNote.layoutManager = getLayoutManager()

        adapter = BookAdapter(
            this@MainListFragment,
            defPref,
            activity as MainActivity
        ) // инициализируем наш adapter
        // передадим adapter, который будет обновлять наш Recycle View, в Recycle View
        rcViewNote.adapter = adapter

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
        mainViewModel.allBooks(currentUser).observe(viewLifecycleOwner) {
            adapter.submitList(it) // it - это обновлённый список
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
                        mainViewModel.updateBook(
                            it.data?.getSerializableExtra(
                                NEW_NOTE_KEY,
                                BookEntity7::class.java
                            )!!
                        )
                    } else {
                        mainViewModel.updateBook(it.data?.getSerializableExtra(NEW_NOTE_KEY) as BookEntity7)
                    }
                } else {
                    //Log.d("MyLog", "title: ${it.data?.getSerializableExtra(NEW_NOTE_KEY) as NoteItem}")
                    // записываем в базу данных нашу заметку
                    // Метод T getSerializableExtra(String, Class<T>)введен в Android 33.
                    // поэтому вам следует использовать блок if для устройств, использующих Android ниже 33.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mainViewModel.insertBook(
                            it.data?.getSerializableExtra(
                                NEW_NOTE_KEY,
                                BookEntity7::class.java
                            )!!
                        )
                    } else {
                        mainViewModel.insertBook(it.data?.getSerializableExtra(NEW_NOTE_KEY) as BookEntity7)
                    }
                }


            }
        }
    }

    override fun editItem(book: BookEntity7) { // нажали на маленькую кнопку редакировать заголовок книги


        val intent = Intent(activity, NewBookActivity::class.java).apply {
            putExtra(NEW_NOTE_KEY, book)
            putExtra(CURRENT_USER, currentUser)
        }
        editLauncher.launch(intent)
    }

    override fun deleteItem(id: Long) { // нажали на маленькую кнопку удалить книгу
        createDialogDelete(
            resources.getString(R.string.sure_delete_book),
            activity as MainActivity,
            id,
            mainViewModel
        )
    }

    // нажали на элемент списка (на какую-нибудь книгу)
    // открывается фрагмент с меню для новой книги
    override fun onClickItem(book: BookEntity7) {
        mainViewModel.bookTr.value = book
        FragmentManager.setFragment(
            ChapterListFragment.newInstance(),
            activity as AppCompatActivity
        )
    }

    // нажали на маленьку кнопку edit_in
    // открывается фрагмент с меню для новой книги
    override fun editInItem(book: BookEntity7) {
        mainViewModel.bookTr.value = book
        FragmentManager.setFragment(
            ChapterListFragment.newInstance(),
            activity as AppCompatActivity
        )
    }


    // тест
    private fun scrollListener() {
        binding.rcViewNote.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recView, newState)
                // не может скролиться вниз и новое состояние это состояние покоя
                if (!recView.canScrollVertically(SCROLL_DOWN) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d("MyLog", "Can't scroll down")
                }
            }
        })
    }

    // функция для выбора размера текста
    private fun setTextSize() = with(binding) {
        textView.setTextSize(pref?.getString("title_size_key", "18"))
        //textView2.setTextSize(pref?.getString("comment_size_key", "16"))
    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {

        textView.setTypeface(
            pref?.getString("font_family_title_key", "sans-serif"),
            activity as MainActivity
        )
        textView2.setTypeface(
            pref?.getString("font_family_title_key", "sans-serif"),
            activity as MainActivity
        )
        textView2.setTypeface(
            pref?.getString("font_family_comment_key", "sans-serif"),
            activity as MainActivity
        )

    }

    companion object {

        const val NEW_NOTE_KEY = "new_book_key"
        const val EDIT_STATE_KEY = "edit_state_key"
        const val SCROLL_DOWN = 1
        const val CURRENT_USER = ""


        @JvmStatic
        fun newInstance() = MainListFragment()
    }


}