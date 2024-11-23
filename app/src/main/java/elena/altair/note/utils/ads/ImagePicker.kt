package elena.altair.note.utils.ads

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import elena.altair.note.R
import elena.altair.note.activities.ads.EditAdsActivity
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// в этом классе получаем картинки, чтобы потом показывать в списке
object ImagePicker {
    const val MAX_IMAGE_COUNT = 3

    private fun getOptions(imageCounter: Int): Options {
        val options = Options().apply {
            count = imageCounter
            isFrontFacing = false
            mode = Mode.Picture
            path = "/pix/images"
        }
        return options
    }

    fun getMultiImages(edAct: EditAdsActivity, imageCounter: Int) {
        edAct.addPixToActivity(R.id.place__holder_ads, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    getMultiSelectedImages(edAct, result.data)
                }

                else -> {}
            }
        }
    }

    fun addImages(edAct: EditAdsActivity, imageCounter: Int, array: ArrayList<Bitmap>) {

        edAct.addPixToActivity(R.id.place__holder_ads, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {

                    val uris = result.data
                    if (uris.isNotEmpty()) {

                        edAct.openChooseImageFrag(uris as ArrayList<Uri>, array, false)
                    }
                }

                else -> {}
            }
        }
    }


    fun getSingleImage(edAct: EditAdsActivity, array: ArrayList<Bitmap>) {
        edAct.addPixToActivity(R.id.place__holder_ads, getOptions(1)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    edAct.openChooseImageFrag2()
                    singleImage(edAct, result.data[0], array)
                }

                else -> {}
            }
        }
    }


    private fun closePixFrag(edAct: EditAdsActivity) {
        val fList = edAct.supportFragmentManager.fragments
        fList.forEach {
            if (it.isVisible) edAct.supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    fun getMultiSelectedImages(edAct: EditAdsActivity, uris: List<Uri>) {
        if (uris.size > 1 && edAct.chooseImageFrag == null) {

            edAct.openChooseImageFrag(uris as ArrayList<Uri>, ArrayList<Bitmap>(), true)

        } else if (uris.size == 1 && edAct.chooseImageFrag == null) {
            CoroutineScope(Dispatchers.Main).launch {
                edAct.binding.pBarLoad.visibility = View.VISIBLE
                val bitMapArray =
                    ImageManager.imageResize(uris as ArrayList<Uri>, edAct) as ArrayList<Bitmap>
                edAct.binding.pBarLoad.visibility = View.GONE
                edAct.imageAdapter.update(bitMapArray)

                edAct.openChooseImageFrag(uris, ArrayList<Bitmap>(), true)
                //closePixFrag(edAct)
            }


        }
    }

    private fun singleImage(edAct: EditAdsActivity, uri: Uri, array: ArrayList<Bitmap>) {
        edAct.chooseImageFrag?.setSingleImage(uri, edAct.editImagePos, array, edAct)
    }
}