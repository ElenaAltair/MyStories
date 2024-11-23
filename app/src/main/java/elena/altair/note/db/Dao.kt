package elena.altair.note.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import elena.altair.note.etities.BookEntity4
import elena.altair.note.etities.ChapterEntity2
import elena.altair.note.etities.HeroEntity2
import elena.altair.note.etities.LocationEntity2
import elena.altair.note.etities.PeopleEntity2
import elena.altair.note.etities.PlotEntity2
import elena.altair.note.etities.TermEntity2
import elena.altair.note.etities.ThemeEntity2
import kotlinx.coroutines.flow.Flow

// класс с помощью, которого мы будем получать доступ к базе данных, для записи и считывания
@Dao
abstract class Dao {
    // возвращается список с нашими заметками
    @Query("SELECT * FROM books WHERE login_author = :login")
    abstract fun getAllBooks(login: String): Flow<List<BookEntity4>>

    @Query("SELECT * FROM books WHERE login_author = :login and public = \"0\"")
    abstract fun getAllBooksNotPublic(login: String): Flow<List<BookEntity4>>


    @Query("SELECT * FROM books WHERE id = :idBook and uid_ad = :uidAd")
    abstract fun getBook(idBook: Long, uidAd: String?): Flow<BookEntity4>


    @Query("SELECT * FROM chapters WHERE id_book = :idBook ORDER BY number, id ") //
    abstract fun getAllChaptersById(idBook: Long): Flow<List<ChapterEntity2>>




    @Query("SELECT * FROM plots WHERE id_book = :idBook")
    abstract fun getPlot(idBook: Long): Flow<PlotEntity2>

    @Query("SELECT * FROM theme WHERE id_book = :idBook")
    abstract fun getTheme(idBook: Long): Flow<ThemeEntity2>

    @Query("SELECT * FROM heroes WHERE id_book = :idBook ORDER BY lower(desc_1)")
    abstract fun getAllHeroes(idBook: Long): Flow<List<HeroEntity2>>

    @Query("SELECT * FROM locations WHERE id_book = :idBook ORDER BY lower(title_location)")
    abstract fun getAllLocations(idBook: Long): Flow<List<LocationEntity2>>

    @Query("SELECT * FROM peoples WHERE id_book = :idBook ORDER BY lower(title_people)")
    abstract fun getAllPeoples(idBook: Long): Flow<List<PeopleEntity2>>




    @Query("SELECT * FROM term WHERE id_book = :idBook ORDER BY lower(title_term)")
    abstract fun getAllTerms(idBook: Long): Flow<List<TermEntity2>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBook(book: BookEntity4)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertChapter(chapter: ChapterEntity2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPlot(plot: PlotEntity2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTheme(theme: ThemeEntity2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHero(hero: HeroEntity2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertLocation(location: LocationEntity2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPeople(people: PeopleEntity2)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTerm(term: TermEntity2)


    @Query("DELETE FROM books WHERE id IS :id")
    abstract suspend fun deleteBook(id: Long)

    @Query("DELETE FROM chapters WHERE id IS :id")
    abstract suspend fun deleteChapter(id: Long)

    @Query("DELETE FROM plots WHERE id IS :id")
    abstract suspend fun deletePlot(id: Long)

    @Query("DELETE FROM theme WHERE id IS :id")
    abstract suspend fun deleteTheme(id: Long)

    @Query("DELETE FROM heroes WHERE id IS :id")
    abstract suspend fun deleteHero(id: Long)

    @Query("DELETE FROM locations WHERE id IS :id")
    abstract suspend fun deleteLocation(id: Long)

    @Query("DELETE FROM peoples WHERE id IS :id")
    abstract suspend fun deletePeople(id: Long)

    @Query("DELETE FROM term WHERE id IS :id")
    abstract suspend fun deleteTerm(id: Long)


    @Query("DELETE FROM chapters WHERE id_book IS :idBook")
    abstract suspend fun deleteAllChapters(idBook: Long)

    @Query("DELETE FROM heroes WHERE id_book IS :idBook")
    abstract suspend fun deleteAllHeroes(idBook: Long)

    @Query("DELETE FROM locations WHERE id_book IS :idBook")
    abstract suspend fun deleteAllLocations(idBook: Long)

    @Query("DELETE FROM peoples WHERE id_book IS :idBook")
    abstract suspend fun deleteAllPeoples(idBook: Long)

    @Query("DELETE FROM term WHERE id_book IS :idBook")
    abstract suspend fun deleteAllTerms(idBook: Long)

    @Transaction
    @Query("")
    suspend fun deleteBookWithCascade(idBook: Long) {
        deletePlot(idBook)
        deleteAllChapters(idBook)
        deleteBook(idBook)
        deleteTheme(idBook)
        deleteAllHeroes(idBook)
        deleteAllLocations(idBook)
        deleteAllPeoples(idBook)
        deleteAllTerms(idBook)
    }


    @Update
    abstract suspend fun updateBook(book: BookEntity4)

    @Update
    abstract suspend fun updateChapter(chapter: ChapterEntity2)

    @Update
    abstract suspend fun updatePlot(plot: PlotEntity2)

    @Update
    abstract suspend fun updateTheme(theme: ThemeEntity2)

    @Update
    abstract suspend fun updateHero(hero: HeroEntity2)

    @Update
    abstract suspend fun updateLocation(location: LocationEntity2)

    @Update
    abstract suspend fun updatePeople(people: PeopleEntity2)

    @Update
    abstract suspend fun updateTerm(term: TermEntity2)
}