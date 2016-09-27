package fr.nicopico.happybirthday.domain.model

import android.net.Uri

class Contact(
        override val id: Long,
        val displayName: String,
        val birthday: Birthday? = null,
        val avatarThumbnail: Uri? = null,
        val avatarFull: Uri? = null
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

    override fun toString(): String {
        return "Contact(id=$id, birthday=$birthday, displayName='$displayName', avatarFull=$avatarFull, avatarThumbnail=$avatarThumbnail)"
    }

}