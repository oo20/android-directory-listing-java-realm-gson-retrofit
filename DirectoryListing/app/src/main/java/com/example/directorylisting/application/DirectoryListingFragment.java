package com.example.directorylisting.application;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.directorylisting.api.WebService;
import com.example.directorylisting.api.WebServiceInterface;
import com.example.directorylisting.entities.Directory;
import com.example.directorylisting.entities.Individual;
import com.example.directorylisting.shared.AppManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Michael Steele on 3/13/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

public class DirectoryListingFragment extends android.support.v4.app.Fragment {

    public static final int REQUEST_REFRESH = 1;

    View directoryListingFragment = null;
    ListView directoryListingListView = null;

    interface DirectoryListingActionInterface {
        void refreshIndividuals();
        void addIndividual();
        void loadIndividual(Individual temp);
    };

    DirectoryListingActionInterface actionInterface = new DirectoryListingActionInterface() {
        @Override
        public void refreshIndividuals() {
            final Boolean forceRefresh = AppManager.shared.directoryListingRefreshNeeded;

            AppManager.shared.directoryListingRefreshNeeded = false;

            WebService.IndividualsInterface individualsInterface = new WebService.IndividualsInterface() {
                @Override
                public void onResponse(List<Individual> individuals) {

                    final IndividualListAdapter updatedAdapter = new IndividualListAdapter(directoryListingFragment.getContext(),
                            R.layout.layout_directory_listing_item, individuals);

                    updatedAdapter.actionInterface = actionInterface;

                    directoryListingListView.setAdapter(updatedAdapter);

                    directoryListingListView.refreshDrawableState();

                }

                @Override
                public void onFailure() {

                }
            };

            if (forceRefresh) {
                AppManager.shared.webService.getIndividuals(individualsInterface);
            } else {
                AppManager.shared.webService.fetchIndividuals(individualsInterface);
            }
        }

        @Override
        public void addIndividual() {
            Individual temp = (new Individual()).clear();
            loadIndividual(temp);
        }

        @Override
        public void loadIndividual(Individual temp) {
            Intent intent = new Intent(getContext(), IndividualDetailActivity.class);
            intent.putExtra("individual", (new Gson()).toJson(temp));
            startActivityForResult(intent, REQUEST_REFRESH);
        }

    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        directoryListingFragment = inflater.inflate(R.layout.fragment_directory_listing, container, false);

        directoryListingListView = (ListView) directoryListingFragment.findViewById(R.id.directory_listing_listview);

        AppManager manager = AppManager.shared;
        AppManager.shared.init();

        actionInterface.refreshIndividuals();

        return directoryListingFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == REQUEST_REFRESH) {
            AppManager.shared.directoryListingRefreshNeeded = true;

            actionInterface.refreshIndividuals();
        }
    }


}