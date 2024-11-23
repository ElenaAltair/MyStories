package elena.altair.note.utils.file

import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import elena.altair.note.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.openxml4j.opc.PackageAccess
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File

object ExtDocx {

    suspend fun extractDocx(
        pathBuff: String,
        editContent: EditText,
        DSQLITE_MAX_LENGTH: Int,
        activity: AppCompatActivity,
    ): String {
        return withContext(Dispatchers.IO) {
            var text = editContent.text.toString()

            try {
                val yourFile = File(pathBuff)
                val opcPackage = OPCPackage.open(yourFile, PackageAccess.READ)
                val docx = XWPFDocument(opcPackage)
                val wx = XWPFWordExtractor(docx)
                var textW = wx.text

                if (textW.length > DSQLITE_MAX_LENGTH) {
                    textW = textW.substring(0, DSQLITE_MAX_LENGTH)
                }

                val byteArray = textW.toByteArray()
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