import 'package:flutter/material.dart';
import 'dart:ui';
import 'package:flutter/services.dart';

import 'package:maogou_video_player/maogou_video_player.dart';

void main() {
  runApp(MyApp());
  SystemChrome.setPreferredOrientations([
      DeviceOrientation.portraitUp,
      DeviceOrientation.portraitDown
  ]);
} 

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  var viewPlayerController;
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    var x = 0.0;
    var y = 0.0;
    var width = window.physicalSize.width - 100;
    var height = width * 9.0 / 16.0;
    print("=====$width");
    print("=====$height");

    MaogouVideoPlayer videoPlayer = new MaogouVideoPlayer(
        onCreated: onViewPlayerCreated,
        onChanged: onChanged,
        x: x,
        y: y,
        width: width,
        height: height);

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text("hello world"),
        ),
        body: Container(
          child: videoPlayer,
          width: width,
          height: height,
        ),
        persistentFooterButtons: <Widget>[
          FlatButton(
            child: Text('getProgress'),
            onPressed: getProgress,
          ),
          FlatButton(
            onPressed: pause,
            child: Text('pause'),
          ),
          FlatButton(
            onPressed: resume,
            child: Text('resume'),
          ),
          FlatButton(
            onPressed: seekTo,
            child: Text('seekTo'),
          ),
        ],
      ),
    );
  }

  void onViewPlayerCreated(viewPlayerController) {
    this.viewPlayerController = viewPlayerController;
    this.viewPlayerController.loadURL(
        //"https://zy.kubozy-youku-163-aiqi.com/20190430/7601_ade539c3/1000k/hls/index.m3u8", 0);
"https://apissources.bamasoso.com/video/rvz3YaO8Gbxq/0e5fdab9c4935093877390e6db94443c.mp4", 0);
  }

  void onChanged(viewPlayerController) {
    print("===onChanged===");
  }

  void getProgress() {
    print(this.viewPlayerController.getCurrentPosition());
  }

  void pause() {
    this.viewPlayerController.pause();
    //this.viewPlayerController.loadURL("https://zy.kubozy-youku-163-aiqi.com/20190430/7601_ade539c3/1000k/hls/index.m3u8", 0);
  }

  void resume() {
    this.viewPlayerController.resume();
    //this.viewPlayerController.loadURL("https://apissources.bamasoso.com/video/rvz3YaO8Gbxq/0e5fdab9c4935093877390e6db94443c.mp4", 0);
  }

  void seekTo() {
    this.viewPlayerController.seekTo(18);
  }
}
