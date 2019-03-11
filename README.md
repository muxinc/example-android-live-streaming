# Deprecated

Moving forward, Mux is consolidating examples in a single location: https://github.com/muxinc/examples. See that repo for a an updated Android Live streaming example (based on javacv) that should be simpler.

# example-android-live-streaming

An example app for live streaming from an Android device using the [Mux](http://mux.com) live streaming service, written in Java and C++ [![GitHub license](https://img.shields.io/badge/license-MIT-lightgrey.svg)](https://github.com/muxinc/example-ios-live-streaming/blob/master/LICENSE)

## Introduction

This repo includes two main components:
- A sample application, located under `/app`, which includes a simple UI to configure and stream your live stream.
- A lower-level moduled, under `/libcamera`, providing all of the live capture and live streaming functionality.

The `libcamera` module supports the following features:
- Integration with [Android's Camera 2 API](https://developer.android.com/reference/android/hardware/camera2/package-summary)
- Landscape and portrait video capture
- Application pause and resume
- Force screen to stay on while capture is active
- Configuration of video and audio encoding settigns
- Minimum Android API level 9

The `libcamera` module is built on, and encapsulates, the following third-party libraries:
- [SimpleRtmp - Client-side RTMP library for Java](https://github.com/faucamp/simplertmp) - com.github.faucamp.simplertmp
- [Yet Another Stream Encoder for Android](https://github.com/begeekmyfriend/yasea) - com.net.ossrs.yasea
- Color space conversion: [libyuv](https://chromium.googlesource.com/libyuv/libyuv/)

## Quick Start

Clone this repo locally, and then load the application from the root directory in Android Studio 3.0+.

Run the application. There will be buttons at the bottom of the screen to start/stop live capture.

### Components

- `app` contains the demo application

- `libcamera` is the functional module

## Resources

* [Mux](http://mux.com)
* [Mux Live Streaming Documentation](https://docs.mux.com/v1/docs/live-streaming)
* [Demuxed](http://demuxed.com)
* [Yasea](https://github.com/begeekmyfriend/yasea/tree/master/library/src/main/java/net/ossrs/yasea) FLC stream muxer
* [libyuv](https://chromium.googlesource.com/libyuv/libyuv/) Color space converter
* [SimpleRTMP](https://github.com/faucamp/SimpleRtmp) RTMP Streaming SDK

## License

`example-android-live-streaming` and `MuxLive` are available under the MIT license, see the [LICENSE](https://github.com/muxinc/example-android-live-streaming/blob/master/LICENSE) file for more information.
