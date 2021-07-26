package com.projects.drawit

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import petrov.kristiyan.colorpicker.ColorPicker


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawing_view.setSizeForBrush(20.toFloat()) // Setting the default brush size to drawing view.

      // Selecting the Brush Size
        ib_brush.setOnClickListener {
            showBrushSizeChooserDialog()
        }

        colourPicker.setOnClickListener{
           selectColourDialog()
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

}