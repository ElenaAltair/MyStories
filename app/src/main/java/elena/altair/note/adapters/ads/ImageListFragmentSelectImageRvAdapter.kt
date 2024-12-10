package elena.altair.note.adapters.ads

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import elena.altair.note.R
import elena.altair.note.activities.ads.EditAdsActivity
import elena.altair.note.databinding.ItemSelectImageFragBinding
import elena.altair.note.utils.ads.AdapterCallback
import elena.altair.note.utils.ads.ImageManager
import elena.altair.note.utils.ads.ImagePicker
import elena.altair.note.utils.ads.ItemTouchMoveCallback

class ImageListFragmentSelectImageRvAdapter(
    val adapterCallback: AdapterCallback,
) :
    RecyclerView.Adapter<ImageListFragmentSelectImageRvAdapter.ImageHolder>(),
    ItemTouchMoveCallback.ItemTouchAdapter {

    var mainArray = ArrayList<Bitmap>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val viewBinding =
            ItemSelectImageFragBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageHolder(viewBinding, parent.context, this)//, editAdsActivity, defPref
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }


    override fun getItemCount(): Int {
        return mainArray.size
    }

    // меняем в массиве элемнты местами, при перетаскивании картинок в RecyclerView
    override fun onMove(startPos: Int, targetPos: Int) {
        val targetItem = mainArray[targetPos]
        mainArray[targetPos] = mainArray[startPos]
        mainArray[startPos] = targetItem
        notifyItemMoved(startPos, targetPos) // сообщаем адаптору, что мы поменяли элементы местами
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onClear() {
        notifyDataSetChanged()
    }

    class ImageHolder(
        private val viewBinding: ItemSelectImageFragBinding,
        val context: Context,
        val adapter: ImageListFragmentSelectImageRvAdapter,
        // private val editAdsActivity: EditAdsActivity,
        // private val defPref: SharedPreferences,
    ) : RecyclerView.ViewHolder(viewBinding.root) {

        fun setData(bitMap: Bitmap) {
            // редактирование одной картинки
            viewBinding.imEditImage.setOnClickListener {
                ImagePicker.getSingleImage(context as EditAdsActivity, adapter.mainArray)
                context.editImagePos = getBindingAdapterPosition()
            }

            // удаление одной картинки
            viewBinding.imDelete.setOnClickListener {

                adapter.mainArray.removeAt(getBindingAdapterPosition())
                adapter.notifyItemRemoved(getBindingAdapterPosition())
                for (n in 0 until adapter.mainArray.size) adapter.notifyItemChanged(n)
                adapter.adapterCallback.onItemDelete()

            }

            viewBinding.tvTitle.text =
                context.resources.getStringArray(R.array.title_array)[getBindingAdapterPosition()]
            ImageManager.chooseScaleType(viewBinding.imageView, bitMap)
            viewBinding.imageView.setImageBitmap(bitMap)

        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean) {

        if (needClear) {
            mainArray.clear()
        }

        mainArray.addAll(newList)
        notifyDataSetChanged()
    }


}