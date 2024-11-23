package elena.altair.note.utils

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.Locale

object LiterKindHelper {

    fun getAllKindLiter(context: Context): ArrayList<String> {
        var tempArray = ArrayList<String>()
        try {
            val inputStream: InputStream = context.assets.open("kind_litr.json")
            // узнаем сколько байт нам доступно
            val size: Int = inputStream.available()
            // массив байт считываемых с файла kind_litr.json
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            // превращаем байты в строки
            val jsonFile: String = String(bytesArray)
            val jsonObject = JSONObject(jsonFile)

            val kindLiter = jsonObject.names()
            if (kindLiter != null) {
                for (n in 0 until kindLiter.length()) {
                    tempArray.add((kindLiter.getString(n)))
                }
            }

        } catch (e: IOException) {

        }
        return tempArray
    }


    fun getGenresLiter(selKindLiter: String, context: Context): ArrayList<String> {
        val tempArray = ArrayList<String>()
        try {
            val inputStream: InputStream = context.assets.open("kind_litr.json")
            // узнаем сколько байт нам доступно
            val size: Int = inputStream.available()
            // массив байт считываемых с файла kind_litr.json
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            // превращаем байты в строки
            val jsonFile: String = String(bytesArray)
            val jsonObject = JSONObject(jsonFile)

            val genresLiter = jsonObject.getJSONArray(selKindLiter)

            for (n in 0 until genresLiter.length()) {
                tempArray.add((genresLiter.getString(n)))
            }


        } catch (e: IOException) {

        }
        return tempArray
    }

    fun getGenresLiterFromAll(selKindLiter: String, context: Context): ArrayList<String> {
        val tempArray = ArrayList<String>()
        try {
            val inputStream: InputStream = context.assets.open("genres_litr.json")
            // узнаем сколько байт нам доступно
            val size: Int = inputStream.available()
            // массив байт считываемых с файла genres_litr.json
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            // превращаем байты в строки
            val jsonFile: String = String(bytesArray)
            val jsonObject = JSONObject(jsonFile)

            val genresLiter = jsonObject.getJSONArray(selKindLiter)

            for (n in 0 until genresLiter.length()) {
                tempArray.add((genresLiter.getString(n)))
            }
        } catch (e: IOException) {

        }
        return tempArray
    }


    fun getAgeCat(selKindAgeCat: String, context: Context): ArrayList<String> {
        val tempArray = ArrayList<String>()
        try {
            val inputStream: InputStream = context.assets.open("age_cat.json")
            // узнаем сколько байт нам доступно
            val size: Int = inputStream.available()
            // массив байт считываемых с файла age_cat.json
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            // превращаем байты в строки
            val jsonFile: String = String(bytesArray)
            val jsonObject = JSONObject(jsonFile)

            val genresLiter = jsonObject.getJSONArray(selKindAgeCat)

            for (n in 0 until genresLiter.length()) {
                tempArray.add((genresLiter.getString(n)))
            }
        } catch (e: IOException) {

        }
        return tempArray
    }


    fun filterListData(list: ArrayList<String>, searchText: String?): ArrayList<String> {
        val tempList = ArrayList<String>()
        tempList.clear()
        if (searchText == null) {
            tempList.add("Nothing was found")
            return tempList
        }
        for (selection: String in list) {
            if (selection.lowercase(Locale.getDefault()) //Local.ROOT
                    .startsWith(searchText.lowercase(Locale.getDefault()))
            )
                tempList.add(selection)
        }
        if (tempList.size == 0) tempList.add("Nothing was found")
        return tempList
    }
}