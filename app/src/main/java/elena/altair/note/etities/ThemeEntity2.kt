package elena.altair.note.etities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "theme",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity4::class,
            parentColumns = ["id"],
            childColumns = ["id_book"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )]
)
data class ThemeEntity2(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,

    @ColumnInfo(name = "id_book")
    var idBook: Long = 0L,

    @ColumnInfo(name = "desc_1") // тема произведения
    var desc1: String? = null,

    @ColumnInfo(name = "desc_2") // главный конфликт
    var desc2: String? = null,

    @ColumnInfo(name = "desc_3") // побочные конфликты
    var desc3: String? = null,

    @ColumnInfo(name = "desc_4") // способы разрешения конфликта
    var desc4: String? = null,

    @ColumnInfo(name = "desc_5") // как меняется главный персонаж на протяжении истории
    var desc5: String? = null,

    @ColumnInfo(name = "desc_6") // как меняются другие персонажи на протяжении истории
    var desc6: String? = null,

    @ColumnInfo(name = "desc_7") // положительные моменты и идеи
    var desc7: String? = null,

    @ColumnInfo(name = "desc_8") // отрицательные моменты и идеи
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
