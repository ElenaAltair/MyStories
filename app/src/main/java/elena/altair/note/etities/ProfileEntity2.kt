package elena.altair.note.etities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "profile")
data class ProfileEntity2(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,

    @ColumnInfo(name = "login_author")
    var loginAuthor: String = "",

    @ColumnInfo(name = "name_author_1")
    var nameAuthor1: String = "",

    @ColumnInfo(name = "name_author_2")
    var nameAuthor2: String = "",

    @ColumnInfo(name = "name_author_3")
    var nameAuthor3: String = "",

    ) : Serializable {
    constructor() : this(0L, "", "", "", "")
}
