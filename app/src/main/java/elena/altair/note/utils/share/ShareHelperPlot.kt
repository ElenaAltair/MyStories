package elena.altair.note.utils.share

import android.content.Context
import android.content.Intent
import elena.altair.note.R
import elena.altair.note.etities.PlotEntity2

object ShareHelperPlot {
    fun sharePlot(plot: PlotEntity2, listName: String, nameA: String, context: Context): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plane"
        intent.apply {
            putExtra(Intent.EXTRA_TEXT, makeShareText(plot, listName, nameA, context))
        }
        return intent
    }

    fun makeShareText(plot: PlotEntity2, listName: String, nameA: String, context: Context): String {
        val sBuilder = StringBuilder()
        sBuilder.append("${context.getString(R.string.book_title)} $listName")
        sBuilder.append("\n${context.getString(R.string.author)} $nameA")
        sBuilder.append("\n \n${context.getString(R.string.plot_edit_1)}\n")
        sBuilder.append("${plot.desc1} ")
        sBuilder.append("\n \n${context.getString(R.string.plot_edit_2)}\n")
        sBuilder.append("${plot.desc2} ")
        sBuilder.append("\n \n${context.getString(R.string.plot_edit_3)}\n")
        sBuilder.append("${plot.desc3} ")
        sBuilder.append("\n \n${context.getString(R.string.plot_edit_4)}\n")
        sBuilder.append("${plot.desc4} ")
        sBuilder.append("\n \n${context.getString(R.string.plot_edit_5)}\n")
        sBuilder.append("${plot.desc5} ")
        sBuilder.append("\n \n${context.getString(R.string.plot_edit_6)}\n")
        sBuilder.append("${plot.desc6} ")
        sBuilder.append("\n \n${context.getString(R.string.plot_edit_7)}\n")
        sBuilder.append("${plot.desc7} ")
        sBuilder.append("\n \n${context.getString(R.string.plot_edit_8)}\n")
        sBuilder.append("${plot.desc8} ")
        sBuilder.append("\n \n")

        return sBuilder.toString()
    }
}