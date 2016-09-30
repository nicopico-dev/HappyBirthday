package fr.nicopico.happybirthday.data.repository

import fr.nicopico.happybirthday.domain.model.Birthday
import fr.nicopico.happybirthday.domain.model.Contact
import rx.Observable

interface ContactRepository {
    fun list(filter: (Birthday) -> Boolean = { true },
             sorter: (Contact, Contact) -> Int = Contact::compareTo): Observable<List<Contact>>
}