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
        val mTextRobotoTf = ResourcesCompat.getFont(this, com.heena.user.R.font.avenir_45_book)

        var mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView)
            .setPinchTextScalable(true)
            .setClipSourceImage(true)
            .setDefaultTextTypeface(mTextRobotoTf)
            .build()

        val mShapeBuilder = ShapeBuilder().withShapeOpacity(100)
            .withShapeType(ShapeType.OVAL)
            .withShapeSize(50F)
        mPhotoEditor.setShape(mShapeBuilder)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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