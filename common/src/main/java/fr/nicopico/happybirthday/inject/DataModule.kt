package fr.nicopico.happybirthday.inject

import android.content.Context
import com.squareup.sqlbrite.SqlBrite
import dagger.Module
import dagger.Provides
import fr.nicopico.happybirthday.data.repository.ContactRepository
import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.data.repository.Repository
import timber.log.Timber

@Module
class DataModule(val debug: Boolean = false) {

    @Provides
    fun provideSqlBrite(): SqlBrite = SqlBrite.create { Timber.tag("SQB").d(it) }

    @Provides
    fun provideContactRepository(context: Context, sqlBrite: SqlBrite): Repository<Contact> {
        return ContactRepository(context, sqlBrite, debug)
    }
}

