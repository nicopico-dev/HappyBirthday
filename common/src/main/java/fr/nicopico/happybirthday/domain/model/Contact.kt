package fr.nicopico.happybirthday.domain.model

import android.net.Uri
import java.util.*

class Contact(
        override val id: Long,
        val displayName: String,
        val birthday: Birthday? = null,
        val avatarThumbnail: Uri? = null,
        val avatarFull: Uri? = null
) : Identifiable, Comparable<Contact> {

    override fun compareTo(other: Contact): Int {
        val birthdayCompare = when (other.birthday) {
            null -> -1
            else -> birthday?.compareTo(other.birthday) ?: 1
        }

        return when (birthdayCompare) {
            0    -> displayName.compareTo(other.displayName)
            else -> birthdayCompare
        }
    }

    override fun toString(): String {
        return "Contact(id=$id, birthday=$birthday, displayName='$displayName', " +
                "avatarFull=$avatarFull, avatarThumbnail=$avatarThumbnail)"
    }
}

fun nextBirthdaySorter(): (Contact, Contact) -> Int {
    val cal = GregorianCalendar.getInstance()
    val reference = Birthday(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1,
            day = cal.get(Calendar.DAY_OF_MONTH)
    )
    return { c1, c2 ->
        val b1 = c1.birthday
        val b2 = c2.birthday

        if (b1 == null || b2 == null) {
            c1.compareTo(c2)
        }
        else {
            val refVersusB1 = reference.compareTo(b1)
            val refVersusB2 = reference.compareTo(b2)
            if (refVersusB1 == refVersusB2) {
                c1.compareTo(c2)
            }
            else {
                refVersusB1 - refVersusB2
            }
        }
    }
}