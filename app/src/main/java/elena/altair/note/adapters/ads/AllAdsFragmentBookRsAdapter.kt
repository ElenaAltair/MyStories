package elena.altair.note.adapters.ads

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import elena.altair.note.R
import elena.altair.note.databinding.AdListItemBinding
import elena.altair.note.model.Ad
import elena.altair.note.utils.settings.TimeManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AllAdsFragmentBookRsAdapter(
    val auth: FirebaseAuth,
    val listener: AdListener,
) : RecyclerView.Adapter<AllAdsFragmentBookRsAdapter.AdHolder>() {

    private val adArray = ArrayList<Ad>()
    private var timeFormatter: SimpleDateFormat? = null

    init {
        timeFormatter = SimpleDateFormat("HH:mm:ss - dd/MM/yyyy", Locale.getDefault())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdHolder {
        val binding = AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdHolder(binding, auth, parent.context, listener, timeFormatter)
    }

    override fun getItemCount(): Int {
        return adArray.size
    }

    override fun onBindViewHolder(holder: AdHolder, position: Int) {
        holder.setData(adArray[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<Ad>) {
        if (newList.isNotEmpty()) {
            val tempArray = ArrayList<Ad>()
            tempArray.addAll(adArray)
            if (newList != tempArray) // временная мера
                tempArray.addAll(newList)

            val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray, tempArray))
            diffResult.dispatchUpdatesTo(this) // обновляем наш адаптер

            adArray.clear()
            adArray.addAll(tempArray)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapterWithClear(newList: List<Ad>) {

        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray, newList))
        diffResult.dispatchUpdatesTo(this) // обновляем наш адаптер
        adArray.clear()
        adArray.addAll(newList)
    }


    class AdHolder(
        private val binding: AdListItemBinding,
        private val auth: FirebaseAuth,
        private val context: Context,
        private val listener: AdListener,
        private val timeFormatter: SimpleDateFormat?,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("UseCompatLoadingForDrawables")
        fun setData(ad: Ad) = with(binding) {
            val str = ad.titleBook
            val k = str!!.indexOf("/")
            tvTitle.text = str.substring(k + 2, str.length)
            tvCatAge.text = ad.categoryAge
            tvGenresLiter.text = ad.categoryLiter
            textDiscription.text = ad.description
            tvViewCounter.text = ad.viewsCounter
            tvFavCounter.text = ad.favCounter


            val timePublish = TimeManager.getTimeFormat(getTimeFromMillis(ad.time), null)
            time.text = timePublish
            if (ad.isFav) {
                ibFav.setBackgroundResource(R.drawable.ic_fav_pressed)
            } else {
                ibFav.setBackgroundResource(R.drawable.ic_fav_normal)
            }
            showEditPanel(isOwner(ad))
            mainOnClick(ad)
        }

        private fun getTimeFromMillis(timeMillis: String): String {
            val c = Calendar.getInstance()
            c.timeInMillis = timeMillis.toLong()
            return timeFormatter!!.format(c.time)
        }

        private fun mainOnClick(ad: Ad) = with(binding) {
            ibFav.setOnClickListener {
                if (auth.currentUser?.isAnonymous == false)
                    listener.onFavClicked(ad)

            }
            itemView.setOnClickListener {
                listener.onAdViewed(ad)

            }
            //ibEditAd.setOnClickListener(onClickEdit(ad))
            ibEditAd.setOnClickListener {
                listener.onClickEdit(ad)
            }
            ibDeleteAd.setOnClickListener {
                listener.onUpdateLocalBook(ad)
                listener.onDeleteItem(ad)
            }
        }
        /*
        private fun onClickEdit(ad: Ad): View.OnClickListener {
            return View.OnClickListener {
                val editIntent = Intent(context, EditAdsActivity::class.java).apply {
                    // true - открыли объявление для редактирования
                    // false - создаём новое объявление
                    putExtra(EDIT_STATE_AD, true)
                    putExtra(ADS_DATA, ad)
                }
                context.startActivity(editIntent)

            }
        }*/

        private fun isOwner(ad: Ad): Boolean {
            return ad.uidOwner == auth.uid
        }

        private fun showEditPanel(isOwner: Boolean) {
            if (isOwner)
                binding.editPanel.visibility = View.VISIBLE
            else
                binding.editPanel.visibility = View.GONE
        }
    }

    interface AdListener {
        fun onDeleteItem(ad: Ad)
        fun onAdViewed(ad: Ad)
        fun onFavClicked(ad: Ad)
        fun onUpdateLocalBook(ad: Ad)
        fun onClickEdit(ad: Ad)
    }

}