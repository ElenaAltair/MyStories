package elena.altair.note.utils.file

import android.app.Activity
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object FilePath {

    // 1) выше апи 29 получать абсолютный путь нельзя (изменили из соображений безопасности) -
    // , нужно копировать файл в кеш папку приложения.
    // 2) pdf файл нужно не только создать но и записать туда данные
    // Функция getDriveFilePath создает 2 File() и 2 stream и копирует данные по stream в файл в кеш директории
    // (если не скопировать данные, а только создать файл, на сервер придет пустой файл,
    // и еще важно копировать данные как для символьного файла, а не как для картинки, иначе тоже будет пустой файл).

    fun getDriveFilePath(uri: Uri, activity: Activity, nameFile: String): String {

        val file = File(activity.cacheDir, nameFile)
        try {
            val instream: InputStream = activity.contentResolver.openInputStream(uri)!!
            val output = FileOutputStream(file)
            val buffer = ByteArray(2048) //1024
            var size: Int
            while (instream.read(buffer).also { size = it } != -1) {
                output.write(buffer, 0, size)
            }
            instream.close()
            output.close()
        } catch (e: IOException) {
            Log.d("TAG1", "e: ${e}")
        }
        return file.absolutePath
    }
}