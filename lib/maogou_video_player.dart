import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

typedef void MaogouVideoPlayerCreatedCallback(
    MaogouVideoPlayerController controller);

typedef void MaogouVideoPlayerChangeCallback(
  MaogouVideoPlayerController controller);

class MaogouVideoPlayerController {
  MethodChannel _channel;
  int currentProgress;
  int currentPosition;

  MaogouVideoPlayerController.init(int id) {
    _channel = new MethodChannel('maogou_video_player_$id');
  }

  Future<void> loadURL(String url, int seek) async {
    return _channel.invokeMethod('loadURL', {'url': url, 'seek': seek ?? 0});
  }

  Future<int> getCurrentPosition() async {
    return await _channel.invokeMethod('getCurrentPosition');
  }

  Future<void> pause() async {
    return _channel.invokeMethod('pause');
  }

  Future<void> resume() async {
    return _channel.invokeMethod('resume');
  }

  Future<void> seekTo(int seek) async {
    return _channel.invokeMethod('seekTo', seek);
  }

  Future<double> getCurrentProgress() async {
    if (defaultTargetPlatform == TargetPlatform.android) {
      int pos = await _channel.invokeMethod('getCurrentPosition');
      int dur = await _channel.invokeMethod('getDuration');
      return ((pos / dur) * 100).floorToDouble();
    } else {
      double pro = await _channel.invokeMethod('progress');
      return pro * 100;
    }
  }

  Future<void> release() async {
    return _channel.invokeMethod('release');
  }
}

class MaogouVideoPlayer extends StatefulWidget {
  final MaogouVideoPlayerCreatedCallback onCreated;
  final MaogouVideoPlayerChangeCallback onChanged;
  final x;
  final y;
  final width;
  final height;

  MaogouVideoPlayer({
    Key key,
    @required this.onCreated,
    @required this.onChanged,
    @required this.x,
    @required this.y,
    @required this.width,
    @required this.height,
  });

  @override
  State<StatefulWidget> createState() => _VideoPlayerState();
}

class _VideoPlayerState extends State<MaogouVideoPlayer> with WidgetsBindingObserver{
  MaogouVideoPlayerController ctrl;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    switch (state) {
      case AppLifecycleState.resumed:
        widget.onChanged(ctrl);
        ctrl.resume();
        break;
      case AppLifecycleState.paused:
        widget.onChanged(ctrl);
        ctrl.pause();
        break;

      default:
    }
  }

  @override
  void dispose() {
    widget.onChanged(ctrl);
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
        behavior: HitTestBehavior.opaque,
        child: nativeView());
  }

  nativeView() {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return AndroidView(
        viewType: 'plugins.maogou_video_player/view',
        onPlatformViewCreated: onPlatformViewCreated,
        creationParams: <String, dynamic>{
          "x": widget.x,
          "y": widget.y,
          "width": widget.width,
          "height": widget.height,
        },
        creationParamsCodec: const StandardMessageCodec(),
        gestureRecognizers: <Factory<OneSequenceGestureRecognizer>>[
          new Factory<OneSequenceGestureRecognizer>(
              () => new EagerGestureRecognizer(),
          ),
        ].toSet(),
      );
    } else {
      return UiKitView(
        viewType: 'plugins.maogou_video_player/view',
        onPlatformViewCreated: onPlatformViewCreated,
        creationParams: <String, dynamic>{
          "x": widget.x,
          "y": widget.y,
          "width": widget.width,
          "height": widget.height,
        },
        creationParamsCodec: const StandardMessageCodec(),
        gestureRecognizers: <Factory<OneSequenceGestureRecognizer>>[
          new Factory<OneSequenceGestureRecognizer>(
                () => new EagerGestureRecognizer(),
          ),
        ].toSet(),
      );
    }
  }

  Future<void> onPlatformViewCreated(id) async {
    if (widget.onCreated == null) {
      return;
    }

    if (ctrl == null) {
      ctrl = new MaogouVideoPlayerController.init(id);
    }

    widget.onCreated(ctrl);
  }
}
