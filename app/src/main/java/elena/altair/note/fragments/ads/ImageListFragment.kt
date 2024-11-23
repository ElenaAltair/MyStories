package elena.altair.note.fragments.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import elena.altair.note.R
import elena.altair.note.activities.ads.EditAdsActivity
import elena.altair.note.adapters.ads.ImageListFragmentSelectImageRvAdapter
import elena.altair.note.databinding.ListImageFragBinding
import elena.altair.note.dialoghelper.ProgressDialog
import elena.altair.note.utils.ads.AdapterCallback
import elena.altair.note.utils.ads.ImageManager
import elena.altair.note.utils.ads.ImagePicker
import elena.altair.note.utils.ads.ItemTouchMoveCallback
import elena.altair.note.utils.font.setTypeface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ImageListFragment(private val fragCloseInterface: FragmentCloseInterface) : Fragment(),
    AdapterCallback {

    private lateinit var defPref: SharedPreferences
    private var adapter = ImageListFragmentSelectImageRvAdapter(this)
    private var dragCallback = ItemTouchMoveCallback(adapter)
    private var touchHelper = ItemTouchHelper(dragCallback)
    private var job: Job? = null
    private var addImageItem: MenuItem? = null
    lateinit var binding: ListImageFragBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        defPref = PreferenceManager.getDefaultSharedPreferences(activity as EditAdsActivity)
        binding = ListImageFragBinding.inflate(layoutInflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setFontFamily()


        super.onViewCreated(view, savedInstanceState)

        setUpToolbar()
        binding.apply {

            touchHelper.attachToRecyclerView(rcViewSelectImage)
            rcViewSelectImage.layoutManager = LinearLayoutManager(activity)
            rcViewSelectImage.adapter = adapter
        }

    }

    override fun onItemDelete() {
        addImageItem?.isVisible = true
    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>) {
        adapter.updateAdapter(bitmapList, true) // true
    }


    // когда фрагмент отсоединяется от активити
    override fun onDetach() {
        super.onDetach()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this@ImageListFragment)
            ?.commit()
        // в это время мы и запускаем интерфейс FragmentCloseInterface,
        // чтобы он запустился и на активити
        fragCloseInterface.onFragClose(adapter.mainArray)
        job?.cancel()
    }


    fun resizeSelectedImages(
        newList: ArrayList<Uri>,
        needClear: Boolean,
        activity: Activity,
        array: ArrayList<Bitmap>
    ) {

        job = CoroutineScope(Dispatchers.Main).launch {
            val dialog = ProgressDialog.createProgressDialog(activity)
            val bitmapList = ImageManager.imageResize(newList, activity)
            dialog.dismiss()
            array.addAll(bitmapList)
            adapter.updateAdapter(array, needClear)
            if (adapter.mainArray.size > 2) addImageItem?.isVisible = false
        }

    }

    // uri - ссылка на картинку, которую мы получили
    // pos - позиция выбранной картинки (она нужна, чтобы знать на какое место записать новую картинку)
    @SuppressLint("NotifyDataSetChanged")
    fun setSingleImage(
        uri: Uri,
        pos: Int,
        array: ArrayList<Bitmap>,
        edAct: EditAdsActivity
    ) {
        job = CoroutineScope(Dispatchers.Main).launch {
            val dialog = ProgressDialog.createProgressDialog(edAct)
            val bitmapList = ImageManager.imageResize(arrayListOf(uri), edAct)
            dialog.dismiss()
            array[pos] = bitmapList[0]
            adapter.mainArray = array
            adapter.notifyItemChanged(pos)
            if (adapter.mainArray.size > 2) addImageItem?.isVisible = false
        }
    }


    private fun setUpToolbar() {

        binding.apply {

            tb.inflateMenu(R.menu.main_chose_image)
            val deleteImagesItem = binding.tb.menu.findItem(R.id.id_delete_image)
            addImageItem = tb.menu.findItem(R.id.id_add_image)
            if (adapter.mainArray.size > 2) addImageItem?.isVisible = false

            // нажали на стрелку
            binding.tb.setNavigationOnClickListener {
                // закрываем наш фрагмент
                activity?.supportFragmentManager?.beginTransaction()?.remove(this@ImageListFragment)
                    ?.commit()
            }


            // нажали на кнопку удалить все картинки
            deleteImagesItem.setOnMenuItemClickListener {
                adapter.updateAdapter(ArrayList(), true)
                addImageItem?.isVisible = true
                true
            }

            // нажали на кнопку добавить картинку
            addImageItem?.setOnMenuItemClickListener {
                val imageCount = ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size
                ImagePicker.addImages(activity as EditAdsActivity, imageCount, adapter.mainArray)
                true
            }

        }
    }

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {

        val tv: TextView = binding.tb.getChildAt(0) as TextView

        tv.setTypeface(
            defPref.getString("font_family_title_key", "sans-serif"),
            activity as EditAdsActivity
        )
    }
}