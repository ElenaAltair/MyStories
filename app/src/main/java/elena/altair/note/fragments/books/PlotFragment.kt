package elena.altair.note.fragments.books

import android.Manifest
import android.app.AlertDialog
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
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import elena.altair.note.R
import elena.altair.note.activities.MainActivity
import elena.altair.note.databinding.FragmentPlotBinding
import elena.altair.note.dialoghelper.DialogInfo.createDialogInfo
import elena.altair.note.dialoghelper.DialogSave.dialogSavePlot
import elena.altair.note.dialoghelper.ProgressDialog
import elena.altair.note.etities.BookEntity7
import elena.altair.note.etities.PlotEntity2
import elena.altair.note.utils.file.DOCXUtils.saveDocx
import elena.altair.note.utils.file.PDFUtils.savePdf
import elena.altair.note.utils.file.TXTUtils.saveTxt
import elena.altair.note.utils.font.setTextSize
import elena.altair.note.utils.font.setTypeface
import elena.altair.note.utils.share.ShareHelperPlot
import elena.altair.note.utils.share.ShareHelperPlot.makeShareText
import elena.altair.note.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlotFragment : BaseFragment(), BackPressed {

    private lateinit var binding: FragmentPlotBinding
    private var pref: SharedPreferences? = null
    private var book: BookEntity7? = null
    private var plot: PlotEntity2? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val STORAGE_CODE: Int = 100
    private var job: Job? = null
    private var saveMess = 1
    private var oldPlot: PlotEntity2? = null
    private var newPlot: PlotEntity2? = null
    private var indicatorOldPlot = 0

    //private val mainViewModel: MainViewModel by activityViewModels {
    //MainViewModel.MainViewModalFactory((context?.applicationContext as MainApp).database)
    //}

    var alertDialog: AlertDialog? = null

    override fun onClickNew() {
        saveMess = 1
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

        val act = activity as MainActivity
        act.findViewById<View>(R.id.add).visibility = View.GONE
        act.findViewById<View>(R.id.mainlist).visibility = View.GONE
        act.findViewById<View>(R.id.tb).visibility = View.VISIBLE

        pref = PreferenceManager.getDefaultSharedPreferences(activity as AppCompatActivity)
        setTextSize()
        setFontFamily()


        binding.imSave.setOnClickListener {
            saveMess = 1
            editPlot()
        }

        binding.imPdf.setOnClickListener {
            // хочу сделать сохранение в PDF
            val string = makeShareText(
                createNewPlot(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString(),
                activity as MainActivity
            )

            var titleTemp = book?.titleBook.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp + "_plot"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(activity as MainActivity)
                    val strMessage = savePdf(title, string, activity as MainActivity)
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
                        val dialog = ProgressDialog.createProgressDialog(activity as MainActivity)
                        val strMessage = savePdf(title, string, activity as MainActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, activity as MainActivity)
                    }
                }
            }
        }

        binding.imTxt.setOnClickListener {
            // хочу сделать сохранение в TXT
            val string = makeShareText(
                createNewPlot(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString(),
                activity as MainActivity
            )
            var titleTemp = book?.titleBook.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp + "_plot"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (версия Q) // Android 11 (версия R)
                job = CoroutineScope(Dispatchers.Main).launch {
                    val dialog = ProgressDialog.createProgressDialog(activity as MainActivity)
                    val strMessage = saveTxt(title, string, activity as MainActivity)
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
                        val dialog = ProgressDialog.createProgressDialog(activity as MainActivity)
                        val strMessage = saveTxt(title, string, activity as MainActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, activity as MainActivity)
                    }
                }
            }
        }

        binding.imDocx.setOnClickListener {
            val string = makeShareText(
                createNewPlot(),
                book?.titleBook.toString(),
                book?.nameAuthor.toString(),
                activity as MainActivity
            )
            var titleTemp = book?.titleBook.toString()
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }
            val title = titleTemp + "_plot"

            job = CoroutineScope(Dispatchers.Main).launch {
                val dialog = ProgressDialog.createProgressDialog(activity as MainActivity)
                val strMessage = saveDocx(title, string, activity as MainActivity)
                dialog.dismiss()
                createDialogInfo(strMessage, activity as MainActivity)
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
            FragmentManager.setFragment(
                MainListFragment.newInstance(),
                activity as AppCompatActivity
            )
        }

        // получаем объект BookEntity из предыдущего фрагмента
        mainViewModel.bookTr.observe(viewLifecycleOwner) {
            book = it
            binding.tBook.text = it.titleBook

            observer(book?.id)
        }

    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            STORAGE_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    val string = makeShareText(
                        createNewPlot(),
                        book?.titleBook.toString(),
                        book?.nameAuthor.toString(),
                        activity as MainActivity
                    )
                    var titleTemp = book?.titleBook.toString()
                    if (titleTemp.length > 10) {
                        titleTemp = titleTemp.substring(0, 10)
                    }
                    val title = titleTemp + "_plot"
                    //permission from popup was granted, call savePdf() method
                    job = CoroutineScope(Dispatchers.Main).launch {
                        val dialog = ProgressDialog.createProgressDialog(activity as MainActivity)
                        val strMessage = savePdf(title, string, activity as MainActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, activity as MainActivity)
                    }
                    job = CoroutineScope(Dispatchers.Main).launch {
                        val dialog = ProgressDialog.createProgressDialog(activity as MainActivity)
                        val strMessage = saveTxt(title, string, activity as MainActivity)
                        dialog.dismiss()
                        createDialogInfo(strMessage, activity as MainActivity)
                    }
                } else {
                    //permission from popup was denied, show error message
                    createDialogInfo(
                        "PlotFragment " + resources.getString(R.string.permission_denied),
                        activity as MainActivity
                    )
                    //Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun editPlot() {
        // записываем в базу данных наш cюжет
        var mess = activity?.resources!!.getString(R.string.sure_save_plot)
        if (saveMess == 2)
            mess = activity?.resources!!.getString(R.string.save_changes_plot_2)
        dialogSavePlot(
            mess, activity as MainActivity, true, mainViewModel, createNewPlot()
        )

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
        edText1.setTextSize(pref?.getString("content_size_key", "18"))
        edText2.setTextSize(pref?.getString("content_size_key", "18"))
        edText3.setTextSize(pref?.getString("content_size_key", "18"))
        edText4.setTextSize(pref?.getString("content_size_key", "18"))
        edText5.setTextSize(pref?.getString("content_size_key", "18"))
        edText6.setTextSize(pref?.getString("content_size_key", "18"))
        edText7.setTextSize(pref?.getString("content_size_key", "18"))
        edText8.setTextSize(pref?.getString("content_size_key", "18"))

        tBook.setTextSize(pref?.getString("title_size_key", "18"))
        tvCT.setTextSize(pref?.getString("title_size_key", "18"))
        textView4.setTextSize(pref?.getString("comments_size_key", "16"))
        textView5.setTextSize(pref?.getString("comments_size_key", "16"))
        textView6.setTextSize(pref?.getString("comments_size_key", "16"))
        textView7.setTextSize(pref?.getString("comments_size_key", "16"))
        textView8.setTextSize(pref?.getString("comments_size_key", "16"))
        textView9.setTextSize(pref?.getString("comments_size_key", "16"))
        textView10.setTextSize(pref?.getString("comments_size_key", "16"))
        textView11.setTextSize(pref?.getString("comments_size_key", "16"))
    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {
        edText1.setTypeface(
            pref?.getString("font_family_content_key", "sans-serif"),
            activity as MainActivity
        )
        edText2.setTypeface(
            pref?.getString("font_family_content_key", "sans-serif"),
            activity as MainActivity
        )
        edText3.setTypeface(
            pref?.getString("font_family_content_key", "sans-serif"),
            activity as MainActivity
        )
        edText4.setTypeface(
            pref?.getString("font_family_content_key", "sans-serif"),
            activity as MainActivity
        )
        edText5.setTypeface(
            pref?.getString("font_family_content_key", "sans-serif"),
            activity as MainActivity
        )
        edText6.setTypeface(
            pref?.getString("font_family_content_key", "sans-serif"),
            activity as MainActivity
        )
        edText7.setTypeface(
            pref?.getString("font_family_content_key", "sans-serif"),
            activity as MainActivity
        )
        edText8.setTypeface(
            pref?.getString("font_family_content_key", "sans-serif"),
            activity as MainActivity
        )



        tBook.setTypeface(
            pref?.getString("font_family_title_key", "sans-serif"),
            activity as MainActivity
        )


        textView4.setTypeface(
            pref?.getString("font_family_comment_key", "sans-serif"),
            activity as MainActivity
        )
        textView5.setTypeface(
            pref?.getString("font_family_comment_key", "sans-serif"),
            activity as MainActivity
        )
        textView6.setTypeface(
            pref?.getString("font_family_comment_key", "sans-serif"),
            activity as MainActivity
        )
        textView7.setTypeface(
            pref?.getString("font_family_comment_key", "sans-serif"),
            activity as MainActivity
        )
        textView8.setTypeface(
            pref?.getString("font_family_comment_key", "sans-serif"),
            activity as MainActivity
        )
        textView9.setTypeface(
            pref?.getString("font_family_comment_key", "sans-serif"),
            activity as MainActivity
        )
        textView10.setTypeface(
            pref?.getString("font_family_comment_key", "sans-serif"),
            activity as MainActivity
        )
        textView11.setTypeface(
            pref?.getString("font_family_comment_key", "sans-serif"),
            activity as MainActivity
        )
        tvCT.setTypeface(
            pref?.getString("font_family_title_key", "sans-serif"),
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
        saveMess = 2
        newPlot = createNewPlot()
        if (newPlot != oldPlot)
            editPlot()

        super.onDestroy()
    }

    companion object {

        @JvmStatic
        fun newInstance() = PlotFragment()

    }


}