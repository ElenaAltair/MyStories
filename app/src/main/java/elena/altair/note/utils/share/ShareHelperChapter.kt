package elena.altair.note.utils.share

import android.content.Context
import android.content.Intent
import elena.altair.note.R
import elena.altair.note.etities.ChapterEntity2

object ShareHelperChapter {
    fun shareChapter(chapter: ChapterEntity2, listName: String, nameA: String,  context: Context): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plane"
        intent.apply {
            putExtra(Intent.EXTRA_TEXT, makeShareText(
                chapter,
                listName,
                nameA,
                context
            ))
        }
        return intent
    }

    fun makeShareText(chapter: ChapterEntity2, listName: String, nameA: String, context: Context): String {
        val sBuilder = StringBuilder()
        sBuilder.append("${context.getString(R.string.book_title)} $listName")
        sBuilder.append("\n")
        sBuilder.append("${context.getString(R.string.author)} $nameA")
        sBuilder.append("\n")

        sBuilder.append("${chapter.number} - ${context.getString(R.string.chapter_title)} ${chapter.titleChapters} " +
                "\n \n${context.getString(R.string.short_description)} \n ${chapter.shotDescribe} " +
                "\n \n${context.getString(R.string.content_)} \n ${chapter.context}")
        sBuilder.append("\n")

        return sBuilder.toString()
    }
}