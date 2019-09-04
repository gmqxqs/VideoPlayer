package vip.maogou.videoplayer.maogou_video_player;

import android.content.Context;
import android.view.View;
import android.view.LayoutInflater;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import static io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.platform.PlatformView;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
//import com.shuyu.gsyvideoplayer.video.MySelfGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.MySelfGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;

import java.util.ArrayList;
import java.util.List;

public class VideoView implements PlatformView, MethodCallHandler {
    //private NormalGSYVideoPlayer video;
    private MySelfGSYVideoPlayer video;
    private OrientationUtils orientationUtils;
    private GSYVideoOptionBuilder gsyVideoOption;
    private final MethodChannel methodChannel;
    private final Registrar registrar;

    VideoView(Context context, int viewId, Object args, Registrar registrar) {
        this.registrar = registrar;
        video = (MySelfGSYVideoPlayer) LayoutInflater.from(registrar.activity()).inflate(R.layout.jz_video, null);

        this.methodChannel = new MethodChannel(registrar.messenger(), "maogou_video_player_" + viewId);
        this.methodChannel.setMethodCallHandler(this);
    }

    @Override
    public View getView() {
        return video;
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method) {
            case "loadURL":
                String url = methodCall.argument("url").toString();
                int seek = methodCall.argument("seek");
                getMaogouVideo(url, seek);
                break;
            case "getCurrentPosition":
                result.success(video.getCurrentPositionWhenPlaying());
                break;
            case "pause":
                video.onVideoPause();
                break;
            case "resume":
                video.onVideoResume();
                break;
            case "seekTo":
                seek = Integer.parseInt(methodCall.arguments.toString());
                video.seekTo(seek);
                break;
            case "getDuration":
                result.success(video.getDuration());
                break;
            case "release":
                video.release();
                break;
            default:
                result.notImplemented();
        }
    }

    @Override
    public void dispose() {
        if(video != null) {
            video.onVideoPause();
            orientationUtils.setEnable(false);
            orientationUtils.releaseListener();
        }
    }

    private void getMaogouVideo(String url, long seek) {
        orientationUtils = new OrientationUtils(registrar.activity(), video);

        video.getBackButton().setVisibility(View.GONE);
        video.setFullHideStatusBar(true);
        video.setFullHideActionBar(true);

        orientationUtils.setEnable(false);

        gsyVideoOption = new GSYVideoOptionBuilder();
        gsyVideoOption.setUrl(url)
                .setCacheWithPlay(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setShowFullAnimation(false)
                .setNeedLockFull(true)
                .setSeekOnStart(seek)
                .setVideoAllCallBack(new GSYSampleCallBack() {
                    @Override
                    public void onPrepared(String url, Object... objects) {
                        super.onPrepared(url, objects);
                        //orientationUtils.setEnable(true);
                    }

                    @Override
                    public void onQuitFullscreen(String url, Object... objects) {
                        super.onQuitFullscreen(url, objects);
                        if (orientationUtils != null) {
                            orientationUtils.setEnable(false);
                            orientationUtils.backToProtVideo();
                        }
                    }
                }).setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    orientationUtils.setEnable(!lock);
                }
            }
        }).build(video);


        System.out.println("1111111111111111111111111111111111");


        video.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orientationUtils.resolveByClick();
                video.startWindowFullscreen(registrar.activity(), true, true);
            }
        });


        //解决拖动视频会弹回来,因为ijk的FFMPEG对关键帧问题。
        VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);
        video.startPlayLogic();
    }
}
