# example-android-live-streaming

An example app for live streaming from an Android device using the [Mux](http://mux.com) live streaming service, written in Java and C++ [![GitHub license](https://img.shields.io/badge/license-MIT-lightgrey.svg)](https://github.com/muxinc/example-ios-live-streaming/blob/master/LICENSE)

## Welcome

Our sample app is a simple UI. All the live capture and streaming functionality is encapsulated inside the libcamera module.

The libcamera module support the following features

- Support Android Camera 2 API.

- Support capture in landscape or portrait mode

- Support app pause and resume

- Screen stays on while in Capture

- Support configuration on video and audio encoding

- Minimum Android API level 9

The libcamera also encapsulated the following open source libraries

- RTMP network protocol library: com.github.faucamp.simplertmp

- Elementary streams muxing to FLV stream library: com.net.ossrs.yasea

- Color space converter library: libyuv

## Quick Start

### Sample Project Setup

After clone the source code, use Android Studio 3.0+ to load from the root directory.

### Components

- `app` is the demo app

- `libcamera` is the functional module.

### Build and Run.

Run the app. There will be buttons at the bottom of screen to start/stop live capture



## Documentation

You can find [the docs here](https://muxinc.github.io/example-android-live-streaming).

## Resources

* [Mux](http://mux.com)
* [Mux Live Streaming Documentation](https://docs.mux.com/v1/docs/live-streaming)
* [Demuxed](http://demuxed.com)
* [Yasea](https://github.com/begeekmyfriend/yasea/tree/master/library/src/main/java/net/ossrs/yasea) FLC stream muxer
* [libyuv](https://chromium.googlesource.com/libyuv/libyuv/) Color space converter
* [SimpleRTMP](https://github.com/faucamp/SimpleRtmp) RTMP Streaming SDK

## License

`example-android-live-streaming` and `MuxLive` are available under the MIT license, see the [LICENSE](https://github.com/muxinc/example-android-live-streaming/blob/master/LICENSE) file for more information.
