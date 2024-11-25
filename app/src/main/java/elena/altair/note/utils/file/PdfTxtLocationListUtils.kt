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
import elena.altair.note.activities.MainActivity
import elena.altair.note.etities.LocationEntity2
import elena.altair.note.utils.share.ShareHelperLocation
import elena.altair.note.utils.share.ShareHelperLocation.makeShareText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

object PdfTxtLocationListUtils {

    //private val FONT = "/assets/arial.ttf"
    // объеденила шрифт arial со шрифтом эмодзи от google, чтобы pdf понимал не только русские буквы, но и эмодзи
    //private val FONT = "/assets/arial_with_emojis.ttf"
    // попробовала тоже самое сделать с times_new_roman
    private val FONT = "/assets/times_new_roman_emoji.ttf"
    private var bf: BaseFont = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
    private var font: Font = Font(bf, 30f, Font.NORMAL)

    suspend fun savePdf(
        title: String,
        nameA: String,
        list: List<LocationEntity2>,
        activity: AppCompatActivity
    ): String {
        return withContext(Dispatchers.IO) {
            val mDoc = Document()

            var titleTemp = title
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }

            val mFileName = titleTemp + "_" + SimpleDateFormat(
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


                mDoc.addAuthor(activity.resources.getString(R.string.app_name))

                if(list.isNotEmpty()) {
                    for (i in list.indices) {
                        val string = makeShareText(
                            list[i],
                            title,
                            nameA,
                            activity as MainActivity
                        )
                        mDoc.add(Paragraph(string, font))
                    }
                } else {
                    mDoc.add(Paragraph(activity.resources.getString(R.string.list_empty), font))
                }

                //close document
                mDoc.close()

                val strMessage =
                    "$mFileName\n \n${activity.resources.getString(R.string.is_save_to)}:\n \n$mFilePath\n"

                return@withContext strMessage

            } catch (e: Exception) {

            }

            return@withContext "PDFUtils " + activity.getString(R.string.permission_denied)
        }
    }

    suspend fun saveTxt(
        title: String,
        nameA: String,
        list: List<LocationEntity2>,
        activity: AppCompatActivity
    ): String {
        return withContext(Dispatchers.IO) {

            var titleTemp = title
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }

            val mFileName = titleTemp + "_" + SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(System.currentTimeMillis()) + ".txt"
            try {

                val stringB = StringBuilder()

                if(list.isNotEmpty()) {
                    for (i in list.indices) {
                        val string = ShareHelperLocation.makeShareText(
                            list[i],
                            title,
                            nameA,
                            activity as MainActivity
                        )
                        stringB.append("$string \n\n")
                    }
                }else {
                    stringB.append(activity.resources.getString(R.string.list_empty))
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {


                    val collection =
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)


                    val dirDest = File(
                        Environment.DIRECTORY_DOWNLOADS,
                        activity.getString(R.string.app_name)
                    )
                    val mFilePath = dirDest.absolutePath


                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, mFileName)
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            "$dirDest${File.separator}"
                        )
                        put(MediaStore.Files.FileColumns.IS_PENDING, 1)


                    }


                    val txtUri = activity.contentResolver.insert(collection, contentValues)


                    withContext(Dispatchers.IO) {

                        txtUri?.let { uri ->
                            activity.contentResolver.openOutputStream(uri, "w").use { out ->
                                out?.write(stringB.toString().toByteArray())
                            }
                            contentValues.clear()
                            contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 0)

                            activity.contentResolver.update(uri, contentValues, null, null)
                        }
                    }

                    val strMessage =
                        "$mFileName\n \n${activity.resources.getString(R.string.is_save_to)}:\n \n$mFilePath\n"

                    return@withContext strMessage
                } else {


                    val mFilePath =
                        Environment.getExternalStorageDirectory().absolutePath + "/download/"


                    File("$mFilePath$mFileName").bufferedWriter()
                        .use { out -> out.write(stringB.toString()) }


                    val strMessage =
                        "$mFileName\n \n${activity.resources.getString(R.string.is_save_to)}:\n \n$mFilePath\n"

                    return@withContext strMessage
                }
            } catch (e: Exception) {

            }
            return@withContext "TXTUtils " + activity.getString(R.string.permission_denied)
        }
    }

    suspend fun saveDocx(
        title: String,
        nameA: String,
        list: List<LocationEntity2>,
        activity: AppCompatActivity
    ): String {
        return withContext(Dispatchers.IO) {

            var titleTemp = title
            if (titleTemp.length > 10) {
                titleTemp = titleTemp.substring(0, 10)
            }

            val mFileName = titleTemp + "_" + SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(System.currentTimeMillis()) + ".docx"


            var mFilePath = ""

            try {

                val stringB = StringBuilder()

                if(list.isNotEmpty()) {
                    for (i in list.indices) {
                        val string = makeShareText(
                            list[i],
                            title,
                            nameA,
                            activity as MainActivity
                        )
                        stringB.append("$string \n\n")
                    }
                }else {
                    stringB.append(activity.resources.getString(R.string.list_empty))
                }

                val xwpfDocument = XWPFDocument()
                val xwpfParagraph = xwpfDocument.createParagraph()
                val xwpfRun = xwpfParagraph.createRun()

                xwpfRun.setText(stringB.toString())
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