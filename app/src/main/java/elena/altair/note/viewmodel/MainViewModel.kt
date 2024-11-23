package elena.altair.note.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import elena.altair.note.db.MainDataBase
import elena.altair.note.etities.BookEntity4
import elena.altair.note.etities.ChapterEntity2
import elena.altair.note.etities.HeroEntity2
import elena.altair.note.etities.LocationEntity2
import elena.altair.note.etities.PeopleEntity2
import elena.altair.note.etities.PlotEntity2
import elena.altair.note.etities.TermEntity2
import elena.altair.note.etities.ThemeEntity2
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(database: MainDataBase) : ViewModel() {
    private val dao = database.getDao()

    val bookTr = MutableLiveData<BookEntity4>()

    // считаем список книг из базы данных
    //val allBooks: LiveData<List<BookEntity>> = dao.getAllBooks().asLiveData()

    fun allBooks(login: String): LiveData<List<BookEntity4>> {
        return dao.getAllBooks(login).asLiveData()
    }

    // найдем все неопубликованные книги
    fun allBooksNotPublic(login: String): LiveData<List<BookEntity4>> {
        return dao.getAllBooksNotPublic(login).asLiveData()
    }

    fun getBook(idBook: Long, uidAd: String?): LiveData<BookEntity4> {
        return dao.getBook(idBook, uidAd).asLiveData()
    }

    fun allChaptersById(idBook: Long): LiveData<List<ChapterEntity2>> {
        return dao.getAllChaptersById(idBook).asLiveData()
    }




    // считываем список героев для выбранной книги
    fun allHeroes(idBook: Long): LiveData<List<HeroEntity2>> {
        return dao.getAllHeroes(idBook).asLiveData()
    }

    // считываем список локаций для выбранной книги
    fun allLocations(idBook: Long): LiveData<List<LocationEntity2>> {
        return dao.getAllLocations(idBook).asLiveData()
    }

    // считываем список народов для выбранной книги
    fun allPeoples(idBook: Long): LiveData<List<PeopleEntity2>> {
        return dao.getAllPeoples(idBook).asLiveData()
    }

    // считываем список терминов для выбранной книги
    fun allTerms(idBook: Long): LiveData<List<TermEntity2>> {
        return dao.getAllTerms(idBook).asLiveData()
    }

    // считываем сюжет для данной книги
    fun getPlot(idBook: Long): LiveData<PlotEntity2> {
        return dao.getPlot(idBook).asLiveData()
    }

    // считываем тему для данной книги
    fun getTheme(idBook: Long): LiveData<ThemeEntity2> {
        return dao.getTheme(idBook).asLiveData()
    }



    // добавление новой книги в базу данных
    fun insertBook(book: BookEntity4) = viewModelScope.launch {

        dao.insertBook(
            BookEntity4(
                book.id,
                book.titleBook,
                book.shotDescribe,
                book.idAuthor,
                book.loginAuthor,
                book.time,
                book.kindLiterature,
                book.genreLiterature,
                book.ageCat,
                book.public,
                book.idComment,
                book.columnTemp1,
                book.number,
                book.uidAd,
            )
        )

    }

    // добавление новой главы в базу данных
    fun insertChapter(chapter: ChapterEntity2) = viewModelScope.launch {

        dao.insertChapter(
            ChapterEntity2(
                chapter.id,
                chapter.idBook,
                chapter.titleChapters,
                chapter.shotDescribe,
                chapter.context,
                chapter.time,
                chapter.number,
                chapter.public_,
                chapter.idComment,
                chapter.columnTemp,
                chapter.uidAd,
            )
        )

    }

    // добавление сюжета в базу данных
    fun insertPlot(plot: PlotEntity2) = viewModelScope.launch {
        dao.insertPlot(
            PlotEntity2(
                plot.id,
                plot.idBook,
                plot.desc1,
                plot.desc2,
                plot.desc3,
                plot.desc4,
                plot.desc5,
                plot.desc6,
                plot.desc7,
                plot.desc8,
                plot.public,
                plot.uidAd,
            )
        )
    }

    // добавление темы в базу данных
    fun insertTheme(theme: ThemeEntity2) = viewModelScope.launch {
        dao.insertTheme(
            ThemeEntity2(
                theme.id,
                theme.idBook,
                theme.desc1,
                theme.desc2,
                theme.desc3,
                theme.desc4,
                theme.desc5,
                theme.desc6,
                theme.desc7,
                theme.desc8,
                theme.public,
                theme.uidAd,
            )
        )
    }

    // добавление новой локации в базу данных
    fun insertLocation(location: LocationEntity2) = viewModelScope.launch {

        dao.insertLocation(
            LocationEntity2(
                location.id,
                location.idBook,
                location.titleLocation,
                location.geography,
                location.population,
                location.politics,
                location.economy,
                location.religion,
                location.history,
                location.feature,
                location.number,
                location.public,
                location.uidAd,
            )
        )
    }

    // добавление нового народа в базу данных
    fun insertPeoples(people: PeopleEntity2) = viewModelScope.launch {

        dao.insertPeople(
            PeopleEntity2(
                people.id,
                people.idBook,
                people.titlePeople,
                people.territoryResidence,
                people.featuresAppearance,
                people.language,
                people.religion,
                people.features,
                people.art,
                people.role,
                people.number,
                people.public,
                people.uidAd,
            )
        )
    }

    // добавление нового термина в базу данных
    fun insertTerm(term: TermEntity2) = viewModelScope.launch {

        dao.insertTerm(
            TermEntity2(
                term.id,
                term.idBook,
                term.titleTerm,
                term.interpretationTerm,
                term.number,
                term.public,
                term.uidAd,
            )
        )
    }

    // добавление героя в базу данных
    fun insertHero(hero: HeroEntity2) = viewModelScope.launch {
        dao.insertHero(
            HeroEntity2(
                hero.id, hero.idBook,
                hero.desc1, hero.desc2, hero.desc3, hero.desc4, hero.desc5,
                hero.desc6, hero.desc7, hero.desc8, hero.desc9, hero.desc10,
                hero.desc11, hero.desc12, hero.desc13, hero.desc14, hero.desc15,
                hero.desc16, hero.desc17, hero.desc18, hero.desc19, hero.desc20,
                hero.desc21,
                hero.number, hero.public,
                hero.uidAd,
            )
        )
    }

    // обновление заголовка книги в базе данных
    fun updateBook(book: BookEntity4) = viewModelScope.launch {
        dao.updateBook(book)
    }

    // обновление главы в базе данных
    fun updateChapter(chapter: ChapterEntity2) = viewModelScope.launch {
        dao.updateChapter(chapter)
    }

    // обновление героя в базе данных
    fun updateHero(hero: HeroEntity2) = viewModelScope.launch {
        dao.updateHero(hero)
    }

    // обновление локации в базе данных
    fun updateLocation(location: LocationEntity2) = viewModelScope.launch {
        dao.updateLocation(location)
    }

    // обновление народа в базе данных
    fun updatePeople(people: PeopleEntity2) = viewModelScope.launch {
        dao.updatePeople(people)
    }

    // обновление термина в базе данных
    fun updateTerm(term: TermEntity2) = viewModelScope.launch {
        dao.updateTerm(term)
    }


    // удаление книги из базы данных
    fun deleteBook(id: Long) = viewModelScope.launch {
        dao.deleteBookWithCascade(id)
    }

    // удаление главы из базы данных
    fun deleteChapter(id: Long) = viewModelScope.launch {
        dao.deleteChapter(id)
    }

    // удаление героя из базы данных
    fun deleteHero(id: Long) = viewModelScope.launch {
        dao.deleteHero(id)
    }

    // удаление локации из базы данных
    fun deleteLocation(id: Long) = viewModelScope.launch {
        dao.deleteLocation(id)
    }

    // удаление народа из базы данных
    fun deletePeople(id: Long) = viewModelScope.launch {
        dao.deletePeople(id)
    }

    // удаление термина из базы данных
    fun deleteTerm(id: Long) = viewModelScope.launch {
        dao.deleteTerm(id)
    }

    companion object {
        const val BOOK_LIMIT = 20
    }

    // этот класс мы будем запускать, чтобы он инициализировал класс MainViewModel
    class MainViewModalFactory(val database: MainDataBase) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(database) as T
            }
            throw IllegalArgumentException("Unknown ViewModelClass")
        }
    }
}