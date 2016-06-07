package fr.nicopico.happybirthday.inject

import android.content.Context
import dagger.Component
import fr.nicopico.happybirthday.ui.MainActivity
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, DataModule::class))
interface AppComponent {
    fun inject(activity: MainActivity)

    fun context(): Context
}