package elena.altair.note.etities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "term",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity4::class,
            parentColumns = ["id"],
            childColumns = ["id_book"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )]
)
data class TermEntity2(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,

    @ColumnInfo(name = "id_book")
    var idBook: Long = 0L,

    @ColumnInfo(name = "title_term") // Название термина
    var titleTerm: String? = null,

    @ColumnInfo(name = "interpretation_term") // Толкование термина
    var interpretationTerm: String? = null,

    @ColumnInfo(name = "number") // на тот случай, если пользователю понадобится личная нумерация
    var number: Int = 0,

    @ColumnInfo(name = "public")
    var public:String = "0",

    @ColumnInfo(name = "uid_ad") // на тот случай, если пользователь решит создавать книги с разных устройств под одним аккаунтом
    var uidAd: String? = null,

) : Serializable{
    constructor():this(0L, 0L, "","",
        0, "0", null)
}
