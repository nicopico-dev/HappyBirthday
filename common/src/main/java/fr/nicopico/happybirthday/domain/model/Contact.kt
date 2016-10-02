package fr.nicopico.happybirthday.domain.model

import android.net.Uri
import fr.nicopico.happybirthday.extensions.toBirthday
import java.util.*

class Contact(
        override val id: Long,
        val lookupKey: String,
        val displayName: String,
        val birthday: Birthday,
        val avatarThumbnail: Uri? = null,
        val avatarFull: Uri? = null
) : Identifiable, Comparable<Contact> {

    fun getAge(reference: Calendar = Calendar.getInstance()): Int? {
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
    val reference = Calendar.getInstance().toBirthday()
    return { c1, c2 ->
        val b1 = c1.birthday
        val b2 = c2.birthday

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