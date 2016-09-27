package fr.nicopico.happybirthday.data.repository

import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.domain.model.Identifiable
import rx.Observable
import rx.Single

private val noFilter: (Contact) -> Boolean = { true }

interface Repository<T : Identifiable> {
    fun get(id: Long): Single<T>
    fun list(filter: (Contact) -> Boolean = noFilter): Observable<List<Contact>>
}