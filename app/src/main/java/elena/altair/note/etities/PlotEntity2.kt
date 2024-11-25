package elena.altair.note.etities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "plots",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity7::class,
            parentColumns = ["id"],
            childColumns = ["id_book"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )]
)
data class PlotEntity2 (

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,

    @ColumnInfo(name = "id_book")
    var idBook: Long = 0L,

    @ColumnInfo(name = "desc_1")
    var desc1: String? = null,

    @ColumnInfo(name = "desc_2")
    var desc2: String? = null,

    @ColumnInfo(name = "desc_3")
    var desc3: String? = null,

    @ColumnInfo(name = "desc_4")
    var desc4: String? = null,

    @ColumnInfo(name = "desc_5")
    var desc5: String? = null,

    @ColumnInfo(name = "desc_6")
    var desc6: String? = null,

    @ColumnInfo(name = "desc_7")
    var desc7: String? = null,

    @ColumnInfo(name = "desc_8")
    var desc8: String? = null,

    @ColumnInfo(name = "public")
    var public:String = "0",

    @ColumnInfo(name = "uid_ad") // на тот случай, если пользователь решит создавать книги с разных устройств под одним аккаунтом
    var uidAd: String? = null,

) : Serializable{
    constructor():this(0L, 0L, "","","","","",
        "", "", "",
        "0", null)
}