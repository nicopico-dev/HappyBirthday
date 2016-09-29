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
import com.tbruyelle.rxpermissions.RxPermissions
import fr.nicopico.happybirthday.domain.model.Birthday
import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.extensions.asUri
import fr.nicopico.happybirthday.extensions.longValue
import fr.nicopico.happybirthday.extensions.stringValue
import rx.Observable
import rx.schedulers.Schedulers

internal class ContactRepository(
        private val context: Context,
        private val sqlBrite: SqlBrite,
        debug: Boolean = false
) : Repository<Contact> {

    private data class ContactBirthday(val contactId: Long, val birthday: Birthday?)

    private val eventMapper = { cursor: Cursor ->
        ContactBirthday(
                contactId = cursor.longValue(Data.CONTACT_ID)!!,
                birthday = cursor.stringValue(Event.START_DATE)?.toBirthday()
        )
    }

    private val contentResolver: BriteContentResolver by lazy {
        sqlBrite.wrapContentProvider(context.contentResolver, Schedulers.io()).apply {
            setLoggingEnabled(debug)
        }
    }

    override fun get(id: Long): Observable<Contact> = ensurePermission {
        getBirthdayQuery(id)
                .mapToOne(eventMapper)
                .flatMap { getContactInfo(it.contactId, it.birthday) }
    }

    override fun list(filter: (Contact) -> Boolean): Observable<List<Contact>> = ensurePermission {
        getBirthdayQuery()
                .flatMap {
                    it.asRows(eventMapper)
                            .flatMap { getContactInfo(it.contactId, it.birthday) }
                            .filter(filter)
                            .toList()
                }
    }

    private inline fun <T> ensurePermission(crossinline action: () -> Observable<T>): Observable<T> {
        return RxPermissions
                .getInstance(context)
                .request(Manifest.permission.READ_CONTACTS)
                .switchMap { granted ->
                    when {
                        granted -> action()
                        else -> Observable.empty()
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
        else -> this.toInt()
    }

    return Birthday(day, month, year)
}
