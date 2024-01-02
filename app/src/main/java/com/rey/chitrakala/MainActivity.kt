package com.rey.chitrakala

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : AppCompatActivity() {

  private var drawingView: DrawingView? = null
  private var mImageButtonCurrentPaint: ImageButton? = null

  val openGalleryLauncher: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
      result ->
      if(result.resultCode == RESULT_OK && result.data!= null){
        val imageBackground:ImageView = findViewById(R.id.iv_background)
        imageBackground.setImageURI(result.data?.data)
      }
    }

  val requestPermission: ActivityResultLauncher<Array<String>> =
      registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions
        ->
        permissions.entries.forEach {
          val permissionName = it.key
          val isGranted = it.value

          if (isGranted) {
            Toast.makeText(
                    this@MainActivity,
                    "Permission Granted now you can read the storage files.",
                    Toast.LENGTH_LONG)
                .show()
            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            openGalleryLauncher.launch(pickIntent)

          } else {
            if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
              Toast.makeText(
                      this@MainActivity, "Oops you just denied the permission", Toast.LENGTH_LONG)
                  .show()
            }
          }
        }
      }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    drawingView = findViewById(R.id.drawing_view)
    drawingView?.setSizeForBrush(20.toFloat())

    val linearLayoutPaintColors = findViewById<LinearLayout>(R.id.ll_paint_colors)

    mImageButtonCurrentPaint = linearLayoutPaintColors[0] as ImageButton

    val ib_brush: ImageButton = findViewById(R.id.ib_brush)
    ib_brush.setOnClickListener { showBrushSizeChooserDialog() }

    val ib_undo: ImageButton = findViewById(R.id.ib_undo)
    ib_undo.setOnClickListener {
      drawingView?.onClickUndo()
    }

    val ib_redo: ImageButton = findViewById(R.id.ib_redo)
    ib_redo.setOnClickListener {
      drawingView?.onClickRedo()
    }

    val ib_gallery: ImageButton = findViewById(R.id.ib_gallery)
    ib_gallery.setOnClickListener { requestStoragePermission() }
  }

  private fun showBrushSizeChooserDialog() {
    val brushDialog = Dialog(this)

    brushDialog.setContentView(R.layout.dialog_brush_size)
    brushDialog.setTitle("Brush Size: ")

    val smallBtn: ImageButton = brushDialog.findViewById(R.id.ib_small_brush)
    smallBtn.setOnClickListener {
      drawingView?.setSizeForBrush(10.toFloat())
      brushDialog.dismiss()
    }

    val mediumBtn: ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
    mediumBtn.setOnClickListener {
      drawingView?.setSizeForBrush(20.toFloat())
      brushDialog.dismiss()
    }

    val largeBtn: ImageButton = brushDialog.findViewById(R.id.ib_large_brush)
    largeBtn.setOnClickListener {
      drawingView?.setSizeForBrush(30.toFloat())
      brushDialog.dismiss()
    }

    brushDialog.show()
  }

  fun paintClicked(view: View) {
    if (view !== mImageButtonCurrentPaint) {
      val imageButton = view as ImageButton
      val colorTag = imageButton.tag.toString()
      drawingView?.setColor(colorTag)

      imageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))

      mImageButtonCurrentPaint?.setImageDrawable(
          ContextCompat.getDrawable(this, R.drawable.pallet_normal))

      mImageButtonCurrentPaint = view
    }
  }

  private fun requestStoragePermission() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(
        this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
      showRationaleDialog("ChitraKala", "ChitraKala needs to access your External Storage")
    } else {
      requestPermission.launch(
          arrayOf(
              Manifest.permission.READ_EXTERNAL_STORAGE
              // TODO - Add Writing external storage permission
              ))
    }
  }

  @SuppressLint("SuspiciousIndentation")
  private fun showRationaleDialog(
      title: String,
      message: String,
  ) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
      builder.setTitle(title).setMessage(message).setPositiveButton("Cancel") { dialog, _ ->
      dialog.dismiss()
    }
    builder.create().show()
  }
}
