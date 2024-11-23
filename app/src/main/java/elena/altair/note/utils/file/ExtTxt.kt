package elena.altair.note.utils.file

import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import elena.altair.note.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ExtTxt {
    suspend fun extractTxt(
        pathBuff: String,
        editContent: EditText,
        DSQLITE_MAX_LENGTH: Int,
        activity: AppCompatActivity,
    ): String {
        return withContext(Dispatchers.IO) {
            var text = editContent.text.toString()
            try {
                val file = File(pathBuff)
                val contentBuilder = StringBuilder()
                file.reader().use { reader ->
                    reader.forEachLine { line ->
                        if (contentBuilder.length < DSQLITE_MAX_LENGTH)
                            contentBuilder.append(line).append("\n")
                    }
                }
                val byteArray = contentBuilder.toString().toByteArray()
                val content = String(byteArray, Charsets.UTF_8)

                text = text + "\n" + content

                if (text.length > DSQLITE_MAX_LENGTH) {
                    text = text.substring(0, DSQLITE_MAX_LENGTH - 100)
                }

                return@withContext text
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return@withContext text + "\n" + activity.getString(R.string.mistake)
        }
    }
}