package elena.altair.note.fragments.books

import androidx.appcompat.app.AppCompatActivity
import elena.altair.note.R

// Когда мы будем помещать в активити наш фрагмент,
// нам нужно будет знать, какой фрагмент сейчас рабочий
// И если мы хотим поместить в активите друной фрагмент, то этот фрагмент нужно убрать.
// Нам надо знать, какой фрагмент подключен к активити,
// поэтому этот фрагмент будет сохранять в переменнной currentFlag
object FragmentManager {

    var currentFlag: BaseFragment? = null

    // функции для переключения между фрагментами
    fun setFragment(newFrag: BaseFragment, activity: AppCompatActivity) {
        val transaction = activity
            .supportFragmentManager.beginTransaction()
            .replace(R.id.placeHolder, newFrag)
            .commit()

        currentFlag = newFrag
    }

}