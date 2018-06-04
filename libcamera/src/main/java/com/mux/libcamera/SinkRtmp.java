package com.mux.libcamera;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;

import com.mux.libcamera.encoders.Encoder;
import com.github.faucamp.simplertmp.DefaultRtmpPublisher;
import com.github.faucamp.simplertmp.RtmpHandler;
import com.net.ossrs.yasea.SrsFlvMuxer;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class SinkRtmp implements Encoder.ISink, RtmpHandler.RtmpListener {
    private final static String TAG = "rtmpSink";
    private DefaultRtmpPublisher publisher;
    private HandlerThread rtmpThread;
    private Handler rtmpHandler;
    private int audioQueueSize = 0, videoQueueSize = 0;
    private SrsFlvMuxer muxer = new SrsFlvMuxer();
    private boolean audioTrackInited = false, videoTrackInited = false;

    public SinkRtmp(final String url, final Size videoSize) {
        RtmpHandler handler = new RtmpHandler(this);
        publisher = new DefaultRtmpPublisher(handler);
        rtmpThread = new HandlerThread("rtmpThread");
        rtmpThread.start();
        rtmpHandler = new Handler(rtmpThread.getLooper());
        rtmpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (publisher.connect(url)) {
                    boolean connected = publisher.publish("live");
                    if (connected) {
                        publisher.setVideoResolution(videoSize.getWidth(), videoSize.getHeight());
                    } else {
                        publisher.close();
                        publisher = null;
                    }
                }
            }
        });
    }

    @Override
    public void close() {
        rtmpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (publisher != null) {
                    publisher.close();
                }
                rtmpThread.quitSafely();
            }
        });
    }

    @Override
    public void onSample(final ByteBuffer buffer, final MediaFormat format,
            final MediaCodec.BufferInfo info) {
        String mime = format.getString(MediaFormat.KEY_MIME);
        final boolean isVideo = (mime.indexOf("video/") == 0);
        if (!audioTrackInited && !isVideo) {
            muxer.addTrack(isVideo, format);
            audioTrackInited = true;
        } else if (!videoTrackInited && isVideo) {
            muxer.addTrack(isVideo, format);
            videoTrackInited = true;
        }

        final SrsFlvMuxer.SrsFlvFrame frame = muxer.writeSampleData(isVideo, buffer, info);
        if (isVideo) {
            videoQueueSize++;
        } else {
            audioQueueSize++;
        }
        if (frame != null && videoQueueSize < 30) {
            rtmpHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (isVideo) {
                        publisher.publishVideoData(frame.flvTag.array(), frame.flvTag.size(),
                                frame.dts);
                        videoQueueSize--;
                    } else if (frame.isAudio()) {
                        publisher.publishAudioData(frame.flvTag.array(), frame.flvTag.size(),
                                frame.dts);
                        audioQueueSize--;
                    }
                    muxer.releaseFrame(frame);
                }
            });
        }
        Log.v(TAG,
                String.format("queue size, video=%d, audio=%d", videoQueueSize, audioQueueSize));
    }

    @Override
    public void onRtmpConnecting(String msg) {

    }

    @Override
    public void onRtmpConnected(String msg) {

    }

    @Override
    public void onRtmpVideoStreaming() {

    }

    @Override
    public void onRtmpAudioStreaming() {

    }

    @Override
    public void onRtmpStopped() {

    }

    @Override
    public void onRtmpDisconnected() {

    }

    @Override
    public void onRtmpVideoFpsChanged(double fps) {

    }

    @Override
    public void onRtmpVideoBitrateChanged(double bitrate) {

    }

    @Override
    public void onRtmpAudioBitrateChanged(double bitrate) {

    }

    @Override
    public void onRtmpSocketException(SocketException e) {

    }

    @Override
    public void onRtmpIOException(IOException e) {

    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {

    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {

    }
}
