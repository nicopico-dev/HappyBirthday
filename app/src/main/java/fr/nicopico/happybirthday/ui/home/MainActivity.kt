package fr.nicopico.happybirthday.ui.home

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import fr.nicopico.happybirthday.R
import fr.nicopico.happybirthday.data.repository.ContactRepository
import fr.nicopico.happybirthday.domain.model.nextBirthdaySorter
import fr.nicopico.happybirthday.extensions.ifElse
import fr.nicopico.happybirthday.extensions.toast
import fr.nicopico.happybirthday.inject.AppComponent
import fr.nicopico.happybirthday.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*
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
        subscription = contactRepository
                .list(sorter = nextBirthdaySorter())
                .ifElse(delay, ifTrue = {
                    delay(1, TimeUnit.SECONDS)
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { progressBar.visibility = View.VISIBLE }
                .subscribe(
                        {
                            progressBar.visibility = View.GONE
                            contactAdapter.data = it
                        },
                        {
                            progressBar.visibility = View.GONE
                            toast("Erreur $it")
                            Timber.e(it, "Unable to retrieve contact")
                        }
                )
    }
}
