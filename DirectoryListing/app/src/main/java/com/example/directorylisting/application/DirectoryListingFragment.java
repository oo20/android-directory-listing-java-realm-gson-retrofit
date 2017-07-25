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
import com.example.directorylisting.entities.Directory;
import com.example.directorylisting.entities.Individual;
import com.example.directorylisting.shared.AppManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        directoryListingFragment = inflater.inflate(R.layout.fragment_directory_listing, container, false);

        directoryListingListView = (ListView) directoryListingFragment.findViewById(R.id.directory_listing_listview);

        AppManager manager = AppManager.shared;
        AppManager.shared.init();

        refreshIndividuals();

        return directoryListingFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == REQUEST_REFRESH) {
            refreshIndividuals();
        }
    }

    public void refreshIndividuals() {
        final Boolean forceRefresh = AppManager.shared.directoryListingRefreshNeeded;

        AppManager.shared.directoryListingRefreshNeeded = false;

        AppManager.shared.webService.getIndividuals().enqueue(new Callback<Directory>() {
            @Override
            public void onResponse(Call<Directory> call, Response<Directory> response) {
                Log.d(WebService.class.toString(), "Response:" + response.toString());

                Directory directory = response.body();

                final List<Individual> individuals = new ArrayList<Individual>();

                for (Individual individual : directory.individuals) {
                    individuals.add(individual);
                }

                final IndividualListAdapter updatedAdapter = new IndividualListAdapter(directoryListingFragment.getContext(),
                        R.layout.layout_directory_listing_item, individuals);

                directoryListingListView.setAdapter(updatedAdapter);

                directoryListingListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Individual item = updatedAdapter.items.get(position);

                        Log.d(WebService.class.toString(), "Selected: " + item.getId());

                        loadIndividual(item);
                    }
                });

                directoryListingListView.refreshDrawableState();
            }

            @Override
            public void onFailure(Call<Directory> call, Throwable t) {
                Log.d(WebService.class.toString(), "Failure");
            }
        });
    }

    public void addIndividual() {
        Individual temp = (new Individual()).clear();
        loadIndividual(temp);
    }

    private void loadIndividual(Individual temp) {
        Intent intent = new Intent(getContext(), IndividualDetailActivity.class);
        intent.putExtra("individual", (new Gson()).toJson(temp));
        startActivityForResult(intent, REQUEST_REFRESH);
    }

}
