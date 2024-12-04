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
import elena.altair.note.activities.MainActivity.Companion.PLOT_FRAGMENT
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
import elena.altair.note.databinding.FragmentPlotBinding
import elena.altair.note.etities.BookEntity7
import elena.altair.note.etities.PlotEntity2
import elena.altair.note.utils.font.setTextSize
import elena.altair.note.utils.share.ShareHelperPlot
import elena.altair.note.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlotFragment : BaseFragment(), BackPressed {

    private lateinit var binding: FragmentPlotBinding
    private var pref: SharedPreferences? = null
    private var book: BookEntity7? = null
    private var plot: PlotEntity2? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val STORAGE_CODE: Int = 100
    private var saveMess = SAVE_PLOT_AND_STAY_ON_FRAGMENT
    private var oldPlot: PlotEntity2? = null
    private var newPlot: PlotEntity2? = null
    private var indicatorOldPlot = 0


    override fun onClickNew() {
        saveMess = SAVE_PLOT_AND_STAY_ON_FRAGMENT
        newPlot = createNewPlot()
        if (newPlot != oldPlot)
            editPlot()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlotBinding.inflate(inflater, container, false)

        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? DialogsAndOtherFunctions)?.viewButtons(PLOT_FRAGMENT)

        pref = PreferenceManager.getDefaultSharedPreferences(activity as AppCompatActivity)
        setTextSize()
        setFontFamily()


        binding.imSave.setOnClickListener {
            saveMess = SAVE_PLOT_AND_STAY_ON_FRAGMENT
            editPlot()
        }

        binding.imPdf.setOnClickListener {
            // хочу сделать сохранение в PDF
            val string = (activity as? DialogsAndOtherFunctions)?.makeShareTextPlot(
                createNewPlot(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString()
            )

            var titleTemp = book?.titleBook.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp + "_plot"

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
            val string = (activity as? DialogsAndOtherFunctions)?.makeShareTextPlot(
                createNewPlot(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString()
            )
            var titleTemp = book?.titleBook.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp + "_plot"

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

        binding.imDocx.setOnClickListener {
            val string = (activity as? DialogsAndOtherFunctions)?.makeShareTextPlot(
                createNewPlot(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString()
            )
            var titleTemp = book?.titleBook.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp + "_plot"

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

        binding.imShare.setOnClickListener {
            // Вызываем диалог выбора приложений для отправки текста поста в другое приложение
            val shareIntent = Intent.createChooser(
                ShareHelperPlot.sharePlot(
                    createNewPlot(),
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
            //FragmentManager.setFragment(MainListFragment.newInstance(),activity as AppCompatActivity)
        }

        // получаем объект BookEntity из предыдущего фрагмента
        mainViewModel.bookTr.observe(viewLifecycleOwner) {
            book = it
            binding.tBook.text = it.titleBook

            observer(book?.id)
        }

    }


    private fun editPlot() {
        // записываем в базу данных наш cюжет
        var mess = activity?.resources!!.getString(R.string.sure_save_plot)
        if (saveMess == SAVE_PLOT_AND_LEAVE_FRAGMENT)
            mess = activity?.resources!!.getString(R.string.save_changes_plot_2)
        (activity as? DialogsAndOtherFunctions)?.dialogSavePlot(mess, true, createNewPlot())

    }


    // observer следит за изменениями в базе данных и присылает нам обновленный plot
    private fun observer(id: Long?) {
        //Log.d("MyLog", "id_book: " +book?.id)
        if (id != null) {
            mainViewModel.getPlot(id).observe(viewLifecycleOwner) {
                plot = it

                with(binding) {
                    edText1.setText(plot?.desc1)
                    edText2.setText(plot?.desc2)
                    edText3.setText(plot?.desc3)
                    edText4.setText(plot?.desc4)
                    edText5.setText(plot?.desc5)
                    edText6.setText(plot?.desc6)
                    edText7.setText(plot?.desc7)
                    edText8.setText(plot?.desc8)
                }

                val oldPlotTemp = plot
                if (oldPlotTemp != null) oldPlot = oldPlotTemp
            }
        }
        if (indicatorOldPlot == 0) {
            oldPlot = createNewPlot()
            indicatorOldPlot = 1
        }
    }


    // функция заполняющая наш PlotEntity
    private fun createNewPlot(): PlotEntity2 {
        return PlotEntity2(
            plot?.id,
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
        saveMess = SAVE_PLOT_AND_LEAVE_FRAGMENT
        newPlot = createNewPlot()
        if (newPlot != oldPlot)
            editPlot()

        super.onDestroy()
    }

    companion object {

        const val SAVE_PLOT_AND_LEAVE_FRAGMENT = 2
        const val SAVE_PLOT_AND_STAY_ON_FRAGMENT = 1

        @JvmStatic
        fun newInstance() = PlotFragment()

    }


}