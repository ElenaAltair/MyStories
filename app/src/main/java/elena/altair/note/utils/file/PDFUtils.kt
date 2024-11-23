package elena.altair.note.utils.file

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.text.Document
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfWriter
import elena.altair.note.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object PDFUtils {

    //private val FONT = "/assets/arial.ttf"
    // объеденила шрифт arial со шрифтом эмодзи от google, чтобы pdf понимал не только русские буквы, но и эмодзи
    //private val FONT = "/assets/arial_with_emojis.ttf"
    // попробовала тоже самое сделать с times_new_roman
    private val FONT = "/assets/times_new_roman_emoji.ttf"
    private var bf: BaseFont = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
    private var font: Font = Font(bf, 30f, Font.NORMAL)

    suspend fun savePdf(title: String, string: String, activity: AppCompatActivity): String {
        var strMessage = ""
        return withContext(Dispatchers.IO) {

            val mDoc = Document()

            val mFileName = title + "_" + SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(System.currentTimeMillis()) + ".pdf"

            var mFilePath = ""



            try {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    val dirDest = File(
                        Environment.DIRECTORY_DOWNLOADS,
                        activity.getString(R.string.app_name)
                    )
                    mFilePath = dirDest.absolutePath


                    val collection =
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, mFileName)
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            "$dirDest${File.separator}"
                        )
                        put(MediaStore.Files.FileColumns.IS_PENDING, 1)
                    }

                    val pdfUri = activity.contentResolver.insert(collection, contentValues)

                    withContext(Dispatchers.IO) {

                        pdfUri?.let { uri ->

                            val writeStream = activity.contentResolver.openOutputStream(uri, "w")
                            PdfWriter.getInstance(mDoc, writeStream)

                            contentValues.clear()
                            contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 0)

                            activity.contentResolver.update(uri, contentValues, null, null)
                        }
                    }

                } else {

                    //pdf file path
                    mFilePath = Environment.getExternalStorageDirectory()
                        .toString() + "/download/" + mFileName
                    //Log.d("MyLog", "fold: ${Environment.getExternalStorageDirectory()} $mFileName $string")
                    //create instance of PdfWriter class
                    PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))
                }
                //open the document for writing
                mDoc.open()

                //get text from EditText i.e. textEt
                val mText = string

                //add author of the document (metadata)
                mDoc.addAuthor("My story")

                //add paragraph to the document
                // приходится указывать шрифт для правильного отображения кириллицы и эмодзи
                mDoc.add(Paragraph(mText, font))

                //close document
                mDoc.close()

                strMessage =
                    "$mFileName\n \n${activity.resources.getString(R.string.is_save_to)}:\n \n$mFilePath\n"

                return@withContext strMessage

            } catch (e: Exception) {

            }

            return@withContext "PDFUtils " + activity.getString(R.string.permission_denied)
        }

    }
}