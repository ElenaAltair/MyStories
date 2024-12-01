package elena.altair.note.utils.share

import android.content.Context
import android.content.Intent
import elena.altair.note.R
import elena.altair.note.etities.TermEntity2

object ShareHelperTerm {
    fun shareTerm(term: TermEntity2, listName: String, nameA: String, context: Context): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plane"
        intent.apply {
            putExtra(Intent.EXTRA_TEXT, makeShareText(term, listName, nameA, context))
        }
        return intent
    }

    fun makeShareText(
        term: TermEntity2,
        listName: String,
        nameA: String,
        context: Context
    ): String {
        val sBuilder = StringBuilder()
        sBuilder.append("${context.getString(R.string.book_title)} $listName")
        sBuilder.append("\n${context.getString(R.string.author)} $nameA")
        sBuilder.append("\n \n${context.getString(R.string.term_title)}\n")
        sBuilder.append("${term.titleTerm} ")
        sBuilder.append("\n \n${context.getString(R.string.term_content)}\n")
        sBuilder.append("${term.interpretationTerm} ")

        return sBuilder.toString()
    }
}