package com.example.directorylisting.application

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import com.example.directorylisting.api.WebService
import com.example.directorylisting.entities.Individual
import com.example.directorylisting.shared.AppManager
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Created by Michael Steele on 3/13/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

class IndividualDetailFragment : android.support.v4.app.Fragment() {

    internal var imageView: ImageView? = null

    internal var individual: Individual? = null

    internal var folder: File? = null

    internal var file: File? = null

    internal var capturedPhoto: Bitmap? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_individual_detail, container, false)
    }


    fun setIndividual(temp: Individual) {
        individual = temp
        val tempIndividual = temp?: return
        val tempView = view?: return

        imageView = tempView.findViewById<View>(R.id.profile_image) as ImageView
        val tempImageView = imageView?: return

        val nameEditText = tempView.findViewById<View>(R.id.name_text) as EditText
        nameEditText.setText(tempIndividual.prettyFullName)

        val birthDateEditText = tempView.findViewById<View>(R.id.born_text) as EditText
        birthDateEditText.setText(tempIndividual.birthdate)

        val affiliationEditText = tempView.findViewById<View>(R.id.affiliation_text) as EditText
        affiliationEditText.setText(tempIndividual.prettyAffiliationText)

        val dpImage = this.context.resources.getDimension(R.dimen.directory_listing_image_size).toInt()

        try {

            // Glide 3
            Glide.with(context)
                    .load(tempIndividual.prettyProfilePicture)
                    .placeholder(R.drawable.missing)
                    .override(dpImage, dpImage)
                    .centerCrop()
                    .signature(AppManager.shared.getCacheKey(temp))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageView)

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

        } catch (e: Exception) {
            Log.d(IndividualListAdapter::class.java.toString(), "Error: " + e.toString())
        }

        val saveButton = tempView.findViewById<View>(R.id.save_button) as Button

        val individualInterface = object : WebService.IndividualInterface {
            override fun onResponse(individual: Individual) {
                if (capturedPhoto == null) {
                    AppManager.shared.dismissProgressDialog()

                    finishAndRefreshListing()
                    return
                }

                val byteArrayOutputStream = ByteArrayOutputStream()
                tempImageView.buildDrawingCache()
                val bitmap = if (capturedPhoto != null) capturedPhoto else tempImageView.drawingCache
                val tempBitmap = bitmap?: return
                tempImageView.destroyDrawingCache()
                tempBitmap.compress(Bitmap.CompressFormat.PNG, AppManager.shared.imageCompressionPercentage, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()

                AppManager.shared.showprogressDialog(context, "Individual", "Uploading photo...")

                AppManager.shared.webService.uploadFile(individual.id, Base64.encodeToString(byteArray, Base64.DEFAULT), object : WebService.BasicSuccessFailureInterface {
                    override fun onSuccess() {
                        AppManager.shared.dismissProgressDialog()

                        finishAndRefreshListing()
                    }

                    override fun onFailure() {

                        AppManager.shared.dismissProgressDialog()
                    }
                })

            }

            override fun onFailure() {
                AppManager.shared.dismissProgressDialog()
            }
        }

        saveButton.setOnClickListener {
            val clickedIndividual = individual!! // "?: return" not supported in lambda and anonymous function not working.  Try prior in future kotlin release

            clickedIndividual.firstName = nameEditText.text.toString().trim { it <= ' ' }
            clickedIndividual.lastName = ""
            clickedIndividual.birthdate = birthDateEditText.text.toString().trim { it <= ' ' }
            clickedIndividual.affiliation = affiliationEditText.text.toString().trim { it <= ' ' }

            if (clickedIndividual.id.isEmpty()) {
                AppManager.shared.showprogressDialog(context, "Individual", "Creating...")


                AppManager.shared.webService.createIndividual(clickedIndividual, individualInterface)
            } else {
                AppManager.shared.showprogressDialog(context, "Individual", "Saving...")


                AppManager.shared.webService.modifyIndividual(clickedIndividual.id, clickedIndividual, individualInterface)
            }
        }

        val imageButton = tempView.findViewById<View>(R.id.profile_image) as ImageView
        imageButton.setOnClickListener {
            Log.d(WebService::class.java.toString(), "Tapped photo to take a picture.")

            folder = File(context.externalCacheDir?.toString() + "/")

            file = File(folder?.toString() + "/temp_capture.jpg")
            val uriImage = Uri.fromFile(file)

            val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriImage)

            startActivityForResult(cameraIntent, CAMERA_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            /*
            // Glide 4
            Glide.with(getContext())
                    .clear(imageView);*/

            // Load cached image.
            var photoToDelete: Bitmap? = BitmapFactory.decodeFile(file?.path)

            // Since the image is stored in the cache directory and not sent through intent data, we need to get the orientation from the Exif data from the loaded file.
            try {

                val ei = ExifInterface(file?.toString())
                val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED)

                var photoToRotate: Bitmap? = null

                when (orientation) {

                    ExifInterface.ORIENTATION_ROTATE_90 -> photoToRotate = rotateImage(photoToDelete, 90)

                    ExifInterface.ORIENTATION_ROTATE_180 -> photoToRotate = rotateImage(photoToDelete, 180)

                    ExifInterface.ORIENTATION_ROTATE_270 -> photoToRotate = rotateImage(photoToDelete, 270)

                    ExifInterface.ORIENTATION_NORMAL -> {
                    }

                    else -> {
                    }
                }

                capturedPhoto = photoToRotate

            } catch (e: Exception) {

                capturedPhoto = photoToDelete

            }

            imageView?.setImageBitmap(capturedPhoto)

            file?.delete() // Delete cached image.

            // One more check to see if it's deleted.
            photoToDelete = BitmapFactory.decodeFile(file?.path)
            assert(photoToDelete == null)
        }
    }

    fun finishAndRefreshListing() {
        /*
        //Glide 4
        Glide.with(getContext())
            .clear(imageView);
        */
        imageView?.setImageDrawable(null)
        imageView = null

        individual = null

        folder = null

        file = null

        capturedPhoto = null
        activity.setResult(DirectoryListingFragment.REQUEST_REFRESH, null)
        activity.finish()
    }

    companion object {

        private val CAMERA_REQUEST = 2
    }

}
