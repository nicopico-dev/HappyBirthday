package fr.nicopico.happybirthday.inject

import android.content.Context
import dagger.Module
import dagger.Provides
import fr.nicopico.happybirthday.data.repository.ContactRepository
import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.data.repository.Repository

@Module
class DataModule {

    @Provides
    fun provideContactRepository(context: Context): Repository<Contact> = ContactRepository(context)
}

