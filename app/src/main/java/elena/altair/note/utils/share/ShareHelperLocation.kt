package elena.altair.note.utils.share

import android.content.Context
import android.content.Intent
import elena.altair.note.R
import elena.altair.note.etities.LocationEntity2

object ShareHelperLocation {
    fun shareLocation(
        location: LocationEntity2,
        listName: String,
        nameA: String,
        context: Context
    ): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plane"
        intent.apply {
            putExtra(Intent.EXTRA_TEXT, makeShareText(location, listName, nameA, context))
        }
        return intent
    }

    fun makeShareText(
        location: LocationEntity2,
        listName: String,
        nameA: String,
        context: Context
    ): String {
        val sBuilder = StringBuilder()
        sBuilder.append("${context.getString(R.string.book_title)} $listName")
        sBuilder.append("\n${context.getString(R.string.author)} $nameA")
        sBuilder.append("\n \n${context.getString(R.string.location_title)}\n")
        sBuilder.append("${location.titleLocation} ")
        sBuilder.append("\n \n${context.getString(R.string.geography)}\n")
        sBuilder.append("${location.geography} ")
        sBuilder.append("\n \n${context.getString(R.string.population)}\n")
        sBuilder.append("${location.population} ")
        sBuilder.append("\n \n${context.getString(R.string.politics)}\n")
        sBuilder.append("${location.politics} ")
        sBuilder.append("\n \n${context.getString(R.string.economy)}\n")
        sBuilder.append("${location.economy} ")
        sBuilder.append("\n \n${context.getString(R.string.religion)}\n")
        sBuilder.append("${location.religion} ")
        sBuilder.append("\n \n${context.getString(R.string.history)}\n")
        sBuilder.append("${location.history} ")
        sBuilder.append("\n \n${context.getString(R.string.feature)}\n")
        sBuilder.append("${location.feature} ")

        return sBuilder.toString()
    }
}