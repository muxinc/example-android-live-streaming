package com.mux.libcamera.encoders;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public abstract class Encoder {
    public interface ISink {
        void close();
        void onSample(ByteBuffer buffer, MediaFormat format, MediaCodec.BufferInfo info);
    }

    public MediaCodec getCodec() {
        return mediaCodec;
    }

    protected MediaCodec mediaCodec;
    protected boolean pauseEncoding = false;

    public void pauseEncoding(boolean pause) {
        pauseEncoding = pause;
    }

    public void start() {
        if (mediaCodec != null)
            mediaCodec.start();
    }

    public void stop() {
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.reset();
            mediaCodec.release();
        }
        mediaCodec = null;
    }

    public abstract void onSample(byte[] data, int size);

    ISink sink;

    public void setSink(ISink sink) {
        this.sink = sink;
    }
}
