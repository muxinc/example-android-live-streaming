package com.mux.libcamera.utils;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.util.Size;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class ImageUtils {
    static {
        System.loadLibrary("yuvimage");
    }

    public static native byte[] N21ToI420(byte[] data, int width, int height);
    public static native byte[] I420ToN21(byte[] data, int width, int height);
    public static native byte[] I420Rotate(byte[] data, int width, int height, int degree);

    public static void capturePreviewFrame(byte[] data, int color, Size size) {
        YuvImage im = new YuvImage(data, color, size.getWidth(), size.getHeight(), null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        im.compressToJpeg(new Rect(0, 0, size.getWidth(), size.getHeight()), 50, baos);

        File file = new File(Environment.getExternalStorageDirectory(), "test.jpg");
        try {
            FileOutputStream output = new FileOutputStream(file);
            output.write(baos.toByteArray());
            output.flush();
            output.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
