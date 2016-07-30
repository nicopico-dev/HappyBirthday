package fr.nicopico.happybirthday.data.repository

import android.Manifest
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI
import android.provider.ContactsContract.CommonDataKinds.Photo.PHOTO_URI
import android.provider.ContactsContract.Data.CONTACT_ID
import android.provider.ContactsContract.Data.DISPLAY_NAME
import com.squareup.sqlbrite.BriteContentResolver
import com.squareup.sqlbrite.SqlBrite
import com.tbruyelle.rxpermissions.RxPermissions
import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.extensions.asUri
import fr.nicopico.happybirthday.extensions.longValue
import fr.nicopico.happybirthday.extensions.stringValue
import rx.Observable
import rx.Single
import rx.schedulers.Schedulers

internal class ContactRepository(
        private val context: Context
) : Repository<Contact> {

    companion object {
        private val PROJECTION = arrayOf(
                CONTACT_ID,
                DISPLAY_NAME,
                PHOTO_THUMBNAIL_URI,
                PHOTO_URI
        )

        private val MAPPER = { cursor: Cursor ->
            Contact(
                    id = cursor.longValue(CONTACT_ID),
                    displayName = cursor.stringValue(DISPLAY_NAME),
                    avatarThumbnail = cursor.stringValue(PHOTO_THUMBNAIL_URI).asUri(),
                    avatarFull = cursor.stringValue(PHOTO_URI).asUri())
        }
    }

    private val contentResolver: BriteContentResolver by lazy {
        SqlBrite.create()
                .wrapContentProvider(context.contentResolver, Schedulers.io())
    }

    override fun get(id: Long): Single<Contact> = ensurePermission {
        contentResolver
                .createQuery(
                        ContactsContract.AUTHORITY_URI,
                        PROJECTION,
                        "$CONTACT_ID = ?",
                        arrayOf(id.toString()),
                        null,
                        false)
                .mapToOne(MAPPER)
    }.toSingle()

    override fun list(): Observable<Contact> = ensurePermission {
        contentResolver
                .createQuery(
                        ContactsContract.AUTHORITY_URI,
                        PROJECTION,
                        null,
                        null,
                        null,
                        false)
                .flatMap { it.asRows(MAPPER) }
    }

    private inline fun ensurePermission(crossinline action: () -> Observable<Contact>): Observable<Contact> {
        return RxPermissions
                .getInstance(context)
                .request(Manifest.permission.READ_CONTACTS)
                .switchMap { granted ->
                    when {
                        granted -> action()
                        else    -> Observable.empty()
                    }
                }
    }
}