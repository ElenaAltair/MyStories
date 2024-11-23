package elena.altair.note.viewmodel

import android.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import elena.altair.note.model.Ad
import elena.altair.note.model.ChapterPublic
import elena.altair.note.model.DbManager

class FirebaseViewModel : ViewModel() {
    private val dbManager = DbManager()
    val liveAdsData = MutableLiveData<ArrayList<Ad>>()
    val liveChapterData = MutableLiveData<ArrayList<ChapterPublic>>()


    fun loadAllAdsFirstPage(dialogProgres: AlertDialog) {

        dbManager.getAllAdsFirstPage(object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
                dialogProgres.dismiss()
            }
        })

    }

    fun loadAllAdsNextPage(time: String) {
        dbManager.getAllAdsNextPage(time, object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }


    fun loadAllAdsFromCatLiterFirstPage(cat: String, dialogProgres: AlertDialog) {
        dbManager.getAllAdsFromCatLiterFirstPage(cat, object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
                dialogProgres.dismiss()
            }
        })
    }

    fun loadAllAdsFromCatLiterNextPage(catLiterTime: String) {
        dbManager.getAllAdsFromCatLiterNextPage(catLiterTime, object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadAllChapters(uid: String, key: String) {
        dbManager.getAllChaptersForBook(uid, key, object : DbManager.ReadDataCallbackChapter {
            override fun readData(list: ArrayList<ChapterPublic>) {
                liveChapterData.value = list
            }
        })
    }


    // нажали на сердечко
    fun onFavClick(ad: Ad) {

        dbManager.onFavClick(ad, object : DbManager.FinishWorkListener {

            // когда изменения в firebase зафиксировались, обновляем наш список
            override fun onFinish(isDone: Boolean) {
                val updatedList = liveAdsData.value
                val pos = updatedList?.indexOf(ad)
                if (pos != -1) {
                    pos?.let {
                        val favCounter =
                            if (ad.isFav) {
                                ad.favCounter.toInt() - 1

                            } else {
                                ad.favCounter.toInt() + 1

                            }
                        updatedList[pos] = updatedList[pos].copy(
                            isFav = !ad.isFav,
                            favCounter = favCounter.toString()
                        )
                    }

                }

                liveAdsData.postValue(updatedList!!)
            }
        })

    }

    fun adViewed(ad: Ad) {
        dbManager.adViewed(ad)
    }

    fun loadMyAdsFirstPage(dialogProgres: AlertDialog) {

        dbManager.getMyAdsFirstPage(object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
                dialogProgres.dismiss()
            }
        })


    }

    fun loadMyAdsNextPage(time: String) {
        dbManager.getMyAdsNextPage(time, object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadMyAdsFirstPageCatLiterTime(cat: String, dialogProgres: AlertDialog) {
        dbManager.getMyAdsFirstPageCatLiterTime(cat, object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
                dialogProgres.dismiss()
            }
        })
    }

    fun loadMyAdsNextPageCatLiterTime(catLiterTime: String) {
        dbManager.getMyAdsNextPageCatLiterTime(catLiterTime, object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadMyFavs(dialogProgres: AlertDialog) {
        dbManager.getMyFavs(object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
                dialogProgres.dismiss()
            }

        })
    }

    fun deleteItem(ad: Ad) {
        dbManager.deleteAd(ad, object : DbManager.FinishWorkListener {
            // как только удалили объявление из базы данных,
            // удаляем его из адаптера
            override fun onFinish(isDone: Boolean) {
                val updatedList = liveAdsData.value
                updatedList?.remove(ad)
                liveAdsData.postValue(updatedList!!)
            }

        })
    }
}