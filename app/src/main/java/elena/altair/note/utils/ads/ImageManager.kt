package elena.altair.note.utils.ads

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageManager {
    //максимальный допустимый размер картинки
    private const val MAX_IMAGE_SIZE = 500
    private const val WIDTH = 0
    private const val HEIGHT = 1

    fun getImageSize(uri: Uri, act: Activity): List<Int> {
        val inStream = act.contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inStream, null, options)
        return listOf(options.outWidth, options.outHeight)
    }

    fun chooseScaleType(im: ImageView, bitMap: Bitmap) {
        if (bitMap.width > bitMap.height) {
            im.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            im.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

    // подготавливаем размеры картинок, до которых потом будем уменьшать наши картинки с помощью библиотеки Picasso
    // уменьшаем размер картинки, если это необходимо
    suspend fun imageResize(uris: ArrayList<Uri>, act: Activity): List<Bitmap> =
        withContext(Dispatchers.IO) {

            // сохраняем в массиве новые размеры картинок (высрту и ширину)
            val tempList = ArrayList<List<Int>>()
            val bitmapList = ArrayList<Bitmap>()
            for (n in uris.indices) {

                val size = getImageSize(uris[n], act)

                // соотношение сторон
                val imageRatio = size[WIDTH].toFloat() / size[HEIGHT].toFloat()

                // проверяем ширина больше чем высота, или наоборот.
                // это необходимо знать, чтобы знать ширину или высоту уменьшать до MAX_IMAGE_SIZE
                if (imageRatio > 1) { // ширина больше высоты (горизонтальная картинка)

                    // проверим не превышает ли наша картинка MAX_IMAGE_SIZE
                    if (size[WIDTH] > MAX_IMAGE_SIZE) {

                        // если допустимые размеры превышены, то уменьшаем размеры до допустимого размера
                        tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt()))

                    } else {

                        // если допустимые размеры не превышены, то не меняем размеры картинки
                        tempList.add(listOf(size[WIDTH], size[HEIGHT]))

                    }

                } else { // если высота больше ширины (вертикальная картинка)

                    if (size[HEIGHT] > MAX_IMAGE_SIZE) {
                        tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE))
                    } else {
                        tempList.add(listOf(size[WIDTH], size[HEIGHT]))

                    }

                }

            }
            for (i in uris.indices) {

                val e = kotlin.runCatching {
                    // изменяем размеры картинок до допустимых с помощью библиотеки Picasso
                    bitmapList.add(
                        Picasso.get().load(uris[i]).resize(tempList[i][WIDTH], tempList[i][HEIGHT])
                            .get()
                    )
                }

            }

            return@withContext bitmapList
        }

    private suspend fun getBitmapFromUris(uris: List<String?>): List<Bitmap> =
        withContext(Dispatchers.IO) {

            val bitmapList = ArrayList<Bitmap>()
            for (i in uris.indices) {
                kotlin.runCatching {
                    bitmapList.add(Picasso.get().load(uris[i]).get())
                }
            }
            return@withContext bitmapList
        }


}