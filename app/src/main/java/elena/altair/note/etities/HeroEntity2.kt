package elena.altair.note.etities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "heroes",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity4::class,
            parentColumns = ["id"],
            childColumns = ["id_book"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )]
)
data class HeroEntity2(

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,

    @ColumnInfo(name = "id_book")
    var idBook: Long = 0L,

    @ColumnInfo(name = "desc_1") // Имя персонажа, прозвище (если есть)
    var desc1: String? = null,

    @ColumnInfo(name = "desc_2") // Пол, возраст
    var desc2: String? = null,

    @ColumnInfo(name = "desc_3") // Раса, народность, вид
    var desc3: String? = null,

    @ColumnInfo(name = "desc_4") // Опишите лицо персонажа: цвет глаз, форма носа и губ, скулы, брови и т.п.
    var desc4: String? = null,

    @ColumnInfo(name = "desc_5") // Цвет кожи
    var desc5: String? = null,

    @ColumnInfo(name = "desc_6") // Прическа, цвет волос
    var desc6: String? = null,

    @ColumnInfo(name = "desc_7") // Телосложение, рост, вес (и в связи с этим впечатление окружающих о персонаже)
    var desc7: String? = null,

    @ColumnInfo(name = "desc_8") // Особенности одежды, аксессуары (и в связи с этим впечатление окружающих о персонаже)
    var desc8: String? = null,

    @ColumnInfo(name = "desc_9") // Привлекательные и отталкивающие стороны персонажа, слабые и сильные стороны персонажа, привычки, хобби, комплексы
    var desc9: String? = null,

    @ColumnInfo(name = "desc_10") // Манера речи (и в связи с этим впечатление окружающих о персонаже)
    var desc10: String? = null,

    @ColumnInfo(name = "desc_11") // Поведение персонажа в семье и в обществе
    var desc11: String? = null,

    @ColumnInfo(name = "desc_12") // Воспитание, образование, профессия
    var desc12: String? = null,

    @ColumnInfo(name = "desc_13") // Испытания выпавшие на долю персонажа
    var desc13: String? = null,

    @ColumnInfo(name = "desc_14") // Мечты и идеалы персонажа, цели и мотивации
    var desc14: String? = null,

    @ColumnInfo(name = "desc_15") // Как персонаж появляется в истории, его роль в истории, как персонаж выходит из истории
    var desc15: String? = null,

    @ColumnInfo(name = "desc_16") // Как персонаж меняется на протяжении истории (личностный рост или падение)
    var desc16: String? = null,

    @ColumnInfo(name = "desc_17") // Семья персонажа, взаимоотношения внутри семьи, как проводят время
    var desc17: String? = null,

    @ColumnInfo(name = "desc_18") // Друзья персонажа, взаимоотношения, какое влияние оказывают друг на друга, как проводят время
    var desc18: String? = null,

    @ColumnInfo(name = "desc_19") // Враги персонажа, взаимоотношения, какое влияние оказывают друг на друга, как проводят время
    var desc19: String? = null,

    @ColumnInfo(name = "desc_20") // Романтические отношения
    var desc20: String? = null,

    @ColumnInfo(name = "desc_21") // Страхи персонажа
    var desc21: String? = null,

    @ColumnInfo(name = "number") // на тот случай, если пользователю понадобится личная нумерация
    var number: Int = 0,

    @ColumnInfo(name = "public")
    var public:String = "0",

    @ColumnInfo(name = "uid_ad") // на тот случай, если пользователь решит создавать книги с разных устройств под одним аккаунтом
    var uidAd: String? = null,

) : Serializable{
    constructor():this(0L, 0L, "","","","","",
        "", "", "","","",
        "", "", "","","",
        "", "", "","","", "",
        0, "0", null)
}

