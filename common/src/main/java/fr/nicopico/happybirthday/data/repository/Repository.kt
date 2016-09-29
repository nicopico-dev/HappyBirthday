package fr.nicopico.happybirthday.data.repository

import fr.nicopico.happybirthday.domain.model.Identifiable
import rx.Observable

interface Repository<T : Identifiable> {
    fun get(id: Long): Observable<T>
    fun list(filter: (T) -> Boolean = { true }): Observable<List<T>>
}