package com.mux.libcamera.camera1;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.mux.libcamera.encoders.Encoder;
import com.mux.libcamera.utils.ImageUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;

@SuppressWarnings("deprecation")
class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private final static String TAG = "CameraPreview";

    boolean takeSnapshot = false;
    private WeakReference<Camera> camera;
    private int captureRotation = 0;
    private Encoder encoder;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.camera = new WeakReference<>(camera);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.get().setPreviewDisplay(holder);
            camera.get().startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            camera.get().stopPreview();
            camera.get().setPreviewCallback(null);
            int frameBufferSize = calcCameraFrameBufferSize();
            for (int i = 0; i < 10; i++)
                camera.get().addCallbackBuffer(new byte[frameBufferSize]);
            camera.get().setPreviewCallbackWithBuffer(this);
            camera.get().setPreviewDisplay(holder);
            camera.get().startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int calcCameraFrameBufferSize() {
        int frameSize;
        Camera.Parameters params= camera.get().getParameters();
        int format = params.getPreviewFormat();
        Camera.Size size = params.getPreviewSize();
        if (format != ImageFormat.YV12) {
            frameSize = size.height * size.width * ImageFormat.getBitsPerPixel(format) / 8;
        } else {
            int yStride = (int)Math.ceil(size.width / 16.0) * 16;
            int uvStride = (int)Math.ceil( (yStride / 2) / 16.0) * 16;
            int ySize = yStride * size.height;
            int uvSize = uvStride * size.height / 2;
            frameSize = ySize + uvSize * 2;
        }
        return frameSize;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(TAG, "surfaceDestroyed");
        try {
            camera.get().setPreviewCallback(null);
            camera.get().setPreviewDisplay(null);
            camera.get().stopPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //https://stackoverflow.com/questions/8435814/how-to-change-orientation-of-camera-preview-callback-buffer
        Camera.Parameters params = camera.getParameters();
        Camera.Size size = params.getPreviewSize();
        if (takeSnapshot) {
            byte[] i420 = ImageUtils.N21ToI420(data, size.width, size.height);
            byte[] nv21 = ImageUtils.I420ToN21(i420, size.width, size.height);
            ImageUtils.capturePreviewFrame(nv21, ImageFormat.NV21, new Size(size.width, size.height));
            takeSnapshot = false;
        }

        if (encoder != null) {
            long t0 = System.currentTimeMillis();
            byte[] i420 = ImageUtils.N21ToI420(data, size.width, size.height);
            if (captureRotation != 0) {
                i420 = ImageUtils.I420Rotate(i420, size.width, size.height, captureRotation);
            }
            //Log.v("onPreviewFrame", "preparing a video frame cost = " +
            //        (System.currentTimeMillis() - t0) + "ms");
            encoder.onSample(i420, i420.length);
        }
        camera.addCallbackBuffer(data);
    }

    void stop() {
        if (encoder != null) {
            encoder.stop();
            encoder = null;
        }
    }

    void startRecording(Encoder encoder) {
        this.encoder = encoder;
        encoder.start();
    }

    void setRotation(int rotation) {
        captureRotation = rotation;
    }

}
