package fr.nicopico.dashclock.birthday

import android.app.Application
import android.content.Context
import dagger.Component
import dagger.Module
import dagger.Provides
import fr.nicopico.happybirthday.data.repository.ContactRepository
import fr.nicopico.happybirthday.inject.DataModule

@Component(modules = arrayOf(AppModule::class, DataModule::class))
interface AppComponent {
    fun contactRepository(): ContactRepository
}

@Module
class AppModule(val app: Application) {
    @Provides
    fun provideContext(): Context = app
}