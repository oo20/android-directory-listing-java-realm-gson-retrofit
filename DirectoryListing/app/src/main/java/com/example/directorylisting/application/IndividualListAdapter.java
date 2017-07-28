package com.example.directorylisting.application;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.BoolRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.example.directorylisting.api.WebService;
import com.example.directorylisting.entities.Directory;
import com.example.directorylisting.entities.Individual;

import java.util.ArrayList;
import java.util.List;

import com.example.directorylisting.shared.AppManager;

import static android.R.attr.content;
import static android.R.attr.id;

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



        Glide.with(holder.imageView.getContext())
                .clear(holder.imageView);

        holder.imageView.setImageDrawable(null);

        if (item.profilePicture.isEmpty()) {
            holder.imageView.setImageResource(R.drawable.missing);
            holder.imageView.setRotation(0f);
        } else try {

            final ObjectAnimator anim = ObjectAnimator.ofFloat(holder.imageView,
                    "rotation", 0f, 360f);
            anim.setDuration(500);
            anim.setRepeatCount(ObjectAnimator.INFINITE);
            anim.start();

            Glide.with(holder.imageView.getContext())
                    .load(item.getPrettyProfilePicture())
                    .apply(RequestOptions.placeholderOf(R.drawable.spinner))
                    .apply(RequestOptions.overrideOf(dpImage, dpImage))
                    .apply(RequestOptions.signatureOf(AppManager.shared.getCacheKey(item)))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.AUTOMATIC))
                    .apply(RequestOptions.priorityOf(Priority.HIGH))
                    .apply(RequestOptions.centerCropTransform())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            anim.end();
                            //imageView.setRotation(0f);
                            return false;
                        }

                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            anim.end();
                            //imageView.setRotation(0f);
                            return false;
                        }
                    })
                    .into(holder.imageView);

        } catch (Exception e) {
            Log.d(IndividualListAdapter.class.toString(), "Error: " + e.toString());
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
