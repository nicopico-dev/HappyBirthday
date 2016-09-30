package fr.nicopico.happybirthday.inject

import android.content.Context
import com.squareup.sqlbrite.SqlBrite
import dagger.Module
import dagger.Provides
import fr.nicopico.happybirthday.data.repository.AndroidContactRepository
import fr.nicopico.happybirthday.data.repository.ContactRepository
import timber.log.Timber

@Module
class DataModule(val debug: Boolean = false) {

    @Provides
    fun provideSqlBrite(): SqlBrite = SqlBrite.create { Timber.tag("SQB").d(it) }

    @Provides
    fun provideContactRepository(context: Context, sqlBrite: SqlBrite): ContactRepository {
        return AndroidContactRepository(context, sqlBrite, debug)
    }
}

