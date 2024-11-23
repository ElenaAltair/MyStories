package elena.altair.note.model

import java.io.Serializable

data class Term(

    val id: Long? = null,
    val idLocal: Long? = null,
    val idBook: Long = 0L,
    val idBookLocal: Long = 0L,
    val titleTerm: String? = null,
    val interpretationTerm: String? = null,
    val number: Int = 0,
    val public: String = "0",
    val idOwner: Long = 0L,
    val loginOwner: String = "",
) : Serializable
