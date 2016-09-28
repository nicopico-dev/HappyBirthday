package fr.nicopico.happybirthday.data.repository

import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.domain.model.Identifiable
import rx.Observable
import rx.Single

interface Repository<T : Identifiable> {
    fun get(id: Long): Observable<T>
    fun list(filter: (T) -> Boolean = { true }): Observable<List<T>>
}