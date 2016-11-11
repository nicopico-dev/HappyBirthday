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

package fr.nicopico.happybirthday.data.repository

import android.Manifest.permission.READ_CONTACTS
import android.support.annotation.RequiresPermission
import fr.nicopico.happybirthday.domain.model.Birthday
import fr.nicopico.happybirthday.domain.model.Contact
import rx.Observable

interface ContactRepository {
    @RequiresPermission(READ_CONTACTS)
    fun list(
            groupId: Long? = null,
            filter: (Birthday) -> Boolean = { true },
            sorter: (Contact, Contact) -> Int = Contact::compareTo
    ): Observable<List<Contact>>
}