import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:maogou_video_player/maogou_video_player.dart';

void main() {
  const MethodChannel channel = MethodChannel('maogou_video_player');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await MaogouVideoPlayer.platformVersion, '42');
  });
}
