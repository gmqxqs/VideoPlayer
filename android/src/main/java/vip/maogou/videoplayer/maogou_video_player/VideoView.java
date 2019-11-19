package vip.maogou.videoplayer.maogou_video_player;

import android.content.Context;
import android.view.View;
import android.view.LayoutInflater;

import io.flutter.Log;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import static io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.platform.PlatformView;
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
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
    public ArrayList<MySelfGSYVideoPlayer.GSYADVideoModel> urls = new ArrayList<>();
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

        video.getBackButton().setVisibility(View.VISIBLE);
      /*  video.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  videoPlayer.changeAdUIState();
                System.out.println("点击返回");
                if (orientationUtils != null) {
                    orientationUtils.backToProtVideo();
                }
                if (GSYVideoManager.backFromWindowFull(registrar.activity())) {
                    return;
                }
                registrar.activity().onBackPressed();
                // videoPlayer.getMadImageView().setLayoutParams(new RelativeLayout.LayoutParams(150,118));
            }
        });*/
        video.setFullHideStatusBar(true);
        video.setFullHideActionBar(true);

        orientationUtils.setEnable(false);

        String tempUrl = "https://youku.com-ok-pptv.com/20190901/6570_497d32b7/index.m3u8";
        ArrayList<String> listUrl = new ArrayList<String>();
        listUrl = video.subString(tempUrl);
        System.out.println("list:" + listUrl);
        //     String url = "https://apd-1f573461e2849c2dff8de8011848088b.v.smtcdns.com/moviets.tc.qq.com/A-pfo_cZrdx-q2vFBqnpS4xcOM5Jb9Q8r8GgdIs8r8P0/uwMROfz2r5zAoaQXGdGnC2df644E7D3uP8M8pmtgwsRK9nEL/h3Ir07Asx9wg0_yDFrgKal0z4RSuVdCBIljI9eWOBAODkcEcQByGBAJMGF42Hkd48Gmf8rSFFPW5hh53XONL7LmdZpg9INujHPIJA-Y8gtK6W5P2XvMvBxKABOCWelv-mebHpqBSSTBUz6uDLGuHFhLeXt8dESEIn7_tnDs0CpU/z00310ev4nq.321002.ts.m3u8?ver=4";

        urls = video.getUrls();
        System.out.println("listUrl.size():" + listUrl.size());
        if(listUrl.size() >= 2){
            urls.add(new MySelfGSYVideoPlayer.GSYADVideoModel(listUrl.get(0),
                    "", MySelfGSYVideoPlayer.GSYADVideoModel.TYPE_DOWN));
            urls.add(new MySelfGSYVideoPlayer.GSYADVideoModel(listUrl.get(1),
                    "", MySelfGSYVideoPlayer.GSYADVideoModel.TYPE_NORMAL));
        } else{
            urls.add(new MySelfGSYVideoPlayer.GSYADVideoModel(listUrl.get(0),
                    "", MySelfGSYVideoPlayer.GSYADVideoModel.TYPE_NORMAL));
        }


  /*      gsyVideoOption = new GSYVideoOptionBuilder();
       // gsyVideoOption.setUrl("https://letv.com-v-letv.com/20180802/7097_e793eb8c/index.m3u8")
     //   gsyVideoOption.setUrl("https://api.maogou.vip/v1/video/share/35308?eid=282663")
        //外部辅助的旋转，帮助全屏
        String temp = "static://storage/emulated/0/Android/data/com.example.gsyvideoplayer/files/d/1/62afc49f55985d7a550edc9f2864aa/d162afc49f55985d7a550edc9f2864aa/index.m3u8https://youku.com-ok-pptv.com/20190901/6570_497d32b7/index.m3u8";
        gsyVideoOption.setUrl("https://youku.com-ok-pptv.com/20190901/6570_497d32b7/index.m3u8")
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
        }).build(video);*/


        video.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  videoPlayer.changeAdUIState();
                System.out.println("点击返回");
                if (orientationUtils != null) {
                    orientationUtils.backToProtVideo();
                }
                if (GSYVideoManager.backFromWindowFull(registrar.activity())) {
                    return;
                }
                registrar.activity().onBackPressed();
                // videoPlayer.getMadImageView().setLayoutParams(new RelativeLayout.LayoutParams(150,118));
            }
        });

        video.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orientationUtils.resolveByClick();
                video.startWindowFullscreen(registrar.activity(), true, true);
            }
        });

        //解决拖动视频会弹回来,因为ijk的FFMPEG对关键帧问题。
        VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"enable-accurate-seek", 1);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "allowed_media_types", "video"); //根据媒体类型来配置
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 20000);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1316);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "infbuf", 1);  // 无限读
        list.add(videoOptionModel);

        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");  // 无限读
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_flags", "prefer_tcp");  // 无限读
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"mediacodec",1);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"start-on-prepared",0);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"http-detect-range-support",0);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_CODEC,"skip_loop_filter",48);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_CODEC,"skip_loop_filter",8);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"analyzemaxduration",100);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"analyzemaxduration",1);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"probesize",4096);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"flush_packets",1);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"packet-buffering",0);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"framedrop",1);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"opensles",0);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"overlay-format",IjkMediaPlayer.SDL_FCC_RV32);
        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);
        PlayerFactory.setPlayManager(Exo2PlayerManager.class);
        //   CacheFactory.setCacheManager(ExoPlayerCacheManager.class);
        GSYVideoManager.onResume(false);
        //    GSYVideoType.setRenderType(GSYVideoType.SUFRACE);
        //    GSYVideoType.setRenderType(GSYVideoType.GLSURFACE);
        GSYVideoType.setRenderType(GSYVideoType.TEXTURE);
        IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);

        // GSYVideoType.enableMediaCodec();
        //GSYVideoType.enableMediaCodecTexture();

        //   IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_ERROR);
        video.setAdUp(urls,true,0);
        video.startPlayLogic();
    }
}
