package com.mux.libcamera.camera1;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;

import com.mux.libcamera.CamcorderBase;
import com.mux.libcamera.SinkRtmp;
import com.mux.libcamera.encoders.Encoder;
import com.mux.libcamera.encoders.EncoderAudioAAC;
import com.mux.libcamera.encoders.EncoderVideoH264;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class Camcorder extends CamcorderBase {
    private final static String TAG = "CAM1";
    private Camera camera;
    private CameraPreview cameraPreview;
    private int captureRotation = 0;
    private Encoder.ISink mSink;

    public Camcorder(Context ctx, int cameraId, OnCameraOpenListener listener) {
        camera = Camera.open(cameraId);
        cameraPreview = new CameraPreview(ctx, camera);
        initPreview();
        setPreviewOrientation(ctx, cameraId);
        listener.onOpened(true);
    }

    private void initPreview() {
        Camera.Parameters params = camera.getParameters();
        Camera.Size preferredPreviewSize = params.getPreferredPreviewSizeForVideo();
        Log.v(TAG, String.format("preferredPreviewSize %d x %d", preferredPreviewSize.width, preferredPreviewSize.height));

        List<Camera.Size> supportedVideoSizes = params.getSupportedVideoSizes();
        supportedCaptureSizes = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for(Camera.Size size : supportedVideoSizes) {
            Size capSize = new Size(size.width, size.height);
            supportedCaptureSizes.add(capSize);
            sb.append(String.format(Locale.getDefault(), "(%d x %d), ", size.width, size.height));
        }
        Log.v(TAG, String.format("supportedVideoSizes are, %s", sb.toString()));
        Collections.sort(supportedCaptureSizes, new Comparator<Size>() {
            @Override
            public int compare(Size p1, Size p2) {
                return p1.getWidth() - p2.getWidth();
            }
        });

        List<Camera.Size> supportedPreviewSizes = params.getSupportedPreviewSizes();
        sb = new StringBuilder();
        for(Camera.Size size : supportedPreviewSizes) {
            sb.append(String.format(Locale.getDefault(), "(%d x %d), ", size.width, size.height));
        }
        Log.v(TAG, String.format("supportedPreviewSizes are, %s", sb.toString()));

        setCaptureSizeIndex(0);
    }

    private void setPreviewOrientation(Context ctx, int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = ((Activity)ctx).getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        Log.v(TAG, String.format("device rotation %d, cam orientation %d, preview adjusted orientation %d",
                rotation * 90, info.orientation, result));
        captureRotation = result;
        cameraPreview.setRotation(result);
        camera.setDisplayOrientation(result);
    }

    @Override
    public void release(Activity ctx) {
        super.release(ctx);
        cameraPreview.stop();
        if (camera != null) {
            camera.release();
        }
        camera = null;
    }

    @Override
    public View getPreview() {
        return cameraPreview;
    }

    @Override
    public void startRecord(Activity activity, String streamKey) throws IOException {
        super.startRecord(activity, streamKey);
        Size capturedSize = supportedCaptureSizes.get(captureSizeIndex);
        if (captureRotation == 90 || captureRotation == 270)
            capturedSize = new Size(capturedSize.getHeight(), capturedSize.getWidth());
        videoEncoder = new EncoderVideoH264(capturedSize, false);
        audioEncoder = new EncoderAudioAAC(EncoderAudioAAC.SupportedSampleRate[7],
                MediaCodecInfo.CodecProfileLevel.AACObjectLC,
                EncoderAudioAAC.SupportBitRate[2]);
        mSink = new SinkRtmp("rtmp://live-staging.mux.com/mux/" + streamKey, capturedSize);
        //mSink = new SinkMp4Muxer("test.mp4", 1);
        videoEncoder.setSink(mSink);
        audioEncoder.setSink(mSink);
        audioEncoder.start();
        cameraPreview.startRecording(videoEncoder);
    }
    
    @Override
    public void stopRecord(Activity activity) {
        super.stopRecord(activity);
        cameraPreview.stop();
        audioEncoder.stop();
        mSink.close();
    }

    @Override
    public void setCaptureSizeIndex(int index) {
        super.setCaptureSizeIndex(index);
        Camera.Parameters params = camera.getParameters();
        Size capturedSize = supportedCaptureSizes.get(captureSizeIndex);
        params.setPreviewSize(capturedSize.getWidth(), capturedSize.getHeight());
        camera.setParameters(params);
    }

    @Override
    public void takeSnapshot() {
        cameraPreview.takeSnapshot = true;
    }
}
