package elena.altair.note.utils.share

import android.content.Context
import android.content.Intent
import elena.altair.note.R
import elena.altair.note.etities.PeopleEntity2

object ShareHelperPeople {
    fun sharePeople(people: PeopleEntity2, listName: String, context: Context): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plane"
        intent.apply {
            putExtra(Intent.EXTRA_TEXT, makeShareText(people, listName, context))
        }
        return intent
    }

    fun makeShareText(people: PeopleEntity2, listName: String, context: Context): String {
        val sBuilder = StringBuilder()
        sBuilder.append("${context.getString(R.string.book_title)} $listName")

        sBuilder.append("\n \n${context.getString(R.string.people_title)}\n")
        sBuilder.append("${people.titlePeople} ")
        sBuilder.append("\n \n${context.getString(R.string.territoryResidence)}\n")
        sBuilder.append("${people.territoryResidence} ")
        sBuilder.append("\n \n${context.getString(R.string.features_appearance)}\n")
        sBuilder.append("${people.featuresAppearance} ")
        sBuilder.append("\n \n${context.getString(R.string.language)}\n")
        sBuilder.append("${people.language} ")
        sBuilder.append("\n \n${context.getString(R.string.religion)}\n")
        sBuilder.append("${people.religion} ")
        sBuilder.append("\n \n${context.getString(R.string.peop_features)}\n")
        sBuilder.append("${people.features} ")
        sBuilder.append("\n \n${context.getString(R.string.art)}\n")
        sBuilder.append("${people.art} ")
        sBuilder.append("\n \n${context.getString(R.string.role)}\n")
        sBuilder.append("${people.role} ")

        return sBuilder.toString()
    }
}