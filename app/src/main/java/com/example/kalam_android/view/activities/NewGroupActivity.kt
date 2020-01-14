package com.example.kalam_android.view.activities

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.databinding.ActivityNewGroupBinding
import com.example.kalam_android.view.adapter.AdapterForContacts
import com.example.kalam_android.view.adapter.AdapterForNewGroupContact
import android.widget.PopupMenu

class NewGroupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewGroupBinding
    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_group)

        setAdapter()
        setToolbar()
    }

    private fun setAdapter() {

        binding.rvForContacts.layoutManager = LinearLayoutManager(this)
        binding.rvForContacts.adapter = AdapterForNewGroupContact()
    }

    private fun setToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        menu!!.findItem(R.id.item_more)!!.isVisible = false
        searchView = menu?.findItem(R.id.action_search)
                ?.actionView as SearchView
        searchView?.setSearchableInfo(
                searchManager
                        .getSearchableInfo(componentName)
        )
        searchView?.maxWidth = Integer.MAX_VALUE

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> return true
            android.R.id.home -> finish()

        }
        return super.onOptionsItemSelected(item)
    }


}

