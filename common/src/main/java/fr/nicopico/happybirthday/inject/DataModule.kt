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

