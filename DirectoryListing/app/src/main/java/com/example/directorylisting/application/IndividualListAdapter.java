package com.example.directorylisting.application;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.example.directorylisting.api.WebService;
import com.example.directorylisting.entities.ImageEngine;
import com.example.directorylisting.entities.Individual;
import com.example.directorylisting.shared.AppManager;
import com.pkmmte.view.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Michael Steele on 7/13/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

public class IndividualListAdapter extends ArrayAdapter<Individual> {

    static class ViewHolder {
        ImageView imageView;
        Button deleteButton;
        FrameLayout frameLayout;
        SwipeRevealLayout swipeRevealLayout;
    }

    List<Individual> items = new ArrayList<Individual>();
    HashMap<Integer,ImageEngine> imageEngines = new HashMap<Integer,ImageEngine>();
    ListView listView = null;


    int dpImage = (int) this.getContext().getResources().getDimension(R.dimen.directory_listing_image_size);

    DirectoryListingFragment.DirectoryListingActionInterface actionInterface = null;

    public IndividualListAdapter(Context context, int textViewResourceId,
                              List<Individual> objects) {
        super(context, textViewResourceId, objects);
        items = objects;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final Individual item = getItem(position);

        final ViewHolder holder;

        //if (convertView == null) { // TODO: Fix recycler with glide.

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_directory_listing_item, parent, false);


            holder = new ViewHolder();

            holder.imageView = (ImageView) convertView.findViewById(R.id.profile_image);

            holder.deleteButton = (Button) convertView.findViewById(R.id.delete_button);

            holder.frameLayout = (FrameLayout) convertView.findViewById(R.id.directory_listing_list_item);

            holder.swipeRevealLayout = (SwipeRevealLayout) convertView.findViewById(R.id.directory_listing_swipe_layout);

            convertView.setTag(holder);
        /*} else {
            holder = (ViewHolder) convertView.getTag();
        }*/



        /*
        //Glide 4
        Glide.with(holder.imageView.getContext())
                .clear(holder.imageView);
        */

        holder.imageView.setImageDrawable(null);
        holder.imageView.setTag(item.getId());

        if (item.profilePicture.isEmpty()) {

            holder.imageView.setImageResource(R.drawable.missing);
            holder.imageView.setRotation(0f);

        } else {

            holder.imageView.setImageResource(R.drawable.spinner);
            final ObjectAnimator anim = ObjectAnimator.ofFloat(holder.imageView,
                    "rotation", 0f, 360f);
            anim.setDuration(500);
            anim.setRepeatCount(ObjectAnimator.INFINITE);
            anim.start();

            if (imageEngines.get(position) == null) {
                imageEngines.put(position, new ImageEngine());
            }

            ImageEngine imageEngine = imageEngines.get(position);

            imageEngines.get(position).loadImage(getContext(), item.id, AppManager.shared.getCacheKey(item), item.getPrettyProfilePicture(), dpImage, new ImageEngine.ImageEngineInterface() {
                @Override
                public void finished(String id, final Drawable resource) {
                    if (item.id.equals(id)) {
                        anim.end();


                        ((MainActivity)getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //holder.imageView.setImageBitmap(null);
                                //holder.imageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.missing, getContext().getTheme()));
                                CircularImageView circle = (CircularImageView)holder.imageView;
                                //circle.setImageDrawable(getContext().getResources().getDrawable(R.drawable.missing, getContext().getTheme()));
                                /*circle.setImageDrawable(resource);
                                circle.getRootView().invalidate();
                                circle.getRootView().refreshDrawableState();*/

                                circle.setImageDrawable(resource);

                                //notifyDataSetChanged();


                                /*if (position >= listView.getFirstVisiblePosition()
                                        && position <= listView.getLastVisiblePosition()) {

                                    listView.invalidateViews();

                                }*/

                                /*holder.imageView.invalidate();
                                holder.imageView.postInvalidate();
                                holder.imageView.refreshDrawableState();
                                holder.imageView.setImageDrawable(resource);
                                holder.imageView.invalidate();
                                holder.imageView.postInvalidate();
                                holder.imageView.refreshDrawableState();*/

                            }
                        });
                    }
                }
            });

        }

        holder.frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Individual item = items.get(position);

                Log.d(WebService.class.toString(), "Selected: " + item.getId());

                actionInterface.loadIndividual(item);

            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.shared.showprogressDialog(getContext(), "Individual", "Deleting..");

                AppManager.shared.webService.deleteIndividual(item.id, new WebService.BasicSuccessFailureInterface() {
                    @Override
                    public void onSuccess() {
                        AppManager.shared.dismissProgressDialog();

                        AppManager.shared.directoryListingRefreshNeeded = true;
                        actionInterface.refreshIndividuals();
                    }

                    @Override
                    public void onFailure() {
                        AppManager.shared.dismissProgressDialog();

                    }
                });

            }
        });

        return convertView;
    }

}
