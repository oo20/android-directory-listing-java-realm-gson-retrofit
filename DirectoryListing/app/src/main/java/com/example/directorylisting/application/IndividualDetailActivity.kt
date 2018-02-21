package com.example.directorylisting.application

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import com.example.directorylisting.entities.Individual
import com.google.gson.Gson

/**
 * Created by Michael Steele on 3/22/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

class IndividualDetailActivity : AppCompatActivity() {

    internal var individual: Individual? = null
    internal var individualDetailFragment: IndividualDetailFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_detail)

        val individualDetailFragment = supportFragmentManager.findFragmentById(R.id.individual_detail_fragment) as IndividualDetailFragment

        val extras = intent.extras?: return

        val tempIndividual = Gson().fromJson(extras.getString("individual"), Individual::class.java)?: return
        individual = tempIndividual

        individualDetailFragment.setIndividual(tempIndividual)
    }

    override fun onBackPressed() {
        sharedBackCode()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            sharedBackCode()
        }
        return super.onKeyDown(keyCode, event)
    }

    fun sharedBackCode() {
        finish()
    }


}
