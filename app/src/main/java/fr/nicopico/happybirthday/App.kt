/*
 * Copyright 2016 Nicolas Picon <nicopico.dev@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
