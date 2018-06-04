package com.mux.libcamera;


import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;

import com.mux.libcamera.encoders.Encoder;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Hashtable;

public class SinkMp4Muxer implements Encoder.ISink {
    private MediaMuxer muxer;
    private Hashtable<String, Integer> trackTable = new Hashtable<>();
    private int trackCount;

    public SinkMp4Muxer(String name, int trackCount) {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), name);
            muxer = new MediaMuxer(file.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            this.trackCount = trackCount;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        muxer.stop();
        muxer.release();
    }

    @Override
    public void onSample(ByteBuffer buffer, MediaFormat format, MediaCodec.BufferInfo info) {
        int trackIndex;
        String mime = format.getString(MediaFormat.KEY_MIME);
        if (!trackTable.containsKey(mime)) {
            trackIndex = muxer.addTrack(format);
            trackTable.put(mime, trackIndex);
            if (trackTable.size() == trackCount)
                muxer.start();
            else
                return;
        }
        if (trackTable.size() == trackCount) {
            trackIndex = trackTable.get(mime);
            muxer.writeSampleData(trackIndex, buffer, info);
        }
    }
}
