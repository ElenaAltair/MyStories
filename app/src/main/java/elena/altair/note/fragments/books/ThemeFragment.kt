package elena.altair.note.fragments.books

//import elena.altair.note.activities.MainActivity
import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import elena.altair.note.R
import elena.altair.note.activities.MainActivity.Companion.MAIN_LIST_FRAGMENT
import elena.altair.note.activities.MainActivity.Companion.THEME_FRAGMENT
import elena.altair.note.activities.MainActivity.Companion.currentFrag
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
import elena.altair.note.databinding.FragmentThemeBinding
import elena.altair.note.etities.BookEntity7
import elena.altair.note.etities.ThemeEntity2
import elena.altair.note.utils.font.setTextSize
import elena.altair.note.utils.share.ShareHelperTheme
import elena.altair.note.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ThemeFragment : BaseFragment(), BackPressed {

    private lateinit var binding: FragmentThemeBinding
    private var pref: SharedPreferences? = null
    private var book: BookEntity7? = null
    private var theme: ThemeEntity2? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val STORAGE_CODE: Int = 100
    private var saveTheme = SAVE_THEME_AND_STAY_ON_FRAGMENT
    private var oldTheme: ThemeEntity2? = null
    private var newTheme: ThemeEntity2? = null
    private var indicatorOldTheme = 0


    override fun onClickNew() {
        saveTheme = SAVE_THEME_AND_STAY_ON_FRAGMENT
        newTheme = createNewTheme()
        if (newTheme != oldTheme)
            editTheme()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentThemeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? DialogsAndOtherFunctions)?.viewButtons(THEME_FRAGMENT)

        pref = PreferenceManager.getDefaultSharedPreferences(activity as AppCompatActivity)
        setTextSize()
        setFontFamily()

        binding.imSave.setOnClickListener {
            saveTheme = SAVE_THEME_AND_STAY_ON_FRAGMENT
            editTheme()
        }

        binding.imPdf.setOnClickListener {
            // хочу сделать сохранение в PDF
            val string = (activity as? DialogsAndOtherFunctions)?.makeShareTextTheme(
                createNewTheme(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString()
            )
            var titleTemp = book?.titleBook.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp + "_theme"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                viewLifecycleOwner.lifecycleScope.launch {
                    val dialog = (activity as? DialogsAndOtherFunctions)?.progressDialog()
                    val strMessage = string?.let { it1 ->
                        (activity as? DialogsAndOtherFunctions)?.savePdf(
                            title,
                            it1
                        )
                    }
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
                        val strMessage = string?.let { it1 ->
                            (activity as? DialogsAndOtherFunctions)?.savePdf(
                                title,
                                it1
                            )
                        }
                        dialog?.dismiss()
                        (activity as? DialogsAndOtherFunctions)?.createDialogI(strMessage!!)
                    }
                }
            }
        }

        binding.imTxt.setOnClickListener {
            // хочу сделать сохранение в TXT
            val string = (activity as? DialogsAndOtherFunctions)?.makeShareTextTheme(
                createNewTheme(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString()
            )
            var titleTemp = book?.titleBook.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp + "_theme"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                viewLifecycleOwner.lifecycleScope.launch {
                    val dialog = (activity as? DialogsAndOtherFunctions)?.progressDialog()
                    val strMessage = string?.let { it1 ->
                        (activity as? DialogsAndOtherFunctions)?.saveTxt(
                            title,
                            it1
                        )
                    }
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
                        val strMessage = string?.let { it1 ->
                            (activity as? DialogsAndOtherFunctions)?.saveTxt(
                                title,
                                it1
                            )
                        }
                        dialog?.dismiss()
                        (activity as? DialogsAndOtherFunctions)?.createDialogI(strMessage!!)
                    }
                }
            }
        }



        binding.imShare.setOnClickListener {
            // Вызываем диалог выбора приложений для отправки текста поста в другое приложение
            val shareIntent = Intent.createChooser(
                ShareHelperTheme.shareTheme(
                    createNewTheme(),
                    book?.titleBook.toString(),
                    book?.nameAuthor.toString(),
                    activity as AppCompatActivity
                ),
                "Share by"
            )
            startActivity(shareIntent)
        }

        binding.imListBook.setOnClickListener {
            currentFrag = MAIN_LIST_FRAGMENT
            requireActivity().supportFragmentManager.commit {
                replace(R.id.placeHolder, MainListFragment.newInstance())
            }
        }

        binding.imDocx.setOnClickListener {
            val string = (activity as? DialogsAndOtherFunctions)?.makeShareTextTheme(
                createNewTheme(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString()
            )
            var titleTemp = book?.titleBook.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp + "_theme"
            viewLifecycleOwner.lifecycleScope.launch {
                val dialog = (activity as? DialogsAndOtherFunctions)?.progressDialog()
                val strMessage = string?.let { it1 ->
                    (activity as? DialogsAndOtherFunctions)?.saveDocx(
                        title,
                        it1
                    )
                }
                dialog?.dismiss()
                (activity as? DialogsAndOtherFunctions)?.createDialogI(strMessage!!)
            }
        }


        // получаем объект BookEntity из предыдущего фрагмента
        mainViewModel.bookTr.observe(viewLifecycleOwner) {
            book = it
            binding.tBook.text = it.titleBook

            observer(book?.id)
        }

    }


    // observer следит за изменениями в базе данных и присылает нам обновленную тему
    private fun observer(id: Long?) {
        //Log.d("MyLog", "id_book: " +book?.id)
        if (id != null) {
            mainViewModel.getTheme(id).observe(viewLifecycleOwner) {
                theme = it
                with(binding) {
                    edText1.setText(theme?.desc1)
                    edText2.setText(theme?.desc2)
                    edText3.setText(theme?.desc3)
                    edText4.setText(theme?.desc4)
                    edText5.setText(theme?.desc5)
                    edText6.setText(theme?.desc6)
                    edText7.setText(theme?.desc7)
                    edText8.setText(theme?.desc8)
                }

                val oldThemeTemp = theme
                if (oldThemeTemp != null) oldTheme = oldThemeTemp
            }
        }
        if (indicatorOldTheme == 0) {
            oldTheme = createNewTheme()
            indicatorOldTheme = 1
        }
    }

    private fun editTheme() {
        // записываем в базу данных нашу тему
        var mess = activity?.resources!!.getString(R.string.sure_save_theme)
        if (saveTheme == SAVE_THEME_AND_LEAVE_FRAGMENT)
            mess = activity?.resources!!.getString(R.string.save_changes_theme_2)
        (activity as? DialogsAndOtherFunctions)?.dialogSaveTheme(mess, true, createNewTheme())
    }

    // функция заполняющая наш ThemeEntity
    private fun createNewTheme(): ThemeEntity2 {
        return ThemeEntity2(
            theme?.id,
            book?.id!!,
            binding.edText1.text.toString(),
            binding.edText2.text.toString(),
            binding.edText3.text.toString(),
            binding.edText4.text.toString(),
            binding.edText5.text.toString(),
            binding.edText6.text.toString(),
            binding.edText7.text.toString(),
            binding.edText8.text.toString(),
            "0",
            null,
        )
    }

    // функция для выбора размера текста
    private fun setTextSize() = with(binding) {
        edText1.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText2.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText3.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText4.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText5.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText6.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText7.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))
        edText8.setTextSize(pref?.getString(CONTENT_SIZE_KEY, CONTENT_SIZE_DEFAULT))

        tBook.setTextSize(pref?.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT))
        tvCT.setTextSize(pref?.getString(TITLE_SIZE_KEY, TITLE_SIZE_DEFAULT))
        textView4.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView5.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView6.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView7.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView8.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView9.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView10.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
        textView11.setTextSize(pref?.getString(COMMENT_SIZE_KEY, COMMENT_SIZE_DEFAULT))
    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_CONTENT_KEY,
                FONT_FAMILY_DEFAULT
            ), edText1
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_CONTENT_KEY,
                FONT_FAMILY_DEFAULT
            ), edText2
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_CONTENT_KEY,
                FONT_FAMILY_DEFAULT
            ), edText3
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_CONTENT_KEY,
                FONT_FAMILY_DEFAULT
            ), edText4
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_CONTENT_KEY,
                FONT_FAMILY_DEFAULT
            ), edText5
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_CONTENT_KEY,
                FONT_FAMILY_DEFAULT
            ), edText6
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_CONTENT_KEY,
                FONT_FAMILY_DEFAULT
            ), edText7
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_CONTENT_KEY,
                FONT_FAMILY_DEFAULT
            ), edText8
        )


        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_TITLE_KEY,
                FONT_FAMILY_DEFAULT
            ), tBook
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
            ), textView4
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_COMMENT_KEY,
                FONT_FAMILY_DEFAULT
            ), textView5
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_COMMENT_KEY,
                FONT_FAMILY_DEFAULT
            ), textView6
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_COMMENT_KEY,
                FONT_FAMILY_DEFAULT
            ), textView7
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_COMMENT_KEY,
                FONT_FAMILY_DEFAULT
            ), textView8
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_COMMENT_KEY,
                FONT_FAMILY_DEFAULT
            ), textView9
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_COMMENT_KEY,
                FONT_FAMILY_DEFAULT
            ), textView10
        )

        (activity as? DialogsAndOtherFunctions)?.textViewSetTypeface(
            pref?.getString(
                FONT_FAMILY_COMMENT_KEY,
                FONT_FAMILY_DEFAULT
            ), textView11
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
        saveTheme = SAVE_THEME_AND_LEAVE_FRAGMENT
        newTheme = createNewTheme()
        if (newTheme != oldTheme)
            editTheme()

        super.onDestroy()
    }


    companion object {

        const val SAVE_THEME_AND_LEAVE_FRAGMENT = 2
        const val SAVE_THEME_AND_STAY_ON_FRAGMENT = 1

        @JvmStatic
        fun newInstance() = ThemeFragment()
    }
}