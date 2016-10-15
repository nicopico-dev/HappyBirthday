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