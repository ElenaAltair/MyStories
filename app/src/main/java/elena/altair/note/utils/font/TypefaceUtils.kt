package elena.altair.note.utils.font

import android.app.Activity
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.text.style.TypefaceSpan
import androidx.appcompat.app.ActionBar
import androidx.core.content.res.ResourcesCompat
import elena.altair.note.R

object TypefaceUtils {
    fun typeface(fontFamily: String?, activity: Activity): Typeface? {

        if (fontFamily != null) {
            if (fontFamily.contains("R.font.")) {

                if (fontFamily == "R.font.asylbekm29kz")
                    return ResourcesCompat.getFont(activity, R.font.asylbekm29kz)

                if (fontFamily == "R.font.couriernewcyr80n")
                    return ResourcesCompat.getFont(activity, R.font.couriernewcyr80n)

                if (fontFamily == "R.font.dited")
                    return ResourcesCompat.getFont(activity, R.font.dited)

                if (fontFamily == "R.font.gildiatitulcmbold")
                    return ResourcesCompat.getFont(activity, R.font.gildiatitulcmbold)

                if (fontFamily == "R.font.karnacone")
                    return ResourcesCompat.getFont(activity, R.font.karnacone)

                if (fontFamily == "R.font.villaphelomena")
                    return ResourcesCompat.getFont(activity, R.font.villaphelomena)

                if (fontFamily == "R.font.josephinac")
                    return ResourcesCompat.getFont(activity, R.font.josephinac)

                if (fontFamily == "R.font.capsmall")
                    return ResourcesCompat.getFont(activity, R.font.capsmall)

                if (fontFamily == "R.font.perfoc")
                    return ResourcesCompat.getFont(activity, R.font.perfoc)

                if (fontFamily == "R.font.schoolbook")
                    return ResourcesCompat.getFont(activity, R.font.schoolbook)



            } else {
                return Typeface.create(fontFamily, Typeface.NORMAL)
            }
        }
        return null
    }

    // изменение FontFamily в ActionBar, если объект типа CharSequence
    fun setTitleActionBar(title: String, font: Typeface?, supportActionBar: ActionBar?) {

        val mNewTitle = SpannableString(title) //
        mNewTitle.setSpan(
            font?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    TypefaceSpan(it)
                } else {
                    CustomTypefaceSpan(it)
                }
            },
            0,
            mNewTitle.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        val actionBar = supportActionBar
        actionBar?.setTitle(mNewTitle)

    }

    // изменение FontFamily в ToolBar, если объект типа CharSequence
    fun setTitleToolBar(title: CharSequence?, font: Typeface?): SpannableString {

        val mNewTitle = SpannableString(title) //
        mNewTitle.setSpan(
            font?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    TypefaceSpan(it)
                } else {
                    CustomTypefaceSpan(it)
                }
            },
            0,
            mNewTitle.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        return mNewTitle
    }

    // изменение FontFamily в кнопках BottomNavigationView, если объект типа CharSequence
    fun setTextBottomNav(title: CharSequence?, font: Typeface?): SpannableString {
        val spannableTitle = SpannableString(title)
        spannableTitle.setSpan(
            font?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    TypefaceSpan(it)
                } else {
                    CustomTypefaceSpan(it)
                }
            },
            0,
            spannableTitle.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        return spannableTitle
    }



    // для изменения FontFamily для версии ниже P (28)
    class CustomTypefaceSpan(private val typeface: Typeface?) : MetricAffectingSpan() {
        override fun updateDrawState(paint: TextPaint) {
            paint.typeface = typeface
        }
        override fun updateMeasureState(paint: TextPaint) {
            paint.typeface = typeface
        }
    }

}