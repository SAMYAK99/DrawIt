package com.projects.drawit

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import petrov.kristiyan.colorpicker.ColorPicker
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawing_view.setSizeForBrush(20.toFloat()) // Setting the default brush size to drawing view.

        // Selecting the Brush Size
        ib_brush.setOnClickListener {
            showBrushSizeChooserDialog()
        }

        colourPicker.setOnClickListener {
            selectColourDialog()
        }

        // Setting the background image
        gallery.setOnClickListener{
            if(isReadStorageAllowed()) {
                // run our code
                if (isReadStorageAllowed()) {
                    // This is for selecting the image from local store or let say from Gallery/Photos.
                    val pickPhoto = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(pickPhoto, GALLERY)
                }
            }
            else{
                request_storage_permission()
            }
        }

        // Undo
        ib_undo.setOnClickListener{
            drawing_view.onClickUndo()
        }

        //Saving
        ib_save.setOnClickListener{
            if(isReadStorageAllowed()){
                BitmapAsyncTask(getBitmapFromView(fl_drawing_view_container)).execute();
            }
            else{
                request_storage_permission()
            }
        }
    }


    // This method is called when different Colours is selected
    private fun selectColourDialog() {
        val colourPicker = ColorPicker(this)
        colourPicker.setOnFastChooseColorListener(object : ColorPicker.OnFastChooseColorListener {
            override fun setOnFastChooseColorListener(position: Int, color: Int) {
                drawing_view.setColor(color)
            }

            override fun onCancel() {
                colourPicker.dismissDialog()
            }

        })

            .disableDefaultButtons(true)
            .setRoundColorButton(true)
            .setColumns(5)
            .show();
    }


    /**
     * Method is used to launch the dialog to select different brush sizes.
     */
    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size :")
        val smallBtn = brushDialog.small_brush
        smallBtn.setOnClickListener(View.OnClickListener {
            drawing_view.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        })
        val mediumBtn = brushDialog.medium_brush
        mediumBtn.setOnClickListener(View.OnClickListener {
            drawing_view.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        })

        val largeBtn = brushDialog.large_brush
        largeBtn.setOnClickListener(View.OnClickListener {
            drawing_view.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        })


        brushDialog.show()
    }


    // getting the image from storage
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                try {
                    if (data!!.data != null) {
                        // Here if the user selects the image from local storage make the image view visible.
                        // By Default we will make it VISIBILITY as GONE.
                        iv_background.visibility = View.VISIBLE
                        // Set the selected image to the backgroung view.
                        iv_background.setImageURI(data.data)
                    } else {
                        // If the selected image is not valid. Or not selected.
                        Toast.makeText(
                            this@MainActivity,
                            "Error in parsing the image or its corrupted.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }




    private fun request_storage_permission() {
        // Informing the user if it denies
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString())) {
            Toast.makeText(this, "Need Permission to add Image is Screen", Toast.LENGTH_SHORT)
                .show()
        }
        // Requesting the permission to user
        ActivityCompat.requestPermissions( this, arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== STORAGE_PERMISSION_CODE){
            if(grantResults.size != null &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Now you can read Storage Files", Toast.LENGTH_SHORT)
                    .show()
            }
            else{
                Toast.makeText(this, "Please allow the permission to read files", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }



     // We are calling this method to check the permission status
    private fun isReadStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }



     // Create bitmap from view and returns it
    private fun getBitmapFromView(view: View): Bitmap {
        //Define a bitmap with the same size as the view.
        // CreateBitmap : Returns a mutable bitmap with the specified width and height
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        }
        // draw the view on the canvas
        view.draw(canvas)
        return returnedBitmap
    }


    // Saving the bitmap in device
    @SuppressLint("StaticFieldLeak")
     private inner class BitmapAsyncTask (val mBitmap: Bitmap):
         AsyncTask<Any,Void,String>() {
        override fun doInBackground(vararg p0: Any?): String {
            var result = ""
            if (mBitmap != null) {

                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    val f = File(
                        externalCacheDir!!.absoluteFile.toString()
                                + File.separator + "DrawingApp_" + System.currentTimeMillis() / 1000 + ".jpg"
                    )

                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result = f.absolutePath
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()

                }
            }
            return result
        }


        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

//            cancelProgressDialog()

            if (result!=null) {
                Toast.makeText(
                    this@MainActivity,
                    "File saved successfully :$result",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Something went wrong while saving the file.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }


    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }

}