package elena.altair.note.activities

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import elena.altair.note.db.MainDataBase

// не забыть указать этот класс в манифесте: android:name=".activiteis.MainApp"
@HiltAndroidApp
class MainApp : Application() {
    // сделаем инициализацию базы данных на уровне всего приложения
    //val database by lazy { MainDataBase.getDataBase(this) }
}