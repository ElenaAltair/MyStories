package elena.altair.note.adapters.ads

import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import elena.altair.note.R
import elena.altair.note.activities.ads.DescriptionActivity
import elena.altair.note.activities.ads.DescriptionActivity.Companion.CHAPTER_KEY
import elena.altair.note.activities.ads.ReadChapterActivity
import elena.altair.note.databinding.ChapterItemForPublic2Binding
import elena.altair.note.fragments.ads.AllAdsFragment.Companion.AD_KEY
import elena.altair.note.model.Ad
import elena.altair.note.model.ChapterPublic
import elena.altair.note.utils.font.setTextSize
import elena.altair.note.utils.font.setTypeface

class DescriptionActivityChapterRsAdapter(
    private val activity: DescriptionActivity,
    private val ad: Ad,
    private val defPref: SharedPreferences,
) : ListAdapter<ChapterPublic, DescriptionActivityChapterRsAdapter.ChapHolder>(ItemComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapHolder {
        return ChapHolder.create(parent, activity, defPref)
    }

    override fun onBindViewHolder(holder: ChapHolder, position: Int) {
        holder.setDate(getItem(position), ad)
    }

    class ChapHolder(
        view: View,
        val parent: ViewGroup,
        val activity: DescriptionActivity,
        val defPref: SharedPreferences,
    ) : RecyclerView.ViewHolder(view) {

        private val binding = ChapterItemForPublic2Binding.bind(view)

        fun setDate(
            chapter: ChapterPublic,
            ad: Ad,
        ) = with(binding) {
            //tvIdChap.text = chapter.id.toString()
            tvIdChap.text = chapter.idLocal
            tvNumChap.text = chapter.number.toString()
            tvChapTitle.text = chapter.titleChapters.toString()
            setTextSize()
            setFontFamily()

            itemChapter.setOnClickListener {
                val i = Intent(parent.context, ReadChapterActivity::class.java)
                i.putExtra(CHAPTER_KEY, chapter)
                i.putExtra(AD_KEY, ad)
                parent.context.startActivity(i)
            }
        }

        // функция для выбора размера текста
        private fun setTextSize() = with(binding) {
            tvNumChap.setTextSize(defPref.getString("content_size_key", "18"))
            tvChapTitle.setTextSize(defPref.getString("content_size_key", "18"))
        }

        //функция изменения fontFamily
        private fun setFontFamily() = with(binding) {

            tvNumChap.setTypeface(
                defPref.getString("font_family_content_key", "sans-serif"),
                activity
            )
            tvChapTitle.setTypeface(
                defPref.getString("font_family_content_key", "sans-serif"),
                activity
            )

        }


        companion object {
            fun create(
                parent: ViewGroup,
                activity: DescriptionActivity,
                defPref: SharedPreferences
            ): ChapHolder {
                return ChapHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.chapter_item_for_public_2, parent, false),
                    parent, activity, defPref
                )
            }
        }

    }

    // класс сравнивающий элементы из старого списка и нового
    class ItemComparator : DiffUtil.ItemCallback<ChapterPublic>() {

        // функция сравнивающая, если отдельные элементы равны
        override fun areItemsTheSame(oldItem: ChapterPublic, newItem: ChapterPublic): Boolean {
            // будем сравнивать по id
            return oldItem.idLocal == newItem.idLocal
        }

        // функция сравнивающая весь контент внутри элемента
        override fun areContentsTheSame(oldItem: ChapterPublic, newItem: ChapterPublic): Boolean {
            return oldItem == newItem
        }
    }

    interface ChapPListener {

    }
}