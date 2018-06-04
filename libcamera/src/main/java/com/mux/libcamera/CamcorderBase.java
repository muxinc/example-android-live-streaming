package com.mux.libcamera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.util.Size;
import android.view.Surface;
import android.view.View;

import com.mux.libcamera.encoders.Encoder;

import java.io.IOException;
import java.util.List;

public abstract class CamcorderBase {
    protected Encoder videoEncoder, audioEncoder;
    protected List<Size> supportedCaptureSizes;
    protected int captureSizeIndex = 0;

    public static CamcorderBase CreateCamera(int cameraApiLevel, Activity ctx, int cameraId,
            OnCameraOpenListener listener) throws CameraAccessException {
        CamcorderBase camcorder = null;
        if (cameraApiLevel == 1) {
            camcorder = new com.mux.libcamera.camera1.Camcorder(ctx, cameraId, listener);
        } else {
            CameraManager manager = (CameraManager) ctx.getSystemService(
                    Context.CAMERA_SERVICE);
            if (manager != null) {
                String[] list = manager.getCameraIdList();
                if (list.length > cameraId) {
                    camcorder = new com.mux.libcamera.camera2.Camcorder(ctx, list[cameraId],
                            listener);
                }
            }
        }
        int rotation = ctx.getWindowManager().getDefaultDisplay().getRotation();
        switch(rotation) {
            case Surface.ROTATION_0:
                ctx.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case Surface.ROTATION_90:
                ctx.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case Surface.ROTATION_180:
                ctx.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
            case Surface.ROTATION_270:
                ctx.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
        }
        return camcorder;
    }

    public void release(Activity ctx) {
        ctx.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    public abstract View getPreview();

    public abstract void startRecord(String streamKey) throws IOException;

    public abstract void stopRecord();

    public void pauseRecord(boolean pause) {
        if (videoEncoder != null) {
            videoEncoder.pauseEncoding(pause);
        }
        if (audioEncoder != null) {
            audioEncoder.pauseEncoding(pause);
        }
    }

    public List<Size> getSupportedCaptureSizes() {
        return supportedCaptureSizes;
    }

    public void setCaptureSizeIndex(int index) {
        captureSizeIndex = index;
    }

    public abstract void takeSnapshot();

    public interface OnCameraOpenListener {
        void onOpened(boolean result);
    }
}
