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

package fr.nicopico.happybirthday.ui.home

import android.Manifest.permission.READ_CONTACTS
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import fr.nicopico.happybirthday.R
import fr.nicopico.happybirthday.data.repository.ContactRepository
import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.domain.model.nextBirthdaySorter
import fr.nicopico.happybirthday.extensions.ensurePermissions
import fr.nicopico.happybirthday.extensions.ifElse
import fr.nicopico.happybirthday.extensions.toast
import fr.nicopico.happybirthday.inject.AppComponent
import fr.nicopico.happybirthday.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var contactRepository: ContactRepository
    lateinit private var contactAdapter: ContactAdapter

    private var subscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        contactAdapter = ContactAdapter(this)
        rvContacts.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = contactAdapter
        }

        loadContacts()
    }

    override fun onDestroy() {
        subscription?.unsubscribe()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_reload) {
            loadContacts(delay = true)
            return true
        }
        else {
            return super.onOptionsItemSelected(item)
        }
    }

    override fun inject(component: AppComponent) {
        component.inject(this)
    }

    private fun loadContacts(delay: Boolean = false) {
        subscription?.unsubscribe()
        val contactObservable: Observable<List<Contact>> = ensurePermissions(READ_CONTACTS) {
            contactRepository
                    .list(sorter = nextBirthdaySorter())
                    .ifElse(delay, ifTrue = {
                        delay(1, TimeUnit.SECONDS)
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { progressBar.visibility = View.VISIBLE }
        }
        subscription = contactObservable.subscribe(subscriber)
    }

    private val subscriber = object : Subscriber<List<Contact>>() {
        override fun onNext(contacts: List<Contact>?) {
            progressBar.visibility = View.GONE
            contactAdapter.data = contacts
        }

        override fun onCompleted() {
            // No-op
        }

        override fun onError(e: Throwable?) {
            progressBar.visibility = View.GONE
            toast("Error $e")
            Timber.e(e, "Unable to retrieve contact")
        }
    }
}
