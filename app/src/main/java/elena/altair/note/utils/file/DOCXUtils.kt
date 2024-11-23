package elena.altair.note.utils.file

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import elena.altair.note.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

object DOCXUtils {

    suspend fun saveDocx(title: String, string: String, activity: AppCompatActivity): String {

        return withContext(Dispatchers.IO) {

            val mFileName = title + "_" + SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(System.currentTimeMillis()) + ".docx"

            var mFilePath = ""

            try {

                val xwpfDocument = XWPFDocument()
                val xwpfParagraph = xwpfDocument.createParagraph()
                val xwpfRun = xwpfParagraph.createRun()

                xwpfRun.setText(string)
                xwpfRun.fontSize = 24


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

                    val docxUri = activity.contentResolver.insert(collection, contentValues)

                    withContext(Dispatchers.IO) {

                        docxUri?.let { uri ->

                            val writeStream = activity.contentResolver.openOutputStream(uri, "w")
                            xwpfDocument.write(writeStream)

                            contentValues.clear()
                            contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 0)

                            activity.contentResolver.update(uri, contentValues, null, null)
                        }
                    }

                } else {
                    val filePath =
                        File(
                            Environment.getExternalStorageDirectory().toString() + "/download/",
                            mFileName
                        )
                    mFilePath = filePath.absolutePath

                    try {
                        if (!filePath.exists()) {
                            filePath.createNewFile()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    val fileOutputStream = FileOutputStream(filePath)
                    xwpfDocument.write(fileOutputStream)

                    if (fileOutputStream != null) {
                        fileOutputStream.flush()
                        fileOutputStream.close()
                    }

                }

                xwpfDocument.close()

                val strMessage =
                    "$mFileName\n \n${activity.resources.getString(R.string.is_save_to)}:\n \n${mFilePath}\n"
                //show file saved message with file name and path
                return@withContext strMessage

            } catch (e: Exception) {

            }
            return@withContext "PDFUtils " + activity.getString(R.string.permission_denied)
        }
    }
}