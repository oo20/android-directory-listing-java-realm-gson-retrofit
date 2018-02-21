package com.example.directorylisting.application

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView

import com.example.directorylisting.api.WebService
import com.example.directorylisting.entities.Individual
import com.example.directorylisting.shared.AppManager
import com.google.gson.Gson

/**
 * Created by Michael Steele on 3/13/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

class DirectoryListingFragment : android.support.v4.app.Fragment() {

    internal var directoryListingFragment: View? = null
    internal var directoryListingListView: ListView? = null

    internal var actionInterface: DirectoryListingActionInterface = object : DirectoryListingActionInterface {
        override fun refreshIndividuals() {
            loadIndividuals()
        }

        override fun addIndividual() {
            val temp = Individual().clear()
            loadIndividual(temp)
        }

        override fun loadIndividual(temp: Individual) {
            val intent = Intent(context, IndividualDetailActivity::class.java)
            intent.putExtra("individual", Gson().toJson(temp))
            startActivityForResult(intent, REQUEST_REFRESH)
        }

    }

    internal interface DirectoryListingActionInterface {
        fun refreshIndividuals()
        fun addIndividual()
        fun loadIndividual(temp: Individual)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        directoryListingFragment = inflater?.inflate(R.layout.fragment_directory_listing, container, false)

        directoryListingListView = directoryListingFragment?.findViewById<View>(R.id.directory_listing_listview) as ListView

        val manager = AppManager.shared

        val t = Thread(Runnable {
            AppManager.shared.init(context, object : WebService.BasicSuccessFailureInterface {
                override fun onSuccess() {
                    Log.d(DirectoryListingFragment::class.java.toString(), "Success initializing.  Refreshing individuals list.")

                    actionInterface.refreshIndividuals()
                }

                override fun onFailure() {
                    Log.d(DirectoryListingFragment::class.java.toString(), "Failed initilization.  Make sure server is running and configured correctly.")
                }
            })
        })
        t.start()

        return directoryListingFragment
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == REQUEST_REFRESH) {
            AppManager.shared.directoryListingRefreshNeeded = true

            actionInterface.refreshIndividuals()
        }
    }

    fun loadIndividuals() {
        val forceRefresh = AppManager.shared.directoryListingRefreshNeeded

        AppManager.shared.directoryListingRefreshNeeded = false

        val individualsInterface = object : WebService.IndividualsInterface {

            override fun onFailure() {
                Log.d(DirectoryListingFragment::class.java.toString(), "Failed to initialize service.")

            }

            override fun onResponse(individuals: List<Individual>) {

                val viewFragment: View = directoryListingFragment?: return
                val viewListView: ListView = directoryListingListView?: return

                val updatedAdapter = IndividualListAdapter(viewFragment.context,
                        R.layout.layout_directory_listing_item, individuals)

                updatedAdapter.actionInterface = actionInterface

                updatedAdapter.listView = directoryListingListView

                activity.runOnUiThread {
                    viewListView.adapter = updatedAdapter

                    viewListView.refreshDrawableState()
                }

            }
        }

        if (forceRefresh) {
            AppManager.shared.webService.getIndividuals(individualsInterface)
        } else {
            AppManager.shared.webService.fetchIndividuals(individualsInterface)
        }
    }

    companion object {

        val REQUEST_REFRESH = 1
    }


}