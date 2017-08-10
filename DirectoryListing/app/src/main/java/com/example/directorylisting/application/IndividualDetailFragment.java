package com.example.directorylisting.application;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.directorylisting.api.WebService;
import com.example.directorylisting.entities.Individual;
import com.example.directorylisting.shared.AppManager;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

/**
 * Created by Michael Steele on 3/13/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

public class IndividualDetailFragment extends android.support.v4.app.Fragment {

    private static final int CAMERA_REQUEST = 2;

    ImageView imageView = null;

    Individual individual = null;

    File folder = null;

    File file = null;

    Bitmap capturedPhoto = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_individual_detail, container, false);

        return view;
    }


    public void setIndividual(Individual temp) {
        individual = temp;

        final EditText nameEditText = (EditText) getView().findViewById(R.id.name_text);
        nameEditText.setText(individual.getPrettyFullName());

        final EditText birthDateEditText = (EditText) getView().findViewById(R.id.born_text);
        birthDateEditText.setText(individual.getPrettyBirthDate());

        final EditText affiliationEditText = (EditText) getView().findViewById(R.id.affiliation_text);
        affiliationEditText.setText(individual.getPrettyAffiliationText());

        imageView = (ImageView) getView().findViewById(R.id.profile_image);
        int dpImage = (int) this.getContext().getResources().getDimension(R.dimen.directory_listing_image_size);

        try {

            // Glide 3
            Glide.with(getContext())
                    .load(individual.getPrettyProfilePicture())
                    .placeholder(R.drawable.missing)
                    .override(dpImage, dpImage)
                    .centerCrop()
                    .signature(AppManager.shared.getCacheKey(individual))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageView);

            /*
            // Glide 4
            Glide.with(getContext())
                    .load(individual.getPrettyProfilePicture())
                    .apply(RequestOptions.placeholderOf(R.drawable.missing))
                    .apply(RequestOptions.overrideOf(dpImage, dpImage))
                    .apply(RequestOptions.signatureOf(AppManager.shared.getCacheKey(individual)))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.AUTOMATIC))
                    .into(imageView);
            */

        } catch (Exception e) {
            Log.d(IndividualListAdapter.class.toString(), "Error: " + e.toString());
        }


        Button saveButton = (Button) getView().findViewById(R.id.save_button);

        final WebService.IndividualInterface individualInterface = new WebService.IndividualInterface() {
            @Override
            public void onResponse(Individual individual) {
                if (capturedPhoto == null) {
                    AppManager.shared.dismissProgressDialog();

                    finishAndRefreshListing();
                    return;
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                imageView.buildDrawingCache();
                Bitmap bitmap = capturedPhoto != null ? capturedPhoto : imageView.getDrawingCache();
                imageView.destroyDrawingCache();
                bitmap.compress(Bitmap.CompressFormat.PNG, AppManager.shared.imageCompressionPercentage, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                AppManager.shared.showprogressDialog(getContext(), "Individual", "Uploading photo...");

                AppManager.shared.webService.uploadFile(individual.getId(), Base64.encodeToString(byteArray, Base64.DEFAULT), new WebService.BasicSuccessFailureInterface() {
                            @Override
                            public void onSuccess() {
                                AppManager.shared.dismissProgressDialog();

                                finishAndRefreshListing();
                            }

                            @Override
                            public void onFailure() {

                                AppManager.shared.dismissProgressDialog();
                            }
                        }
                );

            }

            @Override
            public void onFailure() {
                AppManager.shared.dismissProgressDialog();
            }
        };

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                individual.firstName = nameEditText.getText().toString().trim();
                individual.lastName = "";
                individual.birthdate = birthDateEditText.getText().toString().trim();
                individual.affiliation = affiliationEditText.getText().toString().trim();

                if (individual.getId().isEmpty()) {
                    AppManager.shared.showprogressDialog(getContext(), "Individual", "Creating...");


                    AppManager.shared.webService.createIndividual(individual, individualInterface);
                } else {
                    AppManager.shared.showprogressDialog(getContext(), "Individual", "Saving...");


                    AppManager.shared.webService.modifyIndividual(individual.id, individual, individualInterface);
                }
            }
        });

        ImageView imageButton = (ImageView) getView().findViewById(R.id.profile_image);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(WebService.class.toString(), "Tapped photo to take a picture.");

                folder = new File(getContext().getExternalCacheDir().toString() + "/");

                file = new File(folder.toString() + "/temp_capture.jpg");
                Uri uriImage = Uri.fromFile(file);

                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriImage);

                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            /*
            // Glide 4
            Glide.with(getContext())
                    .clear(imageView);*/

            // Load cached image.
            Bitmap photoToDelete = BitmapFactory.decodeFile(file.getPath());
            assert(photoToDelete != null); // Assert to make sure it exists.

            // Since the image is stored in the cache directory and not sent through intent data, we need to get the orientation from the Exif data from the loaded file.
            try {

                ExifInterface ei = new ExifInterface(file.toString());
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                Bitmap photoToRotate = null;

                switch (orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:
                        photoToRotate = rotateImage(photoToDelete, 90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        photoToRotate = rotateImage(photoToDelete, 180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        photoToRotate = rotateImage(photoToDelete, 270);
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:

                    default:
                        break;
                }

                capturedPhoto = photoToRotate;

            } catch (Exception e) {

                capturedPhoto = photoToDelete;

            }

            imageView.setImageBitmap(capturedPhoto);

            file.delete(); // Delete cached image.

            // One more check to see if it's deleted.
            photoToDelete = BitmapFactory.decodeFile(file.getPath());
            assert(photoToDelete == null);
        }
    }

    public void finishAndRefreshListing() {
        /*
        //Glide 4
        Glide.with(getContext())
            .clear(imageView);
        */
        imageView.setImageDrawable(null);
        imageView = null;

        individual = null;

        folder = null;

        file = null;

        capturedPhoto = null;
        getActivity().setResult(DirectoryListingFragment.REQUEST_REFRESH, null);
        getActivity().finish();
    }

}
