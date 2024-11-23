package elena.altair.note.utils.text

import android.app.Activity
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.EditText
import androidx.core.content.ContextCompat


object TextStyle {
    // сделать жирным выбранный текст
    fun setBoldForSelectedText(start: Int, end:Int, view: EditText){

        // определим какой текст выбран для изменения стиля на жиррный
        val startPos = start
        val endPos = end

        // сделаем проверку, не применен ли к выбранному тексту уже до этого стиль bold
        // getSpans() - класс для работы со стилями
        // в переменной styles будет храниться сколько стилей применено к тексту между данными позициями
        val styles = view.text.getSpans(startPos, endPos, StyleSpan::class.java)
        // проверяем был ли жирный шрифт, если да, то убираем жирный шрифт, если нет, то применяем стиль bold
        var boldStyle: StyleSpan? = null
        if (styles.isNotEmpty() && styles[0].style == Typeface.BOLD) {
            // styles[0] - удаляем стиль, который находится на нулевой позиции, т.к. у нас только 1 стиль, это bold
            view.text.removeSpan(styles[0])
        } else if (styles.isNotEmpty() && styles[0].style == Typeface.ITALIC) {
            view.text.removeSpan(styles[0])
            boldStyle = StyleSpan(Typeface.BOLD)
        } else {
            boldStyle = StyleSpan(Typeface.BOLD)
        }
        view.text.setSpan(boldStyle, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        // после этой процедуры у нас добавятся пробелы, которые нужно убрать
        view.text.trim()
        // делаем так, чтобы курсор оказался в конце текста, который мы выбрали
        view.setSelection(endPos)
        //view.clearFocus()
        // теперь надо сделать так, чтобы эти изменения стилей, сохранились в базе данных (используй: utils/HtmlManager)

    }


    // сделать курсивным выбранный текст
    fun setItalicForSelectedText(start: Int, end:Int, view: EditText){

        // определим какой текст выбран для изменения стиля на жиррный
        val startPos = start
        val endPos = end

        // сделаем проверку, не применен ли к выбранному тексту уже до этого стиль bold
        // getSpans() - класс для работы со стилями
        // в переменной styles будет храниться сколько стилей применено к тексту между данными позициями
        val styles = view.text.getSpans(startPos, endPos, StyleSpan::class.java)

        // проверяем был ли жирный шрифт, если да, то убираем жирный шрифт, если нет, то применяем стиль bold
        var italicStyle: StyleSpan? = null
        if (styles.isNotEmpty() && styles[0].style == Typeface.BOLD) {
            // styles[0] - удаляем стиль, который находится на нулевой позиции
            view.text.removeSpan(styles[0])
            italicStyle = StyleSpan(Typeface.ITALIC)
        } else if (styles.isNotEmpty() && styles[0].style == Typeface.ITALIC) {
            view.text.removeSpan(styles[0])
        } else {
            italicStyle = StyleSpan(Typeface.ITALIC)
        }
        view.text.setSpan(italicStyle, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        // после этой процедуры у нас добавятся пробелы, которые нужно убрать
        view.text.trim()
        // делаем так, чтобы курсор оказался в конце текста, который мы выбрали
        view.setSelection(endPos)
        //view.clearFocus()
        // теперь надо сделать так, чтобы эти изменения стилей, сохранились в базе данных (используй: utils/HtmlManager)

    }


    // сделать подчеркнутым выбранный текст
    fun setUnderlineForSelectedText(start: Int, end:Int, view: EditText){

        // определим какой текст выбран для изменения стиля на жиррный
        val startPos = start
        val endPos = end

        // сделаем проверку, не применен ли к выбранному тексту уже до этого стиль bold
        // getSpans() - класс для работы со стилями
        // в переменной styles будет храниться сколько стилей применено к тексту между данными позициями
        val styles = view.text.getSpans(startPos, endPos, UnderlineSpan::class.java)

        if (styles.isNotEmpty()) {
            // styles[0] - удаляем стиль, который находится на нулевой позиции
            view.text.removeSpan(styles[0])
        } else {
            view.text.setSpan(UnderlineSpan(), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // после этой процедуры у нас добавятся пробелы, которые нужно убрать
        view.text.trim()
        // делаем так, чтобы курсор оказался в конце текста, который мы выбрали
        view.setSelection(endPos)
        //view.clearFocus()
        // теперь надо сделать так, чтобы эти изменения стилей, сохранились в базе данных (используй: utils/HtmlManager)

    }


    // сделать зачеркнутым выбранный текст
    fun setStrikethroughForSelectedText(start: Int, end:Int, view: EditText){

        // определим какой текст выбран для изменения стиля на жиррный
        val startPos = start
        val endPos = end

        // сделаем проверку, не применен ли к выбранному тексту уже до этого стиль bold
        // getSpans() - класс для работы со стилями
        // в переменной styles будет храниться сколько стилей применено к тексту между данными позициями
        val styles = view.text.getSpans(startPos, endPos, StrikethroughSpan::class.java)
        val spannableString = SpannableString(view.getText().toString())

        if (styles.isNotEmpty() ) {
            // styles[0] - удаляем стиль, который находится на нулевой позиции
            view.text.removeSpan(styles[0])
        } else {
            view.text.setSpan(StrikethroughSpan(), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // после этой процедуры у нас добавятся пробелы, которые нужно убрать
        view.text.trim()
        // делаем так, чтобы курсор оказался в конце текста, который мы выбрали
        view.setSelection(endPos)
        //view.clearFocus()
        // теперь надо сделать так, чтобы эти изменения стилей, сохранились в базе данных (используй: utils/HtmlManager)

    }


    // поменять цвет выбранному тексту
    // эту функцию мы будем запускать, когда будем нажимать на какой-нибудь цвет в нашей палитре
    fun setColorForSelectedText(colorId: Int, start: Int, end:Int, view: EditText, activity: Activity){
        // определим какой текст выбран для изменения цвета
        val startPos = start
        val endPos = end

        // сделаем проверку, не применен ли к выбранному тексту уже до этого этот стиль
        // getSpans() - класс для работы со стилями
        // в переменной styles будет храниться сколько стилей применено к тексту между данными позициями
        val styles = view.text.getSpans(startPos, endPos, ForegroundColorSpan::class.java)

        if (styles.isNotEmpty()) view.text.removeSpan(styles[0])


        view.text.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(activity, colorId)
            ),
            startPos,
            endPos,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // после этой процедуры у нас добавятся пробелы, которые нужно убрать
        view.text.trim()
        // делаем так, чтобы курсор оказался в конце текста, который мы выбрали
        view.setSelection(endPos)
        // теперь надо сделать так, чтобы эти изменения стилей, сохранились в базе данных (используй: utils/HtmlManager)
    }
}