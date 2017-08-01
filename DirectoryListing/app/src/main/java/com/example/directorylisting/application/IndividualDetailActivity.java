package com.example.directorylisting.application;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.example.directorylisting.entities.Individual;
import com.google.gson.Gson;

/**
 * Created by Michael Steele on 3/22/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

public class IndividualDetailActivity extends AppCompatActivity {

    Individual individual = null;
    IndividualDetailFragment individualDetailFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_detail);

        individual = new Gson().fromJson(getIntent().getExtras().getString("individual"), Individual.class);

        IndividualDetailFragment individualDetailFragment = (IndividualDetailFragment) getSupportFragmentManager().findFragmentById(R.id.individual_detail_fragment);
        individualDetailFragment.setIndividual(individual);
    }

    @Override
    public void onBackPressed() {
        sharedBackCode();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            sharedBackCode();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void sharedBackCode() {
        finish();
    }


}
