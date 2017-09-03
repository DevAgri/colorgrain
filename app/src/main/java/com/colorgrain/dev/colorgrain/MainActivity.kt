package com.colorgrain.dev.colorgrain

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

import com.colorgrain.dev.colorgrain.views.CameraView
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainActivity : AppCompatActivity() {

    private var mCamera: Camera? = null
    private var mCameraView: CameraView? = null

    private val textHue by lazy { find<TextView>(R.id.tv_hue)}
    private val textSaturation by lazy { find<TextView>(R.id.tv_saturation)}
    private val textValue by lazy { find<TextView>(R.id.tv_value)}

    private val CHECK_CAMERA_PERMISSION = 111

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            mCamera = Camera.open()//you can use open(int) to use different cameras
        } catch (e: Exception) {
            Log.d("ERROR", "Failed to get camera: " + e.message)
        }

        if (mCamera != null) {
            mCameraView = CameraView(this, mCamera!!, { h, s, v -> showValues(h, s, v) })//create a SurfaceView to show camera data
            mCameraView?.imageView = find<ImageView>(R.id.img_view)
            val camera_view = findViewById(R.id.camera_view) as FrameLayout
            camera_view.addView(mCameraView)//add the SurfaceView to the layout


        }

        //btn to close the application
        val imgClose = findViewById(R.id.imgClose) as ImageButton
        imgClose.setOnClickListener { System.exit(0) }

        askForPermission()
    }

    private fun showValues(h: Float, s: Float, v: Float) {

        textHue.text = "HUE: $h"
        textSaturation.text = "SAT.: $s"
        textValue.text = "VAL.: $v"


    }


    internal fun askForPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?


            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),
                    CHECK_CAMERA_PERMISSION)

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            goAhead()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CHECK_CAMERA_PERMISSION) {
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]

                if (permission == Manifest.permission.SEND_SMS) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        goAhead()
                    } else {
                        askForPermission()
                    }
                }
            }
        }
    }

    internal fun goAhead() {


    }
}