package elena.altair.note.dialoghelper

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import elena.altair.note.R
import elena.altair.note.utils.LiterKindHelper

class DialogSpinnerHelper {

    // диалог со списком категорий
    fun showSpinnerDialog(context: Context, list: ArrayList<String>, tvTextView: TextView) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()

        val binding = LayoutInflater.from(context).inflate(R.layout.spinner_layout, null)

        // найдём все элементы нашего диалога
        val adapter = RcViewDialogSpinnerAdapter(dialog, tvTextView)
        val rcView = binding.findViewById<RecyclerView>(R.id.rcSpView)
        val sv = binding.findViewById<SearchView>(R.id.svSpinner)
        rcView.layoutManager = LinearLayoutManager(context)
        rcView.adapter = adapter

        dialog.setView(binding)
        adapter.updateAdapter(list)

        setSearchView(adapter, list, sv)

        dialog.show()
    }


    // обновляем адаптер в зависимости от результата поиска
    private fun setSearchView(
        adapter: RcViewDialogSpinnerAdapter,
        list: ArrayList<String>,
        sv: SearchView?
    ) {
        // добавляем слушатель изменения текста
        sv?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val tempList = LiterKindHelper.filterListData(list, newText)
                adapter.updateAdapter(tempList)
                return true
            }

        })
    }

}