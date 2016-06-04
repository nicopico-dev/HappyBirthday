package fr.nicopico.happybirthday.domain.repository

import fr.nicopico.happybirthday.domain.model.Identifiable
import rx.Observable
import rx.Single

interface Repository<T : Identifiable> {
    fun get(id: Long): Single<T>
    fun list(): Observable<T>
}