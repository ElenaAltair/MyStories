package elena.altair.note.model

import java.io.Serializable

data class Plot(

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
    val public: String = "0",
    val idOwner: Long = 0L,
    val loginOwner: String = "",
) : Serializable
