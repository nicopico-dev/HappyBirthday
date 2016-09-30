package fr.nicopico.happybirthday.ui.home

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import fr.nicopico.happybirthday.R
import fr.nicopico.happybirthday.data.repository.Repository
import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.extensions.ifElse
import fr.nicopico.happybirthday.inject.AppComponent
import fr.nicopico.happybirthday.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var contactRepository: Repository<Contact>
    lateinit private var contactAdapter: ContactAdapter

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
        contactRepository.list()
                .doOnSubscribe { progressBar.visibility = View.VISIBLE }
                .observeOn(AndroidSchedulers.mainThread())
                .ifElse(delay, ifTrue = {
                    delay(1, TimeUnit.SECONDS)
                })
                .subscribe(
                        {
                            contactAdapter.data = it
                            progressBar.visibility = View.GONE
                        },
                        { Timber.e(it, "Unable to retrieve contact") }
                )
    }
}
