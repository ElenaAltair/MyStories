package elena.altair.note.model

import java.io.Serializable

data class ChapterPublic(

    val idLocal: String? = null,
    val idBookLocal: String? = null,
    val titleChapters: String? = null,
    val shotDescribe: String? = null,
    val context: String? = null,
    val time: String? = null,
    val number: String? = null,
    val public: String = "0",
    val key: String? = null,
    val loginOwner: String? = null,
    val uidOwner: String? = null,


    var favCounter: String = "0",
    var isFav: Boolean = false,
    var viewsCounter: String = "0",
) : Serializable
