package elena.altair.note.etities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "books")
data class BookEntity7(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,

    @ColumnInfo(name = "title_book")
    var titleBook: String? = null,

    @ColumnInfo(name = "shot_describe")
    var shotDescribe: String? = null,

    @ColumnInfo(name = "id_author")
    var idAuthor: Long = 0,

    @ColumnInfo(name = "login_author")
    var loginAuthor: String = "",

    @ColumnInfo(name = "time")
    var time: String = "",

    @ColumnInfo(name = "kind_literature")
    var kindLiterature: String = "",

    @ColumnInfo(name = "genre_literature")
    var genreLiterature: String = "",

    @ColumnInfo(name = "age_cat")
    var ageCat: String = "12+",

    @ColumnInfo(name = "public")
    var public: String = "0",

    @ColumnInfo(name = "id_comment")
    var idComment: Long = 0,

    @ColumnInfo(name = "column_temp_1")
    var columnTemp1: String = "",

    @ColumnInfo(name = "number") // на тот случай, если пользователю понадобится личная нумерация
    var number: Int = 0,

    @ColumnInfo(name = "uid_ad") // на тот случай, если пользователь решит создавать книги с разных устройств под одним аккаунтом
    var uidAd: String? = null,

    @ColumnInfo(name = "name_author")
    var nameAuthor: String = "",

    ) : Serializable {
    constructor() : this(
        0L, "", "", 0L, "", "",
        "", "", "12+", "0", 0L, "", 0, null, ""
    )
}
