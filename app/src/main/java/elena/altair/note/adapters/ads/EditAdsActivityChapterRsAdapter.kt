package elena.altair.note.adapters.ads

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import elena.altair.note.R
import elena.altair.note.activities.ads.EditAdsActivity
import elena.altair.note.databinding.ChapterItemForPublicBinding
import elena.altair.note.etities.ChapterEntity2
import elena.altair.note.utils.font.setTextSize
import elena.altair.note.utils.font.setTypeface

class EditAdsActivityChapterRsAdapter(
    private val listenerP: ChapPListener,
    private val defPref: SharedPreferences,
    private val act: EditAdsActivity,
) : ListAdapter<ChapterEntity2, EditAdsActivityChapterRsAdapter.ChapHolder>(ItemComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapHolder {
        return ChapHolder.create(parent, defPref, act)
    }

    override fun onBindViewHolder(holder: ChapHolder, position: Int) {
        holder.setDate(getItem(position), listenerP)
    }

    class ChapHolder(
        view: View,
        val defPref: SharedPreferences,
        val act: EditAdsActivity,
    ) : RecyclerView.ViewHolder(view) {

        private val binding = ChapterItemForPublicBinding.bind(view)

        fun setDate(chapter: ChapterEntity2, listenerP: ChapPListener) = with(binding) {
            tvIdChap.text = chapter.id.toString()
            tvNumChap.text = chapter.number.toString()
            tvChapTitle.text = chapter.titleChapters.toString()
            setTextSize()
            setFontFamily()

            if (chapter.public_ == "0") {
                imPublic.setImageResource(R.drawable.ic_public_off_color)
            } else {
                imPublic.setImageResource(R.drawable.ic_public_color)
            }

            imPublic.setOnClickListener {

                var public = "0"
                if (chapter.public_ == "0") {
                    imPublic.setImageResource(R.drawable.ic_public_off_color)
                    public = "1"
                } else {
                    imPublic.setImageResource(R.drawable.ic_public_color)
                    public = "0"
                }
                val chap = updateChapter(chapter, public)
                listenerP.updatePublic(chap)
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
                act
            )
            tvChapTitle.setTypeface(
                defPref.getString("font_family_content_key", "sans-serif"),
                act
            )

        }

        private fun updateChapter(chapter: ChapterEntity2, num: String): ChapterEntity2 {
            return chapter.copy(public_ = num)
        }

        companion object {
            fun create(parent: ViewGroup, defPref: SharedPreferences, act: EditAdsActivity): ChapHolder {
                return ChapHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.chapter_item_for_public, parent, false),
                    defPref, act
                )
            }
        }

    }

    // класс сравнивающий элементы из старого списка и нового
    class ItemComparator : DiffUtil.ItemCallback<ChapterEntity2>() {

        // функция сравнивающая, если отдельные элементы равны
        override fun areItemsTheSame(oldItem: ChapterEntity2, newItem: ChapterEntity2): Boolean {
            // будем сравнивать по id
            return oldItem.id == newItem.id
        }

        // функция сравнивающая весь контент внутри элемента
        override fun areContentsTheSame(oldItem: ChapterEntity2, newItem: ChapterEntity2): Boolean {
            return oldItem == newItem
        }
    }

    interface ChapPListener {
        fun updatePublic(chapter: ChapterEntity2)
    }
}