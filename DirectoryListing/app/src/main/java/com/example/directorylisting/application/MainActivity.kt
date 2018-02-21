package com.example.directorylisting.application

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.example.directorylisting.shared.AppManager
import io.realm.Realm

/**
 * Created by Michael Steele on 3/22/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

class MainActivity : AppCompatActivity() {

    internal var directoryListingFragment: DirectoryListingFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Realm.init(applicationContext)

        AppManager.packageName = applicationContext.packageName

        setContentView(R.layout.activity_main)

        directoryListingFragment = supportFragmentManager.findFragmentById(R.id.directory_listing_fragment) as DirectoryListingFragment

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * On selecting action bar icons
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_individual -> {
                directoryListingFragment?.actionInterface?.addIndividual()
                return true
            }
            R.id.action_refresh -> {
                AppManager.shared.directoryListingRefreshNeeded = true
                directoryListingFragment?.actionInterface?.refreshIndividuals()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}
