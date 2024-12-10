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
import elena.altair.note.databinding.ItemHeroBinding
import elena.altair.note.etities.HeroEntity2
import elena.altair.note.utils.font.setTypeface

class HeroAdapter(
    private val listener: Listener,
    private val defPref: SharedPreferences,
    private val mainActivity: MainActivity,
) : ListAdapter<HeroEntity2, HeroAdapter.ItemHolder>(ItemComparator()) {

    // функция будет создавать для каждого элемента (для каждой заметки) из базы данных
    // свой ItemHolder, который в себе будет создавать разметку
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder.create(parent, mainActivity)
    }

    // и после того как разметка создана, сразу она заполняется
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.setData(getItem(position), listener, defPref)
    }

    // Во view мы передаем нашу разметку (hero_item.xml)
    class ItemHolder(view: View, private val mainActivity: MainActivity) :
        RecyclerView.ViewHolder(view) {

        private val binding = ItemHeroBinding.bind(view)

        // от сюда будем заполнять наши TextView в hero_item, беря данные из базы данных
        fun setData(hero: HeroEntity2, listener: Listener, defPref: SharedPreferences) =
            with(binding) {
                tvTitle.text = hero.desc1

                tvTitle.setTypeface(
                    defPref.getString(FONT_FAMILY_LIST_KEY, FONT_FAMILY_DEFAULT),
                    mainActivity
                )

                itemView.setOnClickListener {
                    listener.onClickItem(hero)
                }
                imDelete.setOnClickListener {
                    listener.deleteItem(hero.id!!)
                }
                imEdit.setOnClickListener {  // нажали на кнопку редактировать
                    listener.editItem(hero)
                }
            }

        companion object {
            fun create(parent: ViewGroup, mainActivity: MainActivity): ItemHolder {
                return ItemHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_hero, parent, false),
                    mainActivity
                )
            }
        }
    }


    // класс сравнивающий элементы из старого списка и нового
    class ItemComparator : DiffUtil.ItemCallback<HeroEntity2>() {

        // функция сравнивающая, если отдельные элементы равны
        override fun areItemsTheSame(oldItem: HeroEntity2, newItem: HeroEntity2): Boolean {
            // будем сравнивать по id
            return oldItem.id == newItem.id
        }

        // функция сравнивающая весь контент внутри элемента
        override fun areContentsTheSame(oldItem: HeroEntity2, newItem: HeroEntity2): Boolean {
            return oldItem == newItem
        }
    }

    interface Listener {
        fun editItem(hero: HeroEntity2)
        fun deleteItem(id: Long)
        fun onClickItem(hero: HeroEntity2)
    }
}