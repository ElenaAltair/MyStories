package elena.altair.note.utils.settings

import android.content.SharedPreferences
import android.os.Build
import elena.altair.note.constants.MyConstants.TIME_FORMAT_DEFAULT
import elena.altair.note.constants.MyConstants.TIME_FORMAT_KEY
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


object TimeManager {
    //private const val DEF_TIME_FORMAT = "HH:mm:ss - dd/MM/yyyy"

    fun getCurrentTime(): String {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern(TIME_FORMAT_DEFAULT)
            return current.format(formatter)
        }

        val formatter = SimpleDateFormat(TIME_FORMAT_DEFAULT, Locale.getDefault())
        return formatter.format(Calendar.getInstance().time)
    }

    fun getTimeFormat(time: String, defPreferences: SharedPreferences?): String {

        val newFormat =
            defPreferences?.getString(TIME_FORMAT_KEY, TIME_FORMAT_DEFAULT) ?: TIME_FORMAT_DEFAULT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val formatter = DateTimeFormatter.ofPattern(TIME_FORMAT_DEFAULT)
            val date = LocalDateTime.parse(time, formatter)
            val nFormatter = DateTimeFormatter.ofPattern(newFormat)

            return if (date != null) {
                date.format(nFormatter)
            } else {
                time
            }
        }

        val defFormatter = SimpleDateFormat(TIME_FORMAT_DEFAULT, Locale.getDefault())
        val defDate = defFormatter.parse(time)

        val newFormatter = SimpleDateFormat(newFormat, Locale.getDefault())
        return if (defDate != null) {
            newFormatter.format(defDate)
        } else {
            time
        }
    }


}

