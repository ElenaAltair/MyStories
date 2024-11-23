package elena.altair.note.model

import java.io.Serializable

data class People(

    val id: Long? = null,
    val idLocal: Long? = null,
    val idBook: Long = 0L,
    val idBookLocal: Long = 0L,
    val titlePeople: String? = null,
    val territoryResidence: String? = null,
    val featuresAppearance: String? = null,
    val language: String? = null,
    val religion: String? = null,
    val features: String? = null,
    val art: String? = null,
    val role: String? = null,
    val number: Int = 0,
    val public: String = "0",
    val idOwner: Long = 0L,
    val loginOwner: String = "",
) : Serializable
