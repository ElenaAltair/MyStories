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
import elena.altair.note.databinding.TermItemBinding
import elena.altair.note.etities.TermEntity2
import elena.altair.note.utils.font.setTypeface
import elena.altair.note.utils.text.textRedactor.HtmlManager

class TermAdapter(
    private val listener: Listener,
    private val defPref: SharedPreferences,
    private val mainActivity: MainActivity,
) : ListAdapter<TermEntity2, TermAdapter.ItemHolder>(ItemComparator()){

    // функция будет создавать для каждого элемента (для каждой заметки) из базы данных
    // свой ItemHolder, который в себе будет создавать разметку
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder.create(parent, mainActivity)
    }

    // и после того как разметка создана, сразу она заполняется
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.setData(getItem(position), listener, defPref)
    }

    // Во view мы передаем нашу разметку (term_item.xml)
    class ItemHolder(val view: View, private val mainActivity: MainActivity) : RecyclerView.ViewHolder(view) {

        private val binding = TermItemBinding.bind(view)

        // от сюда будем заполнять наши TextView в term_item, беря данные из базы данных
        fun setData(term: TermEntity2, listener: Listener, defPref: SharedPreferences) = with(binding) {
            tvTitle.text = term.titleTerm
            tvDesc.text = HtmlManager.getFromHtml(term.interpretationTerm)

            tvTitle.setTypeface(
                defPref.getString("font_family_list_key", "sans-serif"),
                mainActivity
            )
            tvDesc.setTypeface(
                defPref.getString("font_family_list_key", "sans-serif"),
                mainActivity
            )


            itemView.setOnClickListener {
                listener.onClickItem(term)
            }
            imDelete.setOnClickListener {
                listener.deleteItem(term.id!!)
            }
            imEdit.setOnClickListener {  // нажали на кнопку редактировать
                listener.editItem(term)
            }
        }

        companion object{
            fun create(parent: ViewGroup, mainActivity: MainActivity) : ItemHolder {

                return ItemHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.term_item, parent, false),
                    mainActivity
                )
            }
        }
    }

    // класс сравнивающий элементы из старого списка и нового
    class ItemComparator : DiffUtil.ItemCallback<TermEntity2>(){

        // функция сравнивающая, если отдельные элементы равны
        override fun areItemsTheSame(oldItem: TermEntity2, newItem: TermEntity2): Boolean {
            // будем сравнивать по id
            return oldItem.id == newItem.id
        }

        // функция сравнивающая весь контент внутри элемента
        override fun areContentsTheSame(oldItem: TermEntity2, newItem: TermEntity2): Boolean {
            return oldItem == newItem
        }
    }

    interface Listener {
        fun editItem(term: TermEntity2)
        fun deleteItem(id: Long)
        fun onClickItem(term: TermEntity2)
    }
}