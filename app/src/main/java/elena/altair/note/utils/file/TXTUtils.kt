package elena.altair.note.utils.file

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import elena.altair.note.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale


object TXTUtils {
    suspend fun saveTxt(title: String, string: String, activity: AppCompatActivity): String {
        return withContext(Dispatchers.IO) {

            val mFileName = title + "_" + SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(System.currentTimeMillis()) + ".txt"
            try {
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
                                out?.write(string.toByteArray())
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
                        .use { out -> out.write(string) }


                    val strMessage =
                        "$mFileName\n \n${activity.resources.getString(R.string.is_save_to)}:\n \n$mFilePath\n"

                    return@withContext strMessage
                }
            } catch (e: Exception) {

            }
            return@withContext "TXTUtils " + activity.getString(R.string.permission_denied)
        }
    }
}
