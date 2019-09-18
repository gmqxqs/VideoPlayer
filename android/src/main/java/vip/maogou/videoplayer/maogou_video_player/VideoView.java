package vip.maogou.videoplayer.maogou_video_player;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.WebView;

import android.widget.ImageView;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import static io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.platform.PlatformView;
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import com.google.android.exoplayer2.SeekParameters;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.cache.CacheFactory;
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager;
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
//import com.shuyu.gsyvideoplayer.video.MySelfGSYVideoPlayer;

import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
//import com.shuyu.gsyvideoplayer.video.MySelfGSYVideoPlayer;
//import com.shuyu.gsyvideoplayer.video.MySelfGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.LandscapeGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.MySelfGSYVideoPlayer;

import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.PauseImageAdWebViewActivity;
import com.shuyu.gsyvideoplayer.video.PortraitGSYVideoPlayer;

import java.util.ArrayList;

import java.util.List;


public class VideoView  extends Activity implements PlatformView, MethodCallHandler  {
    // private NormalGSYVideoPlayer video;
    private PortraitGSYVideoPlayer video;
    private OrientationUtils mOrientationUtils;
    private GSYVideoOptionBuilder gsyVideoOption;
    private final MethodChannel methodChannel;
    private final Registrar registrar;
    private WebView webView ;
    private Context context;
    private ViewGroup oldVp;
    private View oldF;
    ArrayList<PortraitGSYVideoPlayer.GSYADVideoModel> urls = new ArrayList<>();
    public ArrayList<Integer> imageUrls = new ArrayList<>();
    VideoView(Context context, int viewId, Object args, Registrar registrar) {
        System.out.println("初始化:");
        this.context = context;
        this.registrar = registrar;

        video = (PortraitGSYVideoPlayer) LayoutInflater.from(registrar.activity()).inflate(R.layout.jz_video, null);
       // video = (NormalGSYVideoPlayer) LayoutInflater.from(registrar.activity()).inflate(R.layout.jz_video, null);
        webView = new WebView(registrar.activity());
        oldVp = video.getViewGroup();
        oldF = oldVp.findViewById(video.getFullId());
        this.methodChannel = new MethodChannel(registrar.messenger(), "maogou_video_player_" + viewId);
        this.methodChannel.setMethodCallHandler(this);
    }

    private  ViewGroup parent;



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
             //   video.setProgressAndVideo(seek);

              /*  VideoOptionModel videoOptionModel =  new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "seek-at-start",seek);
                List<VideoOptionModel> list = new ArrayList<>();
                list.add(videoOptionModel);
                GSYVideoManager.instance().setOptionModelList(list);*/

                break;
            case "getDuration":
                System.out.println("getDuration:");
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
            mOrientationUtils.setEnable(false);
            mOrientationUtils.releaseListener();
        }
    }





    private void getMaogouVideo(String url, long seek) {
        String configRoot = context.getExternalFilesDir(null).getPath();
        System.out.println("configRoot:" + configRoot);
        mOrientationUtils = new OrientationUtils(registrar.activity(), video);
       // video.getBackButton().setVisibility(View.GONE);
        //增加封面
        imageUrls.add(R.drawable.xxx1);
        imageUrls.add(R.drawable.xxx2);
        ImageView imageView = new ImageView(registrar.activity());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(imageUrls.get(0));
        String tempUrl = "static://storage/emulated/0/Android/data/vip.maogou.videoplayer.maogou_video_player_example/files/d/1/62afc49f55985d7a550edc9f2864aa/d162afc49f55985d7a550edc9f2864aa/index.m3u8https://youku.com-ok-pptv.com/20190901/6570_497d32b7/index.m3u8";
        //  String tempUrl = "https://youku.com-ok-pptv.com/20190901/6570_497d32b7/index.m3u8";
        //  String tempUrl = "https://scontent-lga3-1.xx.fbcdn.net/v/t39.24130-6/10000000_194485571543767_1072296362069752098_n.mp4?_nc_cat=100&efg=eyJ2ZW5jb2RlX3RhZyI6Im9lcF9oZCJ9&_nc_oc=AQk0dFtDO98inb99mAaFjvRtWwPBRDPrIJIHT06Qw00mt_x9yRluXEFpgxuvE9XWZUA&_nc_ht=scontent-lga3-1.xx&oh=d051c96085dd5d01d64b1dcce0748d51&oe=5E080AFD";

        //     String url = "https://apd-1f573461e2849c2dff8de8011848088b.v.smtcdns.com/moviets.tc.qq.com/A-pfo_cZrdx-q2vFBqnpS4xcOM5Jb9Q8r8GgdIs8r8P0/uwMROfz2r5zAoaQXGdGnC2df644E7D3uP8M8pmtgwsRK9nEL/h3Ir07Asx9wg0_yDFrgKal0z4RSuVdCBIljI9eWOBAODkcEcQByGBAJMGF42Hkd48Gmf8rSFFPW5hh53XONL7LmdZpg9INujHPIJA-Y8gtK6W5P2XvMvBxKABOCWelv-mebHpqBSSTBUz6uDLGuHFhLeXt8dESEIn7_tnDs0CpU/z00310ev4nq.321002.ts.m3u8?ver=4";

        urls = video.getUrls();
        urls.add(new PortraitGSYVideoPlayer.GSYADVideoModel("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4",
                "",  PortraitGSYVideoPlayer.GSYADVideoModel.TYPE_NORMAL));
        urls.add(new PortraitGSYVideoPlayer.GSYADVideoModel("https://youku.com-ok-pptv.com/20190901/6570_497d32b7/index.m3u8",
                "",  PortraitGSYVideoPlayer.GSYADVideoModel.TYPE_NORMAL));
        video.setImageUrls(imageUrls);
        video.setRotateViewAuto(false);
        video.setAutoFullWithSize(true);
        video.setShowFullAnimation(false);
        video.setLooping(false);
       /* video.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏
                //  orientationUtils.resolveByClick();
                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                video.startWindowFullscreen(registrar.activity(), true, true);
            }
        });*/
        video.setIsTouchWiget(true);
        video.setLooping(false);
        video.setThumbImageView(imageView);
       /* video.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
*/

        //解决拖动视频会弹回来,因为ijk的FFMPEG对关键帧问题。
        VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1316);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 10240);
        list.add(videoOptionModel);
        videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        list.add(videoOptionModel);
        videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 50);
        list.add(videoOptionModel);
        videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"reconnect",5);
        list.add(videoOptionModel);

        GSYVideoManager.instance().setOptionModelList(list);
        GSYVideoManager.instance().setTimeOut(4000, true);
        PlayerFactory.setPlayManager(Exo2PlayerManager.class);
        System.out.println("urls:" + urls);
        video.setAdUp(urls,true,0);
        //   videoPlayer.setUp("http://v1.bjssmd.net/20190715/yXfbhmdr/index.m3u8",true,null,"");
        //   videoPlayer.startPlayLogic();
 ;
        video.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  videoPlayer.changeAdUIState();
                if (mOrientationUtils != null) {
                    mOrientationUtils.backToProtVideo();
                }

                if (GSYVideoManager.backFromWindowFull(registrar.activity())) {

                    return;
                }

                registrar.activity().onBackPressed();
                // videoPlayer.getMadImageView().setLayoutParams(new RelativeLayout.LayoutParams(150,118));
            }
        });

    //    video.startPlayLogic();

    }













}
