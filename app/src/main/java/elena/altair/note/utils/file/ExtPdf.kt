package elena.altair.note.utils.file

import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import elena.altair.note.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object ExtPdf {
    suspend fun extractPdf(
        pathBuff: String,
        editContent: EditText,
        DSQLITE_MAX_LENGTH: Int,
        activity: AppCompatActivity,
    ): String {
        return withContext(Dispatchers.IO) {
            var text = editContent.text.toString()
            try {
                var extractedText = ""

                val pdfReader = PdfReader(pathBuff)
                val n = pdfReader.numberOfPages
                Log.d("MyLog", "n $n")

                for (i in 0 until n) {
                    if (extractedText.length < DSQLITE_MAX_LENGTH)
                        extractedText =
                            extractedText + PdfTextExtractor.getTextFromPage(pdfReader, i + 1)
                                .trim { it <= ' ' } + "\n"

                    // to extract the PDF content from the different pages
                }
                text = text + "\n" + extractedText.trim()

                if (text.length > DSQLITE_MAX_LENGTH) {
                    text = text.substring(0, DSQLITE_MAX_LENGTH - 100)
                }

                pdfReader.close()

                return@withContext text
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext text + "\n" + activity.getString(R.string.mistake)
        }
    }
}