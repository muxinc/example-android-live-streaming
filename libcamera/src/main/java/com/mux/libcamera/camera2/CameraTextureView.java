package com.mux.libcamera.camera2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;
import android.view.Surface;
import android.view.TextureView;

import com.mux.libcamera.encoders.Encoder;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ViewConstructor")
class CameraTextureView extends TextureView {
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private Encoder encoder;

    CameraTextureView(final Context context, final String cameraId,
            final Camcorder.OnCameraOpenListener listener) {
        super(context);
        setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, final int width,
                    final int height) {
                final CameraManager manager = (CameraManager) context.getSystemService(
                        Context.CAMERA_SERVICE);
                if (manager == null) {
                    listener.onOpened(false);
                    return;
                }
                try {
                    manager.openCamera(cameraId, new CameraDevice.StateCallback() {

                        @Override
                        public void onOpened(@NonNull CameraDevice camera) {
                            cameraDevice = camera;
                            int rotation = ((Activity)context).getWindowManager().getDefaultDisplay().getRotation();
                            Matrix matrix = new Matrix();
                            RectF viewRect = new RectF(0, 0, width, height);
                            float centerX = viewRect.centerX();
                            float centerY = viewRect.centerY();
                            if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
                                RectF bufferRect = new RectF(0, 0, height, width);
                                bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
                                matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
                                matrix.postRotate(90 * (rotation - 2), centerX, centerY);
                                setTransform(matrix);
                            }

                            startCapture(true);
                            listener.onOpened(true);
                        }

                        @Override
                        public void onDisconnected(@NonNull CameraDevice camera) {
                            cameraDevice.close();
                            cameraDevice = null;
                        }

                        @Override
                        public void onError(@NonNull CameraDevice camera, int error) {
                            cameraDevice = null;
                            listener.onOpened(false);
                        }
                    }, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    private void startCapture(final boolean preview) {
        try {
            if (captureSession != null) {
                captureSession.close();
            }
            SurfaceTexture texture = getSurfaceTexture();
            texture.setDefaultBufferSize(getWidth(), getHeight());
            final CaptureRequest.Builder captureBuild = cameraDevice.createCaptureRequest(
                    preview ? CameraDevice.TEMPLATE_PREVIEW : CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            captureBuild.addTarget(previewSurface);
            if (!preview && encoder != null) {
                Surface recorderSurface = encoder.getCodec().createInputSurface();
                surfaces.add(recorderSurface);
                captureBuild.addTarget(recorderSurface);
            }
            cameraDevice.createCaptureSession(surfaces,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            try {
                                captureSession = cameraCaptureSession;
                                captureBuild.set(CaptureRequest.CONTROL_MODE,
                                        CameraMetadata.CONTROL_MODE_AUTO);
                                captureSession.setRepeatingRequest(captureBuild.build(),
                                        null,
                                        null);

                                if (!preview && encoder != null) {
                                    encoder.start();
                                }
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    void release() {
        if (encoder != null) {
            encoder.stop();
            encoder = null;
        }
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    void stop() {
        if (encoder != null) {
            encoder.stop();
            encoder = null;
        }
        startCapture(true);
    }

    void startRecording(Encoder encoder) {
        this.encoder = encoder;
        startCapture(false);
    }
}
