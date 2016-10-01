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
import fr.nicopico.happybirthday.data.repository.ContactRepository
import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.domain.model.nextBirthdaySorter
import rx.Subscriber
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class BirthdayService : DashClockExtension() {

    private val DEFAULT_LANG = "en"

    @Inject
    lateinit var contactRepository: ContactRepository
    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    private var daysLimit: Int = 0
    private var showQuickContact: Boolean = false
    private var disableLocalization: Boolean = false
    private var contactGroupId: String? = null

    private var needToRefreshLocalization: Boolean = false

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // TODO Inject
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

        // TODO Handle groups
        val today = Calendar.getInstance()
        contactRepository
                .list(
                        filter = { it.inDays(today) <= daysLimit },
                        sorter = nextBirthdaySorter()
                )
                .first()
                .observeOn(Schedulers.trampoline())
                .subscribe(ContactSubscriber(today))
    }

    private fun handleLocalization() {
        val config = Configuration()
        config.setToDefaults()
        if (needToRefreshLocalization || disableLocalization && DEFAULT_LANG != Locale.getDefault().language) {
            config.locale = when {
                disableLocalization -> Locale(DEFAULT_LANG)
                else -> Resources.getSystem().configuration.locale
            }

            Locale.setDefault(config.locale)
            baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        }
    }

    private fun updatePreferences() {
        daysLimit = prefs.getInt(SettingsActivity.PREF_DAYS_LIMIT_KEY, 7)
        showQuickContact = prefs.getBoolean(SettingsActivity.PREF_SHOW_QUICK_CONTACT, true)
        contactGroupId = prefs.getString(SettingsActivity.PREF_CONTACT_GROUP, SettingsActivity.NO_CONTACT_GROUP_SELECTED)

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
            val today: Calendar
    ) : Subscriber<List<Contact>>() {

        override fun onNext(contacts: List<Contact>) {
            if (contacts.isEmpty()) {
                publishUpdate(ExtensionData().visible(false))
            }
            else {
                val res = resources
                val firstContactName = contacts.first().displayName

                val collapsedTitle = "$firstContactName + ${contacts.size - 1}"
                val expandedTitle = res.getString(R.string.single_birthday_title_format, firstContactName)
                val body = StringBuilder()

                contacts.slice(1..contacts.size).forEach {
                    body.append('\n')
                            .append(it.displayName)
                            .append(", ")

                    val age = it.getAge(today)
                    if (age != null) {
                        val ageText = res.getQuantityString(R.plurals.age_format, age, age)
                        body.append(ageText).append(' ')
                    }

                    val inDays = it.birthday?.inDays(today)
                    val inDaysFormat = when(inDays) {
                        0 -> R.string.when_today_format
                        1 -> R.string.when_tomorrow_format
                        else -> R.string.when_days_format
                    }
                    body.append(res.getString(inDaysFormat, inDays))
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