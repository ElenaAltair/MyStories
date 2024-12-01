package elena.altair.note.utils.text.textRedactor

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener

// класс для перетаскивания элемента, в нашем случае панельки с цветами
// этот слушатель надо будет добавить к нашему элементу
class MyTouchListener : OnTouchListener {
    var xDelta = 0.0f
    var yDelta = 0.0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {

                xDelta = v!!.x - event.rawX
                yDelta = v.y - event.rawY

            }

            MotionEvent.ACTION_MOVE -> {
                v!!.x = xDelta + event.rawX
                v.y = yDelta + event.rawY
            }
        }
        return true
    }

}