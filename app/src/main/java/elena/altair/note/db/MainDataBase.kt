package elena.altair.note.db

import androidx.room.Database
import androidx.room.RoomDatabase
import elena.altair.note.etities.BookEntity4
import elena.altair.note.etities.ChapterEntity2
import elena.altair.note.etities.HeroEntity2
import elena.altair.note.etities.LocationEntity2
import elena.altair.note.etities.PeopleEntity2
import elena.altair.note.etities.PlotEntity2
import elena.altair.note.etities.TermEntity2
import elena.altair.note.etities.ThemeEntity2


@Database(
    entities = [
        BookEntity4::class,
        ChapterEntity2::class,
        PlotEntity2::class,
        ThemeEntity2::class,
        HeroEntity2::class,
        LocationEntity2::class,
        PeopleEntity2::class,
        TermEntity2::class,
    ], version = 1 //, exportSchema = true
) //, autoMigrations = [AutoMigration(from = 1, to = 2)]
abstract class MainDataBase : RoomDatabase() {


    // эта функция возвращает наш интерфейс Dao
    abstract fun getDao(): Dao

    // все функции, прописанные в "companion object"
    // можно использовать без инициализации класса
    // аналог static в Java
    /*
    companion object {

        @Volatile
        private var INSTANCE: MainDataBase? = null

        fun getDataBase(context: Context): MainDataBase {
            return INSTANCE ?: synchronized(this) {
                // если INSTANCE == null, то создаём базу данных
                val instance = Room.databaseBuilder(
                    context.applicationContext, // контекст всего приложения
                    MainDataBase::class.java, // имя класса
                    "note.db" // название базы данных
                )//.addMigrations(MIGRATION_1_5)
                .build()
                instance
            }
        }
    }*/


}