package fr.nicopico.happybirthday.data.repository

import android.content.Context
import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.data.repository.Repository
import rx.Observable
import rx.Single

internal class ContactRepository(context: Context) : Repository<Contact> {

    init {
        context.contentResolver.apply {
        }
    }

    override fun get(id: Long): Single<Contact> {
        throw UnsupportedOperationException()
    }

    override fun list(): Observable<Contact> {
        throw UnsupportedOperationException()
    }
}