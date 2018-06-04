package com.mux.libcamera.encoders;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.util.Size;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EncoderVideoH264 extends Encoder {

    public EncoderVideoH264(Size videoSize, boolean async) throws IOException {
        MediaCodecList mcl = new MediaCodecList(MediaCodecList.ALL_CODECS);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", videoSize.getWidth(),
                videoSize.getHeight());
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 10000000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                async ? MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface :
                        MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        //mediaFormat.setInteger(MediaFormat.KEY_ROTATION, 0);
        String encoderName = mcl.findEncoderForFormat(mediaFormat);
        mediaCodec = MediaCodec.createByCodecName(encoderName);
        if (async) {
            mediaCodec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index,
                        @NonNull MediaCodec.BufferInfo info) {
                    ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                    MediaFormat bufferFormat = codec.getOutputFormat(index);
                    //Log.v("TAG", "Output " + info.size + " bytes at " + info.presentationTimeUs);
                    if (sink != null && !pauseEncoding) {
                        sink.onSample(outputBuffer, bufferFormat, info);
                    }
                    codec.releaseOutputBuffer(index, false);
                }

                @Override
                public void onError(@NonNull MediaCodec codec,
                        @NonNull MediaCodec.CodecException e) {

                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec codec,
                        @NonNull MediaFormat format) {
                }
            });
        }
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        bufferFormat = mediaCodec.getOutputFormat();
    }

    private MediaFormat bufferFormat;

    @Override
    public void onSample(byte[] data, int size) {
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        int offset = 0;
        while(size != 0) {
            int inputBufferId = mediaCodec.dequeueInputBuffer(25000);
            if (inputBufferId >= 0) {
                ByteBuffer inputBuffers = mediaCodec.getInputBuffer(inputBufferId);
                if (inputBuffers != null) {
                    int capacity = inputBuffers.capacity();
                    int cb = size > capacity ? capacity : size;
                    inputBuffers.put(data, offset, cb);
                    offset += cb;
                    size -= cb;
                    mediaCodec.queueInputBuffer(inputBufferId, 0, cb,
                            System.nanoTime() / 1000, 0);
                }
            }
        }

        int outputBufferId = mediaCodec.dequeueOutputBuffer(info, 25000);
        if (outputBufferId >= 0) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(
                    outputBufferId);
            if (outputBuffer != null && sink != null) {
                sink.onSample(outputBuffer, bufferFormat, info);
            }
            mediaCodec.releaseOutputBuffer(outputBufferId, false);
        } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            // Subsequent data will conform to new format.
            bufferFormat = mediaCodec.getOutputFormat();
        }
    }
}
