package elena.altair.note.utils.share

import android.content.Context
import android.content.Intent
import elena.altair.note.R
import elena.altair.note.etities.ThemeEntity2

object ShareHelperTheme {
    fun shareTheme(theme: ThemeEntity2, listName: String, nameA: String, context: Context): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plane"
        intent.apply {
            putExtra(
                Intent.EXTRA_TEXT, makeShareText(
                    theme,
                    listName,
                    nameA,
                    context
                )
            )
        }
        return intent
    }

    fun makeShareText(
        theme: ThemeEntity2,
        listName: String,
        nameA: String,
        context: Context
    ): String {
        val sBuilder = StringBuilder()

        sBuilder.append("${context.getString(R.string.book_title)} $listName")
        sBuilder.append("\n")
        sBuilder.append("${context.getString(R.string.author)} $nameA")
        sBuilder.append("\n")
        sBuilder.append("\n \n${context.getString(R.string.theme_edit_1)}\n")
        sBuilder.append("${theme.desc1} ")
        sBuilder.append("\n \n${context.getString(R.string.theme_edit_2)}\n")
        sBuilder.append("${theme.desc2} ")
        sBuilder.append("\n \n${context.getString(R.string.theme_edit_3)}\n")
        sBuilder.append("${theme.desc3} ")
        sBuilder.append("\n \n${context.getString(R.string.theme_edit_4)}\n")
        sBuilder.append("${theme.desc4} ")
        sBuilder.append("\n \n${context.getString(R.string.theme_edit_5)}\n")
        sBuilder.append("${theme.desc5} ")
        sBuilder.append("\n \n${context.getString(R.string.theme_edit_6)}\n")
        sBuilder.append("${theme.desc6} ")
        sBuilder.append("\n \n${context.getString(R.string.theme_edit_7)}\n")
        sBuilder.append("${theme.desc7} ")
        sBuilder.append("\n \n${context.getString(R.string.theme_edit_8)}\n")
        sBuilder.append("${theme.desc8} ")

        return sBuilder.toString()
    }
}
