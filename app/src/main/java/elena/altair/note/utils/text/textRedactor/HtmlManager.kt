package elena.altair.note.utils.text.textRedactor

import android.text.Html
import android.text.Spanned

// класс для сохранения стилей в редактируемом тексте
object HtmlManager {
    fun getFromHtml(text: String?): Spanned {
        return if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(text) // для старых версий sdk
        } else {
            Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT) // для новых версий sdk
        }
    }

    // сохраняем текст в виде html
    fun toHtml(text: Spanned): String {
        return if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N) {
            Html.toHtml(text) // для старых версий sdk
        } else {
            Html.toHtml(text, Html.FROM_HTML_MODE_COMPACT) // для новых версий sdk
        }
    }
}