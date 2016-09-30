package fr.nicopico.happybirthday.data.repository

import android.Manifest
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.Contacts.Photo
import android.provider.ContactsContract.Data
import com.squareup.sqlbrite.BriteContentResolver
import com.squareup.sqlbrite.QueryObservable
import com.squareup.sqlbrite.SqlBrite
import fr.nicopico.happybirthday.domain.model.Birthday
import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.extensions.asUri
import fr.nicopico.happybirthday.extensions.ensurePermission
import fr.nicopico.happybirthday.extensions.longValue
import fr.nicopico.happybirthday.extensions.stringValue
import rx.Observable
import rx.schedulers.Schedulers

internal class AndroidContactRepository(
        private val context: Context,
        private val sqlBrite: SqlBrite,
        debug: Boolean = false
) : ContactRepository {

    private data class ContactBirthday(val contactId: Long, val birthday: Birthday?)

    private val contentResolver: BriteContentResolver by lazy {
        sqlBrite.wrapContentProvider(context.contentResolver, Schedulers.io()).apply {
            setLoggingEnabled(debug)
        }
    }

    override fun list(filter: (Birthday) -> Boolean, sorter: (Contact, Contact) -> Int): Observable<List<Contact>> {
        return context.ensurePermission(Manifest.permission.READ_CONTACTS) {
            getBirthdayQuery()
                    .flatMap {
                        it
                                .asRows { cursor: Cursor ->
                                    ContactBirthday(
                                            contactId = cursor.longValue(Data.CONTACT_ID)!!,
                                            birthday = cursor.stringValue(Event.START_DATE)?.toBirthday()
                                    )
                                }
                                .filter { it.birthday?.let(filter) ?: false }
                                .flatMap { getContactInfo(it.contactId, it.birthday) }
                                .toSortedList(sorter)
                    }
        }
    }

    private fun getBirthdayQuery(contactId: Long? = null): QueryObservable {
        return contentResolver.createQuery(
                Data.CONTENT_URI,
                arrayOf(Data.CONTACT_ID, Event.START_DATE),
                "${Data.MIMETYPE} = ? and ${Event.TYPE} = ?".let {
                    when (contactId) {
                        null -> it
                        else -> it.plus(" ${Data.CONTACT_ID} = ?")
                    }
                },
                arrayOf(Event.CONTENT_ITEM_TYPE, Event.TYPE_BIRTHDAY.toString()).let {
                    when (contactId) {
                        null -> it
                        else -> it.plus(contactId.toString())
                    }
                },
                null,
                false
        )
    }

    private fun getContactInfo(contactId: Long, birthday: Birthday?): Observable<Contact> {
        return contentResolver
                .createQuery(
                        Data.CONTENT_URI,
                        arrayOf(
                                Data.CONTACT_ID,
                                Data.DISPLAY_NAME,
                                Photo.PHOTO_THUMBNAIL_URI,
                                Photo.PHOTO_URI
                        ),
                        "${Data.CONTACT_ID} = ?",
                        arrayOf(contactId.toString()),
                        null,
                        false
                )
                .mapToOne({ cursor: Cursor ->
                    Contact(
                            id = cursor.longValue(Data.CONTACT_ID)!!,
                            displayName = cursor.stringValue(Data.DISPLAY_NAME)!!,
                            birthday = birthday,
                            avatarThumbnail = cursor.stringValue(Photo.PHOTO_THUMBNAIL_URI)?.asUri(),
                            avatarFull = cursor.stringValue(Photo.PHOTO_URI)?.asUri()
                    )
                })
                .take(1)
    }
}

private val regexDate = Regex("(\\d{4}|-)-(\\d{2})-(\\d{2})", RegexOption.COMMENTS)

private fun String.toBirthday(): Birthday? {
    val matchResult = regexDate.matchEntire(this)
    val (yearS, monthS, dayS) = matchResult!!.destructured

    val day = dayS.toInt()
    val month = monthS.toInt()
    val year: Int? = when (yearS) {
        "-" -> null
        else -> yearS.toInt()
    }

    return Birthday(day, month, year)
}
