package elena.altair.note.utils.share

import android.content.Context
import android.content.Intent
import elena.altair.note.R
import elena.altair.note.etities.BookEntity4

object ShareHelperBook {
    fun shareBook(book: BookEntity4, listName: String, context: Context): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plane"
        intent.apply {
            putExtra(Intent.EXTRA_TEXT, makeShareTextBook(book, listName, context))
        }
        return intent
    }

    fun makeShareTextBook(book: BookEntity4, listName: String, context: Context): String {
        val sBuilder = StringBuilder()
        sBuilder.append("${context.getString(R.string.book_title)} $listName")
        sBuilder.append("\n")

        sBuilder.append("\n${context.getString(R.string.kind_literature)} \n ${book.kindLiterature} ")
        sBuilder.append("\n")
        sBuilder.append("\n${context.getString(R.string.genres_literature)} \n ${book.genreLiterature} ")
        sBuilder.append("\n")
        sBuilder.append("\n${context.getString(R.string.age_cat)} \n ${book.ageCat} ")
        sBuilder.append("\n")
        sBuilder.append("\n${context.getString(R.string.short_description)} \n ${book.shotDescribe} ")
        sBuilder.append("\n")

        return sBuilder.toString()
    }
}