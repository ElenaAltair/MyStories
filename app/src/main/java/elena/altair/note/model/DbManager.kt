package elena.altair.note.model

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DbManager {
    //val db = Firebase.database.reference
    val db = Firebase.database.getReference(MAIN_NODE)
    val auth = Firebase.auth

    // Записываем в базу firebase основные сведения о книге
    fun publishAd(
        ad: Ad,
        finishListener: FinishWorkListener,
        listChaptersPublic: ArrayList<ChapterPublic>
    ) {
        val keyAd = ad.key

        if (auth.uid != null) {
            if (listChaptersPublic.isEmpty()) {

                db.child(keyAd ?: "empty")
                    .child(auth.uid!!)
                    .child(AD_NODE)
                    .setValue(ad)
                    .addOnCompleteListener { // эта функция запустится, когда данные уже запишутся в firebase
                        //if(it.isSuccessful) // сделать проверку на успешность и не успешность операции
                        val bookFilter = AdFilter(ad.time, "${ad.categoryLiter}_${ad.time}")
                        db.child(keyAd ?: "empty").child(FILTER_NODE)
                            .setValue(bookFilter).addOnCompleteListener {
                                finishListener.onFinish(it.isSuccessful)
                            }

                    }

            } else {

                db.child(keyAd ?: "empty")
                    .child(auth.uid!!)
                    .child(AD_NODE)
                    .setValue(ad)
                    .addOnCompleteListener {
                        val size = listChaptersPublic.size
                        for (i in listChaptersPublic.indices) {
                            if (i != size - 1) {
                                db.child(keyAd ?: "empty")
                                    .child(auth.uid!!)
                                    .child(AD_NODE)
                                    .child(CHAPTERS_NODE)
                                    .child(listChaptersPublic[i].idLocal.toString())
                                    .setValue(listChaptersPublic[i])
                            } else {
                                db.child(keyAd ?: "empty")
                                    .child(auth.uid!!)
                                    .child(AD_NODE)
                                    .child(CHAPTERS_NODE)
                                    .child(listChaptersPublic[i].idLocal.toString())
                                    .setValue(listChaptersPublic[i])
                                    .addOnCompleteListener { // эта функция запустится, когда данные уже запишутся в firebase
                                        //if(it.isSuccessful) // сделать проверку на успешность и не успешность операции
                                        val bookFilter =
                                            AdFilter(ad.time, "${ad.categoryLiter}_${ad.time}")
                                        db.child(keyAd ?: "empty").child(FILTER_NODE)
                                            .setValue(bookFilter).addOnCompleteListener {
                                                finishListener.onFinish(it.isSuccessful)
                                            }
                                    }
                            }
                        }
                    }
            }
        }
    }

    // подсчитываем количество просмотров
    // записываем 1 просмотр к объявлению
    fun adViewed(ad: Ad) {

        val counter = (ad.viewsCounter.toInt() + 1).toString()
        val info = InfoItemAd(counter, ad.emailsCounter)

        if (auth.uid != null)
            db.child(ad.key ?: "empty")
                .child(INFO_NODE)
                .setValue(info)
    }

    // добавить в избранное
    private fun addToFavs(ad: Ad, listener: FinishWorkListener) {
        ad.key?.let {
            auth.uid?.let { uid ->
                db.child(it).child(FAVS_NODE)
                    .child(uid)
                    .setValue(uid)
                    .addOnCompleteListener {
                        if (it.isSuccessful) listener.onFinish(true)
                    }
            }
        }
    }

    // удалить из избранного
    private fun removeFromFavs(ad: Ad, listener: FinishWorkListener) {
        ad.key?.let {
            auth.uid?.let { uid ->
                db.child(it).child(FAVS_NODE)
                    .child(uid)
                    .removeValue()
                    .addOnCompleteListener {
                        if (it.isSuccessful) listener.onFinish(true)
                    }
            }
        }
    }

    // функция определяющая, нужно добавить книгу в избранное или удалить из него
    fun onFavClick(ad: Ad, listener: FinishWorkListener) {
        if (ad.isFav) {
            removeFromFavs(ad, listener)
        } else {
            addToFavs(ad, listener)
        }
    }

    // Фильтрация по uid пользователя (найдем все опубликованные книги пользователя)
    fun getMyAdsFirstPage(readDataCallback: ReadDataCallback?) {

        val query = db.orderByChild("${auth.uid}/$AD_NODE/time")     //uidOwner").equalTo(auth.uid)
            .limitToLast(ADS_LIMIT)

        readDataFromDb(query, readDataCallback)

    }

    // Фильтрация по uid пользователя (найдем все опубликованные книги пользователя)
    fun getMyAdsNextPage(time: String, readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild("${auth.uid}/$AD_NODE/time")     //uidOwner").equalTo(auth.uid)
            .endBefore(time)
            .limitToLast(ADS_LIMIT)
        readDataFromDb(query, readDataCallback)
    }

    fun getMyAdsFirstPageCatLiterTime(catLiter: String, readDataCallback: ReadDataCallback?) {
        val query =
            db.orderByChild("${auth.uid}/$AD_NODE/catLiterTime")     //uidOwner").equalTo(auth.uid)
                .startAt(catLiter)
                .endAt(catLiter + "_\uf8ff")
                .limitToLast(ADS_LIMIT)
        readDataFromDb(query, readDataCallback)
    }

    fun getMyAdsNextPageCatLiterTime(catLiterTime: String, readDataCallback: ReadDataCallback?) {
        val query =
            db.orderByChild("${auth.uid}/$AD_NODE/catLiterTime")     //uidOwner").equalTo(auth.uid)
                .endBefore(catLiterTime)
                .limitToLast(ADS_LIMIT)
        readDataFromDb(query, readDataCallback)
    }

    // найдем все книги, добавленные пользователем в избранное
    fun getMyFavs(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild("/$FAVS_NODE/${auth.uid}").equalTo(auth.uid)
        readDataFromDb(query, readDataCallback)
    }

    fun getAllAdsFirstPage(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild("/$FILTER_NODE/time")
            .limitToLast(ADS_LIMIT)
        readDataFromDb(query, readDataCallback)
    }

    fun getAllAdsNextPage(time: String, readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild("/$FILTER_NODE/time")
            .endBefore(time)
            .limitToLast(ADS_LIMIT)
        readDataFromDb(query, readDataCallback)
    }

    fun getAllAdsFromCatLiterFirstPage(catLiter: String, readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild("/$FILTER_NODE/catLiterTime")
            .startAt(catLiter)
            .endAt(catLiter + "_\uf8ff")
            .limitToLast(ADS_LIMIT)
        readDataFromDb(query, readDataCallback)
    }

    fun getAllAdsFromCatLiterNextPage(catLiterTime: String, readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild("/$FILTER_NODE/catLiterTime")
            .endBefore(catLiterTime)
            .limitToLast(ADS_LIMIT)
        readDataFromDb(query, readDataCallback)
    }


    fun deleteAd(ad: Ad, listener: FinishWorkListener) {
        if (ad.key == null || ad.uidOwner == null) return
        val map = mapOf(
            "/$FILTER_NODE" to null,
            "/$INFO_NODE" to null,
            "/$FAVS_NODE" to null,
            "/${ad.uidOwner}" to null
        )
        db.child(ad.key)
            .updateChildren(map)
            .addOnCompleteListener {
                // диалог на случай успеха или не успеха
                if (it.isSuccessful) listener.onFinish(true)
            }
    }


    // Фильтрация по key, выбранной книги (найдем все главы, отмеченные как publik = 1, для выбранной книги)
    fun getAllChaptersForBook(
        uid: String,
        key: String,
        readDataCallbackChapter: ReadDataCallbackChapter
    ) {
        val query = db.orderByChild("${uid}/${AD_NODE}/key").equalTo(key)
        readDataFromDbChapters(key, query, readDataCallbackChapter)
    }

    // Ищем все главы, отмеченные как publik = 1, для выбранной книги
    private fun readDataFromDbChapters(
        key: String,
        query: Query,
        readDataCallbackChapter: ReadDataCallbackChapter?
    ) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val listChapterPublic = ArrayList<ChapterPublic>()

                for (item in snapshot.children) {

                    var chapter: ChapterPublic? = null
                    item.children.forEach { // ищем, в каком узле есть узел chapters
                        if (chapter == null) {
                            it.child(AD_NODE).child(CHAPTERS_NODE).children.forEach { it2 ->
                                chapter = it2.getValue(ChapterPublic::class.java)

                                if (chapter != null) {
                                    if (chapter!!.key == key && chapter!!.public == "1")
                                        listChapterPublic.add(chapter!!)
                                }
                            }
                        }
                    }
                }

                readDataCallbackChapter?.readData(listChapterPublic)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Считываем из базы данных наше объявление
    private fun readDataFromDb(query: Query, readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val adArray = ArrayList<Ad>()
                for (item in snapshot.children) {
                    var ad: Ad? = null
                    item.children.forEach { // ищем, в каком узле есть узел ad
                        if (ad == null) ad = it.child(AD_NODE).getValue(Ad::class.java)
                    }
                    val infoItem = item.child(INFO_NODE).getValue(InfoItemAd::class.java)
                    // подсчитываем количество пользователей, которые добавили нашу книгу в избранное
                    val favCounter = item.child(FAVS_NODE).childrenCount
                    // проверяем, это объявление в избранном или нет
                    val isFav = auth.uid?.let {
                        item.child(FAVS_NODE).child(it).getValue(String::class.java)
                    }
                    ad?.isFav = isFav != null

                    if (infoItem != null) ad?.viewsCounter = infoItem.viewsCounter
                    if (infoItem != null) ad?.emailsCounter = infoItem.emailsCounter
                    ad?.favCounter = favCounter.toString()
                    if (ad != null) adArray.add(ad!!)
                    //Log.d("MyLog", "Data: ${ad?.categoryAge}")

                }
                readDataCallback?.readData(adArray)
            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    interface ReadDataCallback {
        fun readData(list: ArrayList<Ad>)
    }

    interface ReadDataCallbackChapter {
        fun readData(list: ArrayList<ChapterPublic>)
    }

    // это интерфейс будет запускаться, когда данные в firebase загружены
    interface FinishWorkListener {
        fun onFinish(isDone: Boolean)
    }

    companion object {
        const val AD_NODE = "ad"
        const val INFO_NODE = "info"
        const val CHAPTERS_NODE = "chapters"
        const val MAIN_NODE = "main"
        const val FAVS_NODE = "favs"
        const val ADS_LIMIT = 3
        const val FILTER_NODE = "bookFilter"
    }
}