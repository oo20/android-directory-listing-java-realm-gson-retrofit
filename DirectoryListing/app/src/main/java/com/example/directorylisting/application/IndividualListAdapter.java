package com.example.directorylisting.application;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.directorylisting.entities.Individual;

import java.util.ArrayList;
import java.util.List;

import com.example.directorylisting.shared.AppManager;

import static android.R.attr.id;

/**
 * Created by Michael Steele on 7/13/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

public class IndividualListAdapter extends ArrayAdapter<Individual> {

    List<Individual> items = new ArrayList<Individual>();

    int dpImage = (int) this.getContext().getResources().getDimension(R.dimen.directory_listing_image_size);

    public IndividualListAdapter(Context context, int textViewResourceId,
                              List<Individual> objects) {
        super(context, textViewResourceId, objects);
        items = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Individual item = getItem(position);

        // TODO: Recyler
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_directory_listing_item, parent, false);

        final ImageView imageView = (ImageView) convertView.findViewById(R.id.profile_image);

        Glide.with(getContext())
                .clear(imageView);

        if (item.profilePicture.isEmpty()) {
            imageView.setImageResource(R.drawable.missing);
            imageView.setRotation(0f);
        } else try {

            final ObjectAnimator anim = ObjectAnimator.ofFloat(imageView,
                    "rotation", 0f, 360f);
            anim.setDuration(500);
            anim.setRepeatCount(ObjectAnimator.INFINITE);
            anim.start();

            Glide.with(getContext())
                    .load(item.getPrettyProfilePicture())
                    .apply(RequestOptions.placeholderOf(R.drawable.spinner))
                    .apply(RequestOptions.overrideOf(dpImage, dpImage))
                    .apply(RequestOptions.signatureOf(AppManager.shared.getCacheKey(item)))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.AUTOMATIC))
                    .apply(RequestOptions.centerCropTransform())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            anim.cancel();
                            imageView.setRotation(0f);
                            return false;
                        }

                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            anim.cancel();
                            imageView.setRotation(0f);
                            return false;
                        }
                    })
                    .into(imageView);

        } catch (Exception e) {
            Log.d(IndividualListAdapter.class.toString(), "Error: " + e.toString());
        }

        return convertView;
    }

}
