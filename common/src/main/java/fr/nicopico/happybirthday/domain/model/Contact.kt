package fr.nicopico.happybirthday.domain.model

import android.net.Uri

class Contact(
        override val id: Long,
        val displayName: String,
        val birthday: Birthday?,
        val avatar: Uri?
) : Identifiable, Comparable<Contact> {

    override fun compareTo(other: Contact): Int {
        val birthdayCompare = when {
            other.birthday != null -> birthday?.compareTo(other.birthday) ?: 1
            else                   -> -1
        }

        return when (birthdayCompare) {
            0    -> displayName.compareTo(other.displayName)
            else -> birthdayCompare
        }
    }
}