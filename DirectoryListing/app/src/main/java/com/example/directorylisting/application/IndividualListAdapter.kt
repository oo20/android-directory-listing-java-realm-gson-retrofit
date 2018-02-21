package com.example.directorylisting.application

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.example.directorylisting.api.WebService
import com.example.directorylisting.entities.ImageEngine
import com.example.directorylisting.entities.Individual
import com.example.directorylisting.shared.AppManager
import com.pkmmte.view.CircularImageView
import java.util.*

/**
 * Created by Michael Steele on 7/13/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

class IndividualListAdapter(context: Context, textViewResourceId: Int,
                            objects: List<Individual>) : ArrayAdapter<Individual>(context, textViewResourceId, objects) {

    internal var items: List<Individual> = ArrayList()
    internal var imageEngines = HashMap<Int, ImageEngine>()
    internal var listView: ListView? = null


    internal var dpImage = this.context.resources.getDimension(R.dimen.directory_listing_image_size).toInt()

    internal var actionInterface: DirectoryListingFragment.DirectoryListingActionInterface? = null

    internal class ViewHolder {
        var imageView: ImageView? = null
        var deleteButton: Button? = null
        var frameLayout: FrameLayout? = null
        var swipeRevealLayout: SwipeRevealLayout? = null
    }

    init {
        items = objects
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        val item = getItem(position)

        val holder: ViewHolder

        //if (convertView == null) { // TODO: Fix recycler with glide.

        convertView = LayoutInflater.from(context).inflate(R.layout.layout_directory_listing_item, parent, false)


        holder = ViewHolder()

        holder.imageView = convertView.findViewById<View>(R.id.profile_image) as ImageView

        holder.deleteButton = convertView.findViewById<View>(R.id.delete_button) as Button

        holder.frameLayout = convertView.findViewById<View>(R.id.directory_listing_list_item) as FrameLayout

        holder.swipeRevealLayout = convertView.findViewById<View>(R.id.directory_listing_swipe_layout) as SwipeRevealLayout

        convertView.tag = holder
        /*} else {
            holder = (ViewHolder) convertView.getTag();
        }*/


        /*
        //Glide 4
        Glide.with(holder.imageView.getContext())
                .clear(holder.imageView);
        */

        holder.imageView?.setImageDrawable(null)
        holder.imageView?.tag = item?.id

        if (item.profilePicture.isEmpty()) {

            holder.imageView?.setImageResource(R.drawable.missing)
            holder.imageView?.rotation = 0f

        } else {

            holder.imageView?.setImageResource(R.drawable.spinner)
            val anim = ObjectAnimator.ofFloat(holder.imageView,
                    "rotation", 0f, 360f)
            anim.duration = 500
            anim.repeatCount = ObjectAnimator.INFINITE
            anim.start()

            if (imageEngines[position] == null) {
                imageEngines.put(position, ImageEngine())
            }

            val imageEngine = imageEngines[position]

            imageEngines[position]?.loadImage(context, item.id, AppManager.shared.getCacheKey(item), item.prettyProfilePicture, dpImage, object : ImageEngine.ImageEngineInterface {
                override fun finished(id: String, resource: Drawable) {
                    if (item.id == id) {
                        anim.end()


                        (context as MainActivity).runOnUiThread {
                            val circle = holder.imageView as CircularImageView?
                            circle?.setImageDrawable(resource)
                        }
                    }
                }
            })

        }

        holder.frameLayout?.setOnClickListener {
            val item = items[position]

            Log.d(WebService::class.java.toString(), "Selected: " + item.id)

            actionInterface?.loadIndividual(item)
        }

        holder.deleteButton?.setOnClickListener {
            AppManager.shared.showprogressDialog(context, "Individual", "Deleting..")

            AppManager.shared.webService.deleteIndividual(item.id, object : WebService.BasicSuccessFailureInterface {
                override fun onSuccess() {
                    AppManager.shared.dismissProgressDialog()

                    AppManager.shared.directoryListingRefreshNeeded = true
                    actionInterface?.refreshIndividuals()
                }

                override fun onFailure() {
                    AppManager.shared.dismissProgressDialog()

                }
            })
        }

        return convertView
    }

}
