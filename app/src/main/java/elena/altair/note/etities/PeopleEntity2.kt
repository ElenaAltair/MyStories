package elena.altair.note.etities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "peoples",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity7::class,
            parentColumns = ["id"],
            childColumns = ["id_book"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )]
)
data class PeopleEntity2(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,

    @ColumnInfo(name = "id_book")
    var idBook: Long = 0L,

    @ColumnInfo(name = "title_people") // Название народа
    var titlePeople: String? = null,

    @ColumnInfo(name = "territory_residence") // территория проживания
    var territoryResidence: String? = null,

    @ColumnInfo(name = "features_appearance") // особенности внешности, костюмы
    var featuresAppearance: String? = null,

    @ColumnInfo(name = "language") // язык
    var language: String? = null,

    @ColumnInfo(name = "religion") // вероисповедание
    var religion: String? = null,

    @ColumnInfo(name = "features") // традиции, обычаи, кухня, другие особенности
    var features: String? = null,

    @ColumnInfo(name = "art") // литература, музыка, живопись
    var art: String? = null,

    @ColumnInfo(name = "role") // роль данного народа в вашей истории
    var role: String? = null,

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
