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
import elena.altair.note.databinding.BookItemBinding
import elena.altair.note.etities.BookEntity7
import elena.altair.note.utils.font.setTypeface
import elena.altair.note.utils.text.textRedactor.HtmlManager
import elena.altair.note.utils.settings.TimeManager

class BookAdapter(
    private val listener: Listener,
    private val defPref: SharedPreferences,
    private val mainActivity: MainActivity
) : ListAdapter<BookEntity7, BookAdapter.ItemHolder>(ItemComparator()) {

    // функция будет создавать для каждого элемента (для каждой заметки) из базы данных
    // свой ItemHolder, который в себе будет создавать разметку
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder.create(parent, mainActivity)
    }

    // и после того как разметка создана, сразу она заполняется
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.setData(getItem(position), listener, defPref)
    }

    // Во view мы передаем нашу разметку (book_item.xml)
    class ItemHolder(view: View, private val mainActivity: MainActivity) : RecyclerView.ViewHolder(view) {

        private val binding = BookItemBinding.bind(view)

        // от сюда будем заполнять наши TextView в book_item, беря данные из базы данных
        fun setData(book: BookEntity7, listener: Listener, defPref: SharedPreferences) = with(binding){
            tvTitle.text = book.titleBook
            tvShotDescrip.text = HtmlManager.getFromHtml(book.shotDescribe).trim()
            tvTime.text = TimeManager.getTimeFormat(book.time, defPref)

            tvTitle.setTypeface(
                defPref.getString("font_family_list_key", "sans-serif"),
                mainActivity
            )
            tvShotDescrip.setTypeface(
                defPref.getString("font_family_list_key", "sans-serif"),
                mainActivity
            )
            tvTime.setTypeface(
                defPref.getString("font_family_list_key", "sans-serif"),
                mainActivity
            )

            itemView.setOnClickListener {
                listener.onClickItem(book)
            }
            imDelete.setOnClickListener {
                listener.deleteItem(book.id!!)
            }
            imEdit.setOnClickListener {  // нажали на кнопку редактировать
                listener.editItem(book)
            }
            imEditIn.setOnClickListener {
                listener.editInItem(book)
            }
        }

        companion object{
            fun create(parent: ViewGroup, mainActivity: MainActivity) : ItemHolder {
                return ItemHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.book_item, parent, false),
                    mainActivity
                )
            }
        }
    }

    // класс сравнивающий элементы из старого списка и нового
    class ItemComparator : DiffUtil.ItemCallback<BookEntity7>(){

        // функция сравнивающая, если отдельные элементы равны
        override fun areItemsTheSame(oldItem: BookEntity7, newItem: BookEntity7): Boolean {
            // будем сравнивать по id
            return oldItem.id == newItem.id
        }

        // функция сравнивающая весь контент внутри элемента
        override fun areContentsTheSame(oldItem: BookEntity7, newItem: BookEntity7): Boolean {
            return oldItem == newItem
        }
    }

    interface Listener {
        fun editItem(book: BookEntity7)
        fun deleteItem(id: Long)
        fun onClickItem(book: BookEntity7)
        fun editInItem(book: BookEntity7)
    }
}