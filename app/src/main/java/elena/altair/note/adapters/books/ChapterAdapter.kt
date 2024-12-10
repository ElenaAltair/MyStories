package elena.altair.note.adapters.books

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import elena.altair.note.R
import elena.altair.note.activities.MainActivity
import elena.altair.note.constants.MyConstants.FONT_FAMILY_DEFAULT
import elena.altair.note.constants.MyConstants.FONT_FAMILY_LIST_KEY
import elena.altair.note.databinding.ItemChapterBinding
import elena.altair.note.etities.ChapterEntity2
import elena.altair.note.utils.font.setTypeface
import elena.altair.note.utils.settings.TimeManager
import elena.altair.note.utils.text.textRedactor.HtmlManager

class ChapterAdapter(
    private val listener: Listener,
    private val defPref: SharedPreferences,
    private val mainActivity: MainActivity,
) : ListAdapter<ChapterEntity2, ChapterAdapter.ItemHolder>(ItemComparator()) {

    // функция будет создавать для каждого элемента (для каждой заметки) из базы данных
    // свой ItemHolder, который в себе будет создавать разметку
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder.create(parent, mainActivity)
    }

    // и после того как разметка создана, сразу она заполняется
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.setData(getItem(position), listener, defPref)
    }

    // Во view мы передаем нашу разметку (chapter_item.xml)
    class ItemHolder(view: View, private val mainActivity: MainActivity) :
        RecyclerView.ViewHolder(view) {

        private val binding = ItemChapterBinding.bind(view)

        // от сюда будем заполнять наши TextView в chapter_item, беря данные из базы данных
        fun setData(chapter: ChapterEntity2, listener: Listener, defPref: SharedPreferences) =
            with(binding) {
                tvTitle.text = chapter.titleChapters
                tvShotDescrip.text = HtmlManager.getFromHtml(chapter.shotDescribe).trim()
                tvTime.text = TimeManager.getTimeFormat(chapter.time, defPref)

                tvTitle.setTypeface(
                    defPref.getString(FONT_FAMILY_LIST_KEY, FONT_FAMILY_DEFAULT),
                    mainActivity
                )
                tvShotDescrip.setTypeface(
                    defPref.getString(FONT_FAMILY_LIST_KEY, FONT_FAMILY_DEFAULT),
                    mainActivity
                )
                tvTime.setTypeface(
                    defPref.getString(FONT_FAMILY_LIST_KEY, FONT_FAMILY_DEFAULT),
                    mainActivity
                )


                if (chapter.public_ == "0") {
                    imPublic.setImageResource(R.drawable.ic_public_off)
                } else {
                    imPublic.setImageResource(R.drawable.ic_public)
                }

                imPublic.setOnClickListener {

                    var public = "0"
                    if (chapter.public_ == "0") {
                        imPublic.setImageResource(R.drawable.ic_public_off)
                        public = "1"
                    } else {
                        imPublic.setImageResource(R.drawable.ic_public)
                        public = "0"
                    }
                    val chap = updateChapter(chapter, public)
                    listener.updatePublic(chap)
                }


                itemView.setOnClickListener {
                    listener.onClickItem(chapter)
                }
                imDelete.setOnClickListener {
                    listener.deleteItem(chapter.id!!)
                }
                imEdit.setOnClickListener {  // нажали на кнопку редактировать
                    listener.editItem(chapter)
                }
            }

        private fun updateChapter(chapter: ChapterEntity2, num: String): ChapterEntity2 {
            return chapter.copy(public_ = num)
        }


        companion object {
            fun create(parent: ViewGroup, mainActivity: MainActivity): ItemHolder {
                return ItemHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_chapter, parent, false),
                    mainActivity
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

    interface Listener {
        fun editItem(chapter: ChapterEntity2)
        fun deleteItem(id: Long)
        fun onClickItem(chapter: ChapterEntity2)
        fun updatePublic(chapter: ChapterEntity2)
    }
}