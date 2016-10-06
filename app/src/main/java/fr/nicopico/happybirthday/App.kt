package fr.nicopico.happybirthday

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import fr.nicopico.happybirthday.inject.AppComponent
import fr.nicopico.happybirthday.inject.AppModule
import fr.nicopico.happybirthday.inject.DaggerAppComponent
import fr.nicopico.happybirthday.inject.DataModule
import timber.log.Timber

class App : Application() {

    lateinit var component: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        component = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .dataModule(DataModule(debug = BuildConfig.DEBUG))
                .build()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

}
