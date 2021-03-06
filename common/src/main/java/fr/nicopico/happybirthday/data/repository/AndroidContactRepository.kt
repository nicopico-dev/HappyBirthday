/*
 * Copyright 2016 Nicolas Picon <nicopico.dev@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.nicopico.happybirthday.data.repository

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.GroupMembership
import android.provider.ContactsContract.Contacts.Photo
import android.provider.ContactsContract.Data
import com.squareup.sqlbrite.BriteContentResolver
import com.squareup.sqlbrite.QueryObservable
import com.squareup.sqlbrite.SqlBrite
import fr.nicopico.happybirthday.domain.model.Birthday
import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.extensions.asUri
import fr.nicopico.happybirthday.extensions.longValue
import fr.nicopico.happybirthday.extensions.stringValue
import rx.Observable
import rx.schedulers.Schedulers
import timber.log.Timber

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

    override fun list(
            groupId: Long?,
            filter: (Birthday) -> Boolean,
            sorter: (Contact, Contact) -> Int
    ): Observable<List<Contact>> {
        return getBirthdayQuery()
                .flatMap { query: SqlBrite.Query ->
                    // Build contacts for each birthday
                    val contacts = query
                            .asRows { cursor: Cursor ->
                                ContactBirthday(
                                        contactId = cursor.longValue(Data.CONTACT_ID)!!,
                                        birthday = cursor.stringValue(Event.START_DATE)?.toBirthday()
                                )
                            }
                            .filter { it.birthday?.let(filter) ?: false }
                            .flatMap { getContactInfo(it.contactId, it.birthday!!) }

                    // Filter by groupId if provided
                    val groupFiltered = when (groupId) {
                        null -> contacts
                        else -> getContactIdsInGroup(groupId).flatMap {
                            contactIdsInGroup ->
                            contacts.filter { it.id in contactIdsInGroup }
                        }
                    }

                    // Sort the result
                    groupFiltered.toSortedList(sorter)
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
                Data.CONTACT_ID,
                false
        )
    }

    private fun getContactIdsInGroup(groupId: Long): Observable<List<Long>> {
        return contentResolver
                .createQuery(
                        Data.CONTENT_URI,
                        arrayOf(Data.CONTACT_ID),
                        "${Data.MIMETYPE} = ? and ${GroupMembership.GROUP_ROW_ID} = ?",
                        arrayOf(GroupMembership.CONTENT_ITEM_TYPE, groupId.toString()),
                        Data.CONTACT_ID,
                        false
                )
                .mapToList {
                    it.longValue(Data.CONTACT_ID)!!
                }
                .first()
    }

    private fun getContactInfo(contactId: Long, birthday: Birthday): Observable<Contact> {
        return contentResolver
                .createQuery(
                        Data.CONTENT_URI,
                        arrayOf(
                                Data.CONTACT_ID,
                                Data.LOOKUP_KEY,
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
                            lookupKey = cursor.stringValue(Data.LOOKUP_KEY)!!,
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
    if (matchResult == null) {
        Timber.e("Unable to convert %s to a birthday", this)
        return null
    }

    try {
        val (yearS, monthS, dayS) = matchResult.destructured

        val day = dayS.toInt()
        val month = monthS.toInt()
        val year: Int? = when (yearS) {
            "-" -> null
            else -> yearS.toInt()
        }

        return Birthday(year, month, day)
    }
    catch(e: Exception) {
        Timber.e(e, "Unable to convert %s to a birthday", this)
        return null
    }
}
