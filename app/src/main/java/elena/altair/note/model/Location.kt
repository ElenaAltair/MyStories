package elena.altair.note.model

import java.io.Serializable

data class Location(

    val id: Long? = null,
    val idLocal: Long? = null,
    val idBook: Long = 0L,
    val idBookLocal: Long = 0L,
    val titleLocation: String? = null,
    val geography: String? = null,
    val population: String? = null,
    val politics: String? = null,
    val economy: String? = null,
    val religion: String? = null,
    val history: String? = null,
    val feature: String? = null,
    val number: Int = 0,
    val public: String = "0",
    val idOwner: Long = 0L,
    val loginOwner: String = "",
) : Serializable
