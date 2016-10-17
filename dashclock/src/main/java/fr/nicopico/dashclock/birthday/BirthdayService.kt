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

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.preference.PreferenceManager
import android.provider.ContactsContract.Contacts
import com.google.android.apps.dashclock.api.DashClockExtension
import com.google.android.apps.dashclock.api.ExtensionData
import com.jakewharton.threetenabp.AndroidThreeTen
import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.domain.model.nextBirthdaySorter
import fr.nicopico.happybirthday.extensions.today
import fr.nicopico.happybirthday.inject.DataModule
import org.threeten.bp.LocalDate
import rx.Subscriber
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.*

class BirthdayService : DashClockExtension() {

    private val DEFAULT_LANG = BuildConfig.DEFAULT_LANG

    private val contactRepository by lazy {
        DaggerAppComponent.builder()
                .appModule(AppModule(application))
                .dataModule(DataModule(BuildConfig.DEBUG))
                .build()
                .contactRepository()
    }
    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    private var daysLimit: Int = 0
    private var showQuickContact: Boolean = false
    private var disableLocalization: Boolean = false
    private var contactGroupId: Long? = null

    private var needToRefreshLocalization: Boolean = false

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(application)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun onInitialize(isReconnect: Boolean) {
        super.onInitialize(isReconnect)
        updatePreferences()
    }

    override fun onUpdateData(reason: Int) {
        if (reason == DashClockExtension.UPDATE_REASON_SETTINGS_CHANGED) {
            updatePreferences()
        }

        handleLocalization()

        val today = today()
        contactRepository
                .list(
                        filter = { it.inDays(today) <= daysLimit },
                        sorter = nextBirthdaySorter(),
                        groupId = contactGroupId
                )
                .observeOn(Schedulers.trampoline())
                .subscribe(ContactSubscriber(today))
    }

    @Suppress("DEPRECATION")
    private fun handleLocalization() {
        val config = Configuration()
        config.setToDefaults()
        if (needToRefreshLocalization || disableLocalization && DEFAULT_LANG != Locale.getDefault().language) {
            val locale = when {
                disableLocalization -> Locale(DEFAULT_LANG)
                else -> Resources.getSystem().configuration.locale
            }
            config.locale = locale
            Locale.setDefault(locale)
            baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        }
    }

    private fun updatePreferences() {
        // Note daysLimit preference is stored as a String because of the EditTextPreference
        daysLimit = prefs.getString(SettingsActivity.PREF_DAYS_LIMIT_KEY, "7").toInt()
        showQuickContact = prefs.getBoolean(SettingsActivity.PREF_SHOW_QUICK_CONTACT, true)
        val noGroupId = SettingsActivity.NO_CONTACT_GROUP_SELECTED
        contactGroupId = try {
            prefs.getString(SettingsActivity.PREF_CONTACT_GROUP, noGroupId)
                    .let {
                        if (it != noGroupId) it
                        else null
                    }
                    ?.toLong()
        }
        catch (e: NumberFormatException) {
            Timber.w(e)
            null
        }

        val previousDisableLocalizationValue = disableLocalization
        disableLocalization = prefs.getBoolean(SettingsActivity.PREF_DISABLE_LOCALIZATION, false)
        needToRefreshLocalization = previousDisableLocalizationValue != disableLocalization
    }

    private fun buildClickIntent(contacts: List<Contact>): Intent {
        val clickIntent: Intent
        val firstContact = contacts.first()

        if (showQuickContact) {
            // Open QuickContact dialog on click
            clickIntent = QuickContactProxy.buildIntent(applicationContext, firstContact.lookupKey)
        }
        else {
            clickIntent = Intent(Intent.ACTION_VIEW)
            clickIntent.data = Uri.withAppendedPath(Contacts.CONTENT_URI, firstContact.id.toString())
        }

        return clickIntent
    }

    private inner class ContactSubscriber(
            val today: LocalDate
    ) : Subscriber<List<Contact>>() {

        override fun onNext(contacts: List<Contact>) {
            if (contacts.isEmpty()) {
                publishUpdate(ExtensionData().visible(false))
            }
            else {
                val res = resources
                val firstContactName = contacts.first().displayName

                val collapsedTitle = when (contacts.size) {
                    1 -> firstContactName
                    else -> "$firstContactName + ${contacts.size - 1}"
                }
                val expandedTitle = res.getString(R.string.single_birthday_title_format, firstContactName)
                val body = StringBuilder()

                contacts.forEachIndexed { i, contact ->
                    if (i > 0) {
                        body.append(contact.displayName).append(", ")
                    }

                    val birthday = contact.birthday
                    if (contact.canComputeAge()) {
                        // Compute age on next birthday
                        val nextBirthdayCal = birthday.nextBirthdayDate(today)
                        val age = contact.getAge(nextBirthdayCal)!!
                        body.append(res.getQuantityString(R.plurals.age_format, age, age)).append(' ')
                    }

                    val inDays = birthday.inDays(today)
                    val inDaysFormat = when (inDays) {
                        0L -> R.string.when_today_format
                        1L -> R.string.when_tomorrow_format
                        else -> R.string.when_days_format
                    }
                    body.append(res.getString(inDaysFormat, inDays)).append('\n')
                }

                publishUpdate(ExtensionData()
                        .visible(true)
                        .icon(R.drawable.ic_extension_white)
                        .status(collapsedTitle)
                        .expandedTitle(expandedTitle)
                        .expandedBody(body.toString())
                        .clickIntent(buildClickIntent(contacts))
                )
            }
        }

        override fun onError(e: Throwable?) {
            Timber.e(e, "Unable to retrieve birthdays")
        }

        override fun onCompleted() {
            // no-op
        }
    }
}
