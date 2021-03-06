package com.example.directorylisting.application;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.directorylisting.shared.AppManager;

import io.realm.Realm;

/**
 * Created by Michael Steele on 3/22/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

public class MainActivity extends AppCompatActivity {

    DirectoryListingFragment directoryListingFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Realm.init(getApplicationContext());

        AppManager.shared.packageName = getApplicationContext().getPackageName();

        setContentView(R.layout.activity_main);

        directoryListingFragment = (DirectoryListingFragment) getSupportFragmentManager().findFragmentById(R.id.directory_listing_fragment);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * On selecting action bar icons
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_individual:
                directoryListingFragment.actionInterface.addIndividual();
                return true;
            case R.id.action_refresh:
                AppManager.shared.directoryListingRefreshNeeded = true;
                directoryListingFragment.actionInterface.refreshIndividuals();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
