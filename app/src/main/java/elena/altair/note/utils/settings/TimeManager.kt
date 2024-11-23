package elena.altair.note.utils.settings

import android.content.SharedPreferences
import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


object TimeManager {
    private const val DEF_TIME_FORMAT = "HH:mm:ss - dd/MM/yyyy"

    fun getCurrentTime(): String {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy")
            return current.format(formatter)
        }

        val formatter = SimpleDateFormat(DEF_TIME_FORMAT, Locale.getDefault())
        return formatter.format(Calendar.getInstance().time)
    }

    fun getTimeFormat(time: String, defPreferences: SharedPreferences?): String {

        val newFormat = defPreferences?.getString("time_format_key", DEF_TIME_FORMAT) ?: "HH:mm:ss - dd/MM/yyyy"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy")
            val date = LocalDateTime.parse(time, formatter)
            val nFormatter = DateTimeFormatter.ofPattern(newFormat)

            return if (date != null) {
                date.format(nFormatter)
            } else {
                time
            }
        }

        val defFormatter = SimpleDateFormat(DEF_TIME_FORMAT, Locale.getDefault())
        val defDate = defFormatter.parse(time)

        val newFormatter = SimpleDateFormat(newFormat, Locale.getDefault())
        return if (defDate != null) {
            newFormatter.format(defDate)
        } else {
            time
        }
    }


}

