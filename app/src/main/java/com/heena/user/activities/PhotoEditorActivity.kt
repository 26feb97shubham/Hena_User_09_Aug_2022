package com.heena.user.activities

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.graphics.Typeface
import android.util.Log
import android.view.MotionEvent
import android.view.View

import androidx.core.content.res.ResourcesCompat
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import ja.burhanrashid52.photoeditor.*

import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeType
import java.lang.Exception


class PhotoEditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.heena.user.R.layout.activity_photo_editor)

        var mPhotoEditorView = findViewById<PhotoEditorView>(com.heena.user.R.id.photoEditorView)

        mPhotoEditorView.source.setImageResource(com.heena.user.R.drawable.download)

        //Use custom font using latest support library
        //Use custom font using latest support library
        val mTextRobotoTf = ResourcesCompat.getFont(this, com.heena.user.R.font.avenir_45_book)

//loading font from asset

//loading font from asset
//        val mEmojiTypeFace = Typeface.createFromAsset(assets, "emojione-android.ttf")

        var mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView)
            .setPinchTextScalable(true)
            .setClipSourceImage(true)
            .setDefaultTextTypeface(mTextRobotoTf)
            .build()

        val mShapeBuilder = ShapeBuilder().withShapeOpacity(100)
            .withShapeType(ShapeType.OVAL)
            .withShapeSize(50F)
        mPhotoEditor.setShape(mShapeBuilder)

        mPhotoEditor.setOnPhotoEditorListener(object : OnPhotoEditorListener {
            override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {

            }

            override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {}
            override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {

            }

            override fun onStartViewChangeListener(viewType: ViewType?) {

            }

            override fun onStopViewChangeListener(viewType: ViewType?) {

            }

            override fun onTouchSourceImage(event: MotionEvent?) {

            }
        })


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        val filePath = ""
        mPhotoEditor.saveAsFile(filePath, object : PhotoEditor.OnSaveListener {
            override fun onSuccess(imagePath: String) {
                Log.e("PhotoEditor", "Image Saved Successfully")
            }

            override fun onFailure(exception: Exception) {
                Log.e("PhotoEditor", "Failed to save Image")
            }
        })

    }
}