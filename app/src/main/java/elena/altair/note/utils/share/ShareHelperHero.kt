package elena.altair.note.utils.share

import android.content.Context
import android.content.Intent
import elena.altair.note.R
import elena.altair.note.etities.HeroEntity2

object ShareHelperHero {
    fun shareHero(hero: HeroEntity2, listName: String, nameA: String, context: Context): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plane"
        intent.apply {
            putExtra(Intent.EXTRA_TEXT, makeShareText(hero, listName, nameA, context))
        }
        return intent
    }

    fun makeShareText(hero: HeroEntity2, listName: String, nameA: String, context: Context): String {
        val sBuilder = StringBuilder()
        sBuilder.append("${context.getString(R.string.book_title)} $listName")
        sBuilder.append("\n${context.getString(R.string.author)} $nameA")
        sBuilder.append("\n \n${context.getString(R.string.hero_name)}\n")
        sBuilder.append("${hero.desc1} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_2)}\n")
        sBuilder.append("${hero.desc2} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_3)}\n")
        sBuilder.append("${hero.desc3} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_4)}\n")
        sBuilder.append("${hero.desc4} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_5)}\n")
        sBuilder.append("${hero.desc5} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_6)}\n")
        sBuilder.append("${hero.desc6} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_7)}\n")
        sBuilder.append("${hero.desc7} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_8)}\n")
        sBuilder.append("${hero.desc8} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_9)}\n")
        sBuilder.append("${hero.desc9} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_10)}\n")
        sBuilder.append("${hero.desc10} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_11)}\n")
        sBuilder.append("${hero.desc11} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_12)}\n")
        sBuilder.append("${hero.desc12} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_13)}\n")
        sBuilder.append("${hero.desc13} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_14)}\n")
        sBuilder.append("${hero.desc14} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_15)}\n")
        sBuilder.append("${hero.desc15} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_16)}\n")
        sBuilder.append("${hero.desc16} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_17)}\n")
        sBuilder.append("${hero.desc17} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_18)}\n")
        sBuilder.append("${hero.desc18} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_19)}\n")
        sBuilder.append("${hero.desc19} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_20)}\n")
        sBuilder.append("${hero.desc20} ")
        sBuilder.append("\n \n${context.getString(R.string.hero_edit_21)}\n")
        sBuilder.append("${hero.desc21} ")

        return sBuilder.toString()
    }
}