package fr.nicopico.happybirthday.domain.model

import android.net.Uri
import fr.nicopico.happybirthday.extensions.toBirthday
import org.threeten.bp.LocalDate

class Contact(
        override val id: Long,
        val lookupKey: String,
        val displayName: String,
        val birthday: Birthday,
        val avatarThumbnail: Uri? = null,
        val avatarFull: Uri? = null
) : Identifiable, Comparable<Contact> {

    fun canComputeAge(): Boolean = birthday.year != null

    fun getAge(reference: LocalDate = LocalDate.now()): Int? {
        if (birthday.year == null) {
            return null
        }
        else {
            val refBirthday = reference.toBirthday()
            val yearBirthday = birthday.withYear(refBirthday.year!!)
            val age: Int = refBirthday.year - birthday.year
            return when (refBirthday >= yearBirthday) {
                true -> age
                false -> age - 1
            }
        }
    }

    override fun compareTo(other: Contact): Int {
        val birthdayCompare = birthday.compareTo(other.birthday)
        return when (birthdayCompare) {
            0 -> displayName.compareTo(other.displayName)
            else -> birthdayCompare
        }
    }

    override fun toString(): String {
        return "Contact(id=$id, birthday=$birthday, displayName='$displayName', " +
                "avatarFull=$avatarFull, avatarThumbnail=$avatarThumbnail)"
    }
}

fun nextBirthdaySorter(reference: LocalDate = LocalDate.now()): (Contact, Contact) -> Int {
    val referenceBirthday = reference.toBirthday()
    return { c1, c2 ->
        val b1 = c1.birthday
        val b2 = c2.birthday

        val refVersusB1 = referenceBirthday.compareTo(b1)
        val refVersusB2 = referenceBirthday.compareTo(b2)

        if (refVersusB1 == refVersusB2) {
            c1.compareTo(c2)
        }
        else if (refVersusB1 == 0) {
            // c1 birthday is today !
            -1
        }
        else if (refVersusB2 == 0) {
            // c2 birthday is today !
            1
        }
        else {
            refVersusB1 - refVersusB2
        }
    }
}