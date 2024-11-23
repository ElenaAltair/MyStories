package elena.altair.note.utils.ads

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemTouchMoveCallback(val adapter: ItemTouchAdapter) : ItemTouchHelper.Callback() {

    // указатель: какие именно движения мы хотим замечать
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlag, 0)
    }

    // функция работает, когда мы двигаем элемент
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onMove(viewHolder.adapterPosition, target.adapterPosition)
        //adapter.onMove(viewHolder.layoutPosition - 1, target.layoutPosition - 1)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    // Эта функция запускается, когда мы нажали на наш элемент
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        // добавим прозрачность к перетаскиваемому элементу
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            viewHolder?.itemView?.alpha = 0.5f
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    // функция срабатывает при отпускании элемента
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        // делаем элемент снова непрозрачным
        viewHolder.itemView.alpha = 1.0f
        adapter.onClear() // чтобы картинки менялись местами, а заголовки оставлись в старом порядке на старых местах
        super.clearView(recyclerView, viewHolder)
    }

    // создадим интерфейс, чтобы связать этот класс с классом SelectImageRvAdapter
    interface ItemTouchAdapter {
        fun onMove(startPos: Int, targetPos: Int)
        fun onClear()
    }
}