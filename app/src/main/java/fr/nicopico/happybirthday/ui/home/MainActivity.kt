package fr.nicopico.happybirthday.ui.home

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import fr.nicopico.happybirthday.R
import fr.nicopico.happybirthday.data.repository.Repository
import fr.nicopico.happybirthday.domain.model.Contact
import fr.nicopico.happybirthday.inject.AppComponent
import fr.nicopico.happybirthday.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var contactRepository: Repository<Contact>
    private var subscription: rx.Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val contactAdapter = ContactAdapter(this)
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
            loadContacts()
            return true
        }
        else {
            return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        subscription?.unsubscribe()
        super.onDestroy()
    }

    override fun inject(component: AppComponent) {
        component.inject(this)
    }

    private fun loadContacts() {
        subscription = contactRepository.list()
                .subscribeOn(AndroidSchedulers.mainThread())
                .toList()
                .subscribe(object : rx.Observer<List<Contact>> {
                    override fun onNext(t: List<Contact>) {
                        (rvContacts.adapter as ContactAdapter).data = t
                    }

                    override fun onError(e: Throwable) {
                        Timber.e(e, "Unable to retrieve contact")
                    }

                    override fun onCompleted() {
                        Toast.makeText(this@MainActivity, "OK", Toast.LENGTH_SHORT).show()
                    }
                })
    }
}
