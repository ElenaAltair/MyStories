package elena.altair.note.model

import java.io.Serializable


data class Ad(
    val key: String? = null,
    val tel: String? = null,
    val email: String? = null,
    val categoryLiter: String? = null,
    val categoryAge: String? = null,
    val titleBook: String? = null,
    val description: String? = null,

    val mainImage: String = "empty",
    val image2: String = "empty",
    val image3: String = "empty",

    val loginOwner: String? = null,
    val uidOwner: String? = null,

    val time: String = "0",
    val catLiterTime: String? = null,
    val idBookLocal: String? = null,

    var favCounter: String = "0",
    var isFav: Boolean = false,
    var viewsCounter: String = "0",
    var emailsCounter: String = "0",

    //var callsCounter: String = "0",
) : Serializable