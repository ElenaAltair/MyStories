package elena.altair.note.fragments.books

import android.app.AlertDialog
import android.widget.EditText
import android.widget.TextView
import elena.altair.note.etities.ChapterEntity2
import elena.altair.note.etities.HeroEntity2
import elena.altair.note.etities.LocationEntity2
import elena.altair.note.etities.PeopleEntity2
import elena.altair.note.etities.PlotEntity2
import elena.altair.note.etities.TermEntity2
import elena.altair.note.etities.ThemeEntity2

interface DialogsAndOtherFunctions {
    fun createDialogDelete(message: String, id: Long)
    fun createDialogI(message: String)
    fun viewButtons(currentFrag: String)
    fun progressDialog(): AlertDialog
    fun textViewSetTypeface(fontFamily: String?, textView: TextView)
    fun editTextSetTypeface(fontFamily: String?, editText: EditText)
    suspend fun saveDocxChapters(
        titleBook: String,
        nameAuthor: String,
        list: List<ChapterEntity2>
    ): String

    suspend fun saveDocxHero(titleBook: String, nameAuthor: String, list: List<HeroEntity2>): String
    suspend fun saveDocxLocation(
        titleBook: String,
        nameAuthor: String,
        list: List<LocationEntity2>
    ): String

    suspend fun saveDocxPeople(
        titleBook: String,
        nameAuthor: String,
        list: List<PeopleEntity2>
    ): String

    suspend fun saveDocxTerm(titleBook: String, nameAuthor: String, list: List<TermEntity2>): String
    suspend fun saveDocx(title: String, string: String): String

    suspend fun savePdfChapters(
        titleBook: String,
        nameAuthor: String,
        list: List<ChapterEntity2>
    ): String

    suspend fun savePdfHero(titleBook: String, nameAuthor: String, list: List<HeroEntity2>): String
    suspend fun savePdfLocation(
        titleBook: String,
        nameAuthor: String,
        list: List<LocationEntity2>
    ): String

    suspend fun savePdfPeople(
        titleBook: String,
        nameAuthor: String,
        list: List<PeopleEntity2>
    ): String

    suspend fun savePdfTerm(titleBook: String, nameAuthor: String, list: List<TermEntity2>): String
    suspend fun savePdf(title: String, string: String): String

    suspend fun saveTxtChapters(
        titleBook: String,
        nameAuthor: String,
        list: List<ChapterEntity2>
    ): String

    suspend fun saveTxtHero(titleBook: String, nameAuthor: String, list: List<HeroEntity2>): String
    suspend fun saveTxtLocation(
        titleBook: String,
        nameAuthor: String,
        list: List<LocationEntity2>
    ): String

    suspend fun saveTxtPeople(
        titleBook: String,
        nameAuthor: String,
        list: List<PeopleEntity2>
    ): String

    suspend fun saveTxtTerm(titleBook: String, nameAuthor: String, list: List<TermEntity2>): String
    suspend fun saveTxt(title: String, string: String): String

    fun makeShareTextTheme(theme: ThemeEntity2, listName: String, nameA: String): String
    fun makeShareTextPlot(plot: PlotEntity2, listName: String, nameA: String): String

    fun dialogSaveTheme(message: String, new: Boolean, tempTheme: ThemeEntity2)
    fun dialogSavePlot(message: String, new: Boolean, tempPlot: PlotEntity2)
}