package com.yono.yono_vamana.ui.transact

/** Dummy payee shown on the Transact screen. Placeholder data only. */
data class DummyContact(
    val id: String,
    val name: String,
    val subtitle: String
)

object DummyContacts {
    val contacts: List<DummyContact> = listOf(
        DummyContact("1", "Aarav Mehta", "A/C ••••6972"),
        DummyContact("2", "Priya Sharma", "A/C ••••9859"),
        DummyContact("3", "Rohan Verma", "A/C ••••4821"),
        DummyContact("4", "Ananya Iyer", "A/C ••••6304"),
        DummyContact("5", "Karan Malhotra", "A/C ••••7734")
    )

    fun find(id: String?): DummyContact = contacts.firstOrNull { it.id == id } ?: contacts.first()
}
