package elena.altair.note.etities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "locations",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity7::class,
            parentColumns = ["id"],
            childColumns = ["id_book"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )]
)
data class LocationEntity2(

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,

    @ColumnInfo(name = "id_book")
    var idBook: Long = 0L,

    @ColumnInfo(name = "title_location") // Название локации
    var titleLocation: String? = null,

    @ColumnInfo(name = "geography") // География локации (географическое положение, рельеф, природа, животный мир)
    var geography: String? = null,

    @ColumnInfo(name = "population") // Население
    var population: String? = null,

    @ColumnInfo(name = "politics") // Политика
    var politics: String? = null,

    @ColumnInfo(name = "economy") // Экономика
    var economy: String? = null,

    @ColumnInfo(name = "religion") // Религия
    var religion: String? = null,

    @ColumnInfo(name = "history") // История
    var history: String? = null,

    @ColumnInfo(name = "feature") // Чем это место примечательно для вашей истории
    var feature: String? = null,

    @ColumnInfo(name = "number") // на тот случай, если пользователю понадобится личная нумерация
    var number: Int = 0,

    @ColumnInfo(name = "public")
    var public:String = "0",

    @ColumnInfo(name = "uid_ad") // на тот случай, если пользователь решит создавать книги с разных устройств под одним аккаунтом
    var uidAd: String? = null,

) : Serializable{
    constructor():this(0L, 0L, "","","","","",
        "", "", "",
        0, "0", null)
}
