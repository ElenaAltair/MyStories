package elena.altair.note.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import elena.altair.note.db.MainDataBase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {

    @Provides
    @Singleton
    fun provideMainDb(app: Application): MainDataBase {
        return Room.databaseBuilder(
            app,
            MainDataBase::class.java,
            "note.db" // название базы данных
        ).build()

    }

}