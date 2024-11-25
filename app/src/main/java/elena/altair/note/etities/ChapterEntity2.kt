package elena.altair.note.etities

import androidx.room.ColumnInfo
import androidx.room.ColumnInfo.Companion.TEXT
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity7::class,
            parentColumns = ["id"],
            childColumns = ["id_book"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )]
    )
data class ChapterEntity2(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,

    @ColumnInfo(name = "id_book")
    var idBook: Long = 0L,

    @ColumnInfo(name = "title_chapters")
    var titleChapters: String? = null,

    @ColumnInfo(name = "shot_describe")
    var shotDescribe: String? = null,

    @ColumnInfo(name = "context", typeAffinity = TEXT)
    var context: String? = null,

    @ColumnInfo(name = "time")
    var time: String = "",

    @ColumnInfo(name = "number")
    var number: Int = 0,

    @ColumnInfo(name = "public_")
    var public_: String = "0",

    @ColumnInfo(name = "id_comment")
    var idComment: Long = 0L,

    @ColumnInfo(name = "column_temp")
    var columnTemp: String = "",

    @ColumnInfo(name = "uid_ad") // на тот случай, если пользователь решит создавать книги с разных устройств под одним аккаунтом
    var uidAd: String? = null,

) : Serializable{
    constructor():this(0L, 0L, "","","","",0,"0",
        0L, "", null)//
}

