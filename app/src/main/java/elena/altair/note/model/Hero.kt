package elena.altair.note.model

import java.io.Serializable

data class Hero(

    val id: Long? = null,
    val idLocal: Long? = null,
    val idBook: Long = 0L,
    val idBookLocal: Long = 0L,
    val desc1: String? = null,
    val desc2: String? = null,
    val desc3: String? = null,
    val desc4: String? = null,
    val desc5: String? = null,
    val desc6: String? = null,
    val desc7: String? = null,
    val desc8: String? = null,
    val desc9: String? = null,
    val desc10: String? = null,
    val desc11: String? = null,
    val desc12: String? = null,
    val desc13: String? = null,
    val desc14: String? = null,
    val desc15: String? = null,
    val desc16: String? = null,
    val desc17: String? = null,
    val desc18: String? = null,
    val desc19: String? = null,
    val desc20: String? = null,
    val desc21: String? = null,
    val number: Int = 0,
    val public: String = "0",
    val idOwner: Long = 0L,
    val loginOwner: String = "",
) : Serializable
