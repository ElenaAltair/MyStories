package elena.altair.note.dialoghelper

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import elena.altair.note.R

class RcViewDialogSpinnerAdapter(
    private var dialog: AlertDialog,
    private var tvTextView: TextView,
) : RecyclerView.Adapter<RcViewDialogSpinnerAdapter.SpViewHolder>() {

    private val mainList = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.spinner_list_item, parent, false)
        return SpViewHolder(view, dialog, tvTextView)
    }

    override fun onBindViewHolder(holder: SpViewHolder, position: Int) {
        holder.setData(mainList[position])
    }

    override fun getItemCount(): Int {
        return mainList.size
    }

    class SpViewHolder(
        itemView: View,
        private var dialog: AlertDialog,
        private var tvTextView: TextView
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var itemText = ""

        fun setData(text: String) {
            val tvSpItem = itemView.findViewById<TextView>(R.id.tvSpItem)
            tvSpItem.text = text
            itemText = text
            itemView.setOnClickListener(this)
        }

        // эта функция запускается при нажатии на элемент списка
        override fun onClick(v: View?) {
            tvTextView.text = itemText
            dialog.dismiss() // закрываем диалог
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list: ArrayList<String>) {
        mainList.clear()
        mainList.addAll(list)
        notifyDataSetChanged()
    }

}


