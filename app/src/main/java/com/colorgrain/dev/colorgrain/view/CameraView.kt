package com.colorgrain.dev.colorgrain.views

import android.content.Context
import android.graphics.*
import android.hardware.Camera
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.widget.ImageView
import org.jetbrains.anko.imageBitmap
import android.graphics.PorterDuffXfermode
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.R.attr.data
import android.graphics.YuvImage
import java.io.ByteArrayOutputStream
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth


/**
 * Created by luan on 03/09/17.
 */
class CameraView(context: Context, camera: Camera, val callback: (Float, Float, Float) -> Unit) : SurfaceView(context), SurfaceHolder.Callback, Camera.PreviewCallback {
    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        try {
            //when the surface is created, we can set the camera to draw images in this surfaceholder
            mCamera.setPreviewDisplay(mHolder)
            mCamera.setPreviewCallback(this)
            mCamera.startPreview()
            if (mCamera != null) {
                val params = mCamera.parameters
                params.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;

                mCamera.parameters = params

            }
        } catch (e: Exception) {
            Log.d("ERROR", "Camera error on surfaceCreated " + e.message)
        }

    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        //our app has only one screen, so we'll destroy the camera in the surface
        //if you are unsing with more screens, please move this code your activity
        mCamera.stopPreview();
        mCamera.release();
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        if (mHolder.surface == null)
        //check if the surface is ready to receive camera data
            return

        try {
            mCamera.stopPreview()
        } catch (e: Exception) {
            //this will happen when you are trying the camera if it's not running
        }


        //now, recreate the camera preview
        try {
            mCamera.setPreviewDisplay(mHolder)
            mCamera.startPreview()
        } catch (e: IOException) {
            Log.d("ERROR", "Camera error on surfaceChanged " + e.message)
        }

    }


    var mHolder: SurfaceHolder
    var mCamera: Camera
    var imageView: ImageView? = null

    var touchedX: Float = 0f
    var touchedY: Float = 0f
    var touched: Boolean = false


    init {
        mCamera = camera;
        mCamera.setDisplayOrientation(90);

        //get the holder and set this class as the callback, so we can get camera data here
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        // TODO Auto-generated method stub

        touchedX = event.x
        touchedY = event.y

        val action = event.action

        val lastTouched = touched

        when (action) {
            MotionEvent.ACTION_DOWN -> touched = true;

            MotionEvent.ACTION_MOVE -> touched = true;

            MotionEvent.ACTION_UP -> touched = false;

            MotionEvent.ACTION_CANCEL -> touched = false;

            MotionEvent.ACTION_OUTSIDE -> touched = false;

        }


        if (lastTouched != touched) {
            updateTouched()

        }

        return true
    }

    private fun updateTouched() {


        if (touched) {

            takePicture()

        }

    }

    override fun onPreviewFrame(data: ByteArray?, p1: Camera?) {

        if (data != null) {

            val parameters = mCamera.parameters
            val width = parameters.previewSize.width
            val height = parameters.previewSize.height

            val yuv = YuvImage(data, parameters.previewFormat, width, height, null)
            val out = ByteArrayOutputStream()
            yuv.compressToJpeg(Rect(0, 0, width, height), 50, out)

            val bytes = out.toByteArray()

            try {
                val bmp = cropBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))


                processImage(bmp, 100, 100)

            } catch (e: Exception) {
                Log.d("a", e.message)
            }
        }

    }

    private fun processImage(bmp: Bitmap, width: Int, height: Int) {

        var centerX = width / 2
        var centerY = height / 2


        var centerPixel =  bmp.getPixel(centerX, centerY)

        var redPixel = Color.red(centerPixel)
        var bluePixel = Color.blue(centerPixel)
        var greenPixel = Color.green(centerPixel)


        val hsv = FloatArray(3)
        Color.RGBToHSV(redPixel,  greenPixel, bluePixel, hsv)

        callback(hsv[0], hsv[1], hsv[2])

    }

    private fun cropBitmap(bitmap: Bitmap): Bitmap {

        val parameters = mCamera.parameters
        val portraitWidth = 100
        val portraitHeight = 100
        val width = parameters.previewSize.width
        val height = parameters.previewSize.height


        val topX = (width / 2) - (portraitWidth / 2)
        val topY = (height / 2) - (portraitHeight / 2)


        val croppedBitmap = Bitmap.createBitmap(bitmap, topX, topY, portraitWidth, portraitHeight)

        return croppedBitmap
    }

    private fun takePicture() {


    }

    fun getImage(): Bitmap {
        val b = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        draw(c)
        return b
    }


}

