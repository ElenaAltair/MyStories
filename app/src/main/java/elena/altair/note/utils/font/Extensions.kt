package elena.altair.note.utils.font

import android.app.Activity
import android.graphics.Typeface
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import elena.altair.note.R

fun EditText.setTypeface(fontFamily: String?, activity: Activity) {

    if (fontFamily != null) {
        if (fontFamily.contains("R.font.")) {

            if (fontFamily == "R.font.asylbekm29kz")
                this.typeface = ResourcesCompat.getFont(activity, R.font.asylbekm29kz)

            if (fontFamily == "R.font.couriernewcyr80n")
                this.typeface = ResourcesCompat.getFont(activity, R.font.couriernewcyr80n)

            if (fontFamily == "R.font.dited")
                this.typeface = ResourcesCompat.getFont(activity, R.font.dited)

            if (fontFamily == "R.font.gildiatitulcmbold")
                this.typeface = ResourcesCompat.getFont(activity, R.font.gildiatitulcmbold)

            if (fontFamily == "R.font.karnacone")
                this.typeface = ResourcesCompat.getFont(activity, R.font.karnacone)

            if (fontFamily == "R.font.villaphelomena")
                this.typeface = ResourcesCompat.getFont(activity, R.font.villaphelomena)

            if (fontFamily == "R.font.josephinac")
                this.typeface = ResourcesCompat.getFont(activity, R.font.josephinac)

            if (fontFamily == "R.font.capsmall")
                this.typeface = ResourcesCompat.getFont(activity, R.font.capsmall)

            if (fontFamily == "R.font.perfoc")
                this.typeface = ResourcesCompat.getFont(activity, R.font.perfoc)

            if (fontFamily == "R.font.schoolbook")
                this.typeface = ResourcesCompat.getFont(activity, R.font.schoolbook)

        } else {
            this.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL))
        }
    }
}

fun EditText.setTextSize(size: String?) {
    if (size != null) this.textSize = size.toFloat()
}

fun TextView.setTextSize(size: String?) {
    if (size != null) this.textSize = size.toFloat()
}

fun TextView.setTypeface(fontFamily: String?, activity: Activity) {

    if (fontFamily != null) {
        if (fontFamily.contains("R.font.")) {

            if (fontFamily == "R.font.asylbekm29kz")
                this.typeface = ResourcesCompat.getFont(activity, R.font.asylbekm29kz)

            if (fontFamily == "R.font.couriernewcyr80n")
                this.typeface = ResourcesCompat.getFont(activity, R.font.couriernewcyr80n)

            if (fontFamily == "R.font.dited")
                this.typeface = ResourcesCompat.getFont(activity, R.font.dited)

            if (fontFamily == "R.font.gildiatitulcmbold")
                this.typeface = ResourcesCompat.getFont(activity, R.font.gildiatitulcmbold)

            if (fontFamily == "R.font.karnacone")
                this.typeface = ResourcesCompat.getFont(activity, R.font.karnacone)

            if (fontFamily == "R.font.villaphelomena")
                this.typeface = ResourcesCompat.getFont(activity, R.font.villaphelomena)

            if (fontFamily == "R.font.josephinac")
                this.typeface = ResourcesCompat.getFont(activity, R.font.josephinac)

            if (fontFamily == "R.font.capsmall")
                this.typeface = ResourcesCompat.getFont(activity, R.font.capsmall)

            if (fontFamily == "R.font.perfoc")
                this.typeface = ResourcesCompat.getFont(activity, R.font.perfoc)

            if (fontFamily == "R.font.schoolbook")
                this.typeface = ResourcesCompat.getFont(activity, R.font.schoolbook)

        } else {
            this.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL))
        }
    }
}




