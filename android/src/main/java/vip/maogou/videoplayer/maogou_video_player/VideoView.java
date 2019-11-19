package vip.maogou.videoplayer.maogou_video_player;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.shuyu.gsyvideoplayer.video.MySelfGSYVideoPlayer;

import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.PauseImageAdWebViewActivity;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class VideoView  extends Activity implements PlatformView, MethodCallHandler  {
    // private NormalGSYVideoPlayer video;
    private MySelfGSYVideoPlayer video;
    private OrientationUtils mOrientationUtils;
    private GSYVideoOptionBuilder gsyVideoOption;
    private final MethodChannel methodChannel;
    private final Registrar registrar;
    private WebView webView ;
    private Context context;
    private ViewGroup oldVp;
    private View oldF;
    ArrayList<MySelfGSYVideoPlayer.GSYADVideoModel> urls = new ArrayList<>();
  //  MySelfGSYVideoPlayer.TimeCount timeCount;
    int width;
    int height;
    int count = 0;
    VideoView(Context context, int viewId, Object args, Registrar registrar) {
        System.out.println("初始化:");
        this.context = context;
        this.registrar = registrar;

        video = (MySelfGSYVideoPlayer) LayoutInflater.from(registrar.activity()).inflate(R.layout.jz_video, null);
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
        //String tempUrl = "static://storage/emulated/0/Android/data/vip.maogou.videoplayer.maogou_video_player_example/files/d/1/62afc49f55985d7a550edc9f2864aa/d162afc49f55985d7a550edc9f2864aa/index.m3u8https://youku.com-ok-pptv.com/20190901/6570_497d32b7/index.m3u8";
       // String tempUrl = "https://iqiyi.com-l-iqiyi.com/20190823/22550_41c5b03c/index.m3u8";
        String tempUrl = "https://iqiyi.com-l-iqiyi.com/20190303/21817_a6cd96be/index.m3u8";
        //  String tempUrl = "https://scontent-lga3-1.xx.fbcdn.net/v/t39.24130-6/10000000_194485571543767_1072296362069752098_n.mp4?_nc_cat=100&efg=eyJ2ZW5jb2RlX3RhZyI6Im9lcF9oZCJ9&_nc_oc=AQk0dFtDO98inb99mAaFjvRtWwPBRDPrIJIHT06Qw00mt_x9yRluXEFpgxuvE9XWZUA&_nc_ht=scontent-lga3-1.xx&oh=d051c96085dd5d01d64b1dcce0748d51&oe=5E080AFD";
        ArrayList<String> listUrl = new ArrayList<String>();
        listUrl = subString(tempUrl);
        System.out.println("list:" + listUrl);
        //     String url = "https://apd-1f573461e2849c2dff8de8011848088b.v.smtcdns.com/moviets.tc.qq.com/A-pfo_cZrdx-q2vFBqnpS4xcOM5Jb9Q8r8GgdIs8r8P0/uwMROfz2r5zAoaQXGdGnC2df644E7D3uP8M8pmtgwsRK9nEL/h3Ir07Asx9wg0_yDFrgKal0z4RSuVdCBIljI9eWOBAODkcEcQByGBAJMGF42Hkd48Gmf8rSFFPW5hh53XONL7LmdZpg9INujHPIJA-Y8gtK6W5P2XvMvBxKABOCWelv-mebHpqBSSTBUz6uDLGuHFhLeXt8dESEIn7_tnDs0CpU/z00310ev4nq.321002.ts.m3u8?ver=4";
        urls = video.getUrls();
        System.out.println("listUrl.size():" + listUrl.size());
        urls.add(new MySelfGSYVideoPlayer.GSYADVideoModel("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4",
                "", MySelfGSYVideoPlayer.GSYADVideoModel.TYPE_AD));
        if(listUrl.size() >= 2){
            urls.add(new MySelfGSYVideoPlayer.GSYADVideoModel(listUrl.get(0),
                    "", MySelfGSYVideoPlayer.GSYADVideoModel.TYPE_DOWN));
            urls.add(new MySelfGSYVideoPlayer.GSYADVideoModel(listUrl.get(1),
                    "", MySelfGSYVideoPlayer.GSYADVideoModel.TYPE_NORMAL));
        } else{
            urls.add(new MySelfGSYVideoPlayer.GSYADVideoModel(listUrl.get(0),
                    "", MySelfGSYVideoPlayer.GSYADVideoModel.TYPE_NORMAL));
        }
        video.setRotateViewAuto(false);
        video.setAutoFullWithSize(true);
        video.setShowFullAnimation(false);
        video.setLooping(false);
        video.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏
                //  orientationUtils.resolveByClick();
                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                video.startWindowFullscreen(registrar.activity(), true, true);
            }
        });
        video.setIsTouchWiget(true);
        video.setLooping(false);
        video.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //   videoPlayer.setImageAdUrl("http://www.baidu.com/");
       // video.setVideoAdUrl("http://xm.ganji.com/");
        //设置暂停图片广告的跳转地址
       // video.setPauseAdImageUrl("https://www.suning.com/");
        //点击暂停广告图片跳转
        video.getMadImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(registrar.activity(), PauseImageAdWebViewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Bundle bundle = new Bundle();
                bundle.putString("pauseImageAdUrl",video.getPauseAdImageUrl());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

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
        video.getMadImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(registrar.activity(), PauseImageAdWebViewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Bundle bundle = new Bundle();
                bundle.putString("pauseImageAdUrl",video.getPauseAdImageUrl());
                intent.putExtras(bundle);
                registrar.activity().startActivity(intent);
                video.onVideoPause();
            }
        });
        video.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏

                if(video.isImageAd){
                    System.out.println("图片背景");
                    mOrientationUtils.resolveByClick();
                   // mOrientationUtils.backToProtVideo();
                   /* if(count == 0){
                        count++;
                        fullWindow(video);
                    }*/

                   // displayAdImageStart();
                    video.startWindowFullscreen(registrar.activity(), true, true);
                  //  video.getCurrentPlayer().startPlayLogic();
                }else{
                    video.startWindowFullscreen(registrar.activity(), true, true);
                }

            /*    if(video.isImageAd){
                 //   video.startWindowFullscreen(registrar.activity(), true, true);
                    //mOrientationUtils.resolveByClick();
                    fullWindow(video);
                    video.startWindowFullscreen(registrar.activity(), true, true);

                } else {
                    video.startWindowFullscreen(registrar.activity(), true, true);
                }*/
            }
        });

        video.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  videoPlayer.changeAdUIState();
                System.out.println("点击返回");
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
        startPlay();
    }
    MySelfGSYVideoPlayer gsyVideoPlayer;
    public void backWindow(MySelfGSYVideoPlayer video){
        video.getBackButton().setVisibility(View.GONE);
        ViewGroup vp = video.getViewGroup();
        ViewGroup parent = (ViewGroup) video.getParent();
        FrameLayout.LayoutParams lpParent = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        vp.addView(gsyVideoPlayer, lpParent);

     /*   ViewGroup parent = (ViewGroup) video.getParent();
        System.out.println("vide.getParent1:"+video.getParent());
        if (parent != null) {
            parent.removeAllViews();

        }
        System.out.println("vide.getParent2:"+video.getParent());*/
        //video.removeAllViews();
        // frameLayout.addView(video, lpParent);
      /*  FrameLayout.LayoutParams lpParent = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        try{
            if(video.getParent() == null){
                vp.addView(video, lpParent);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
*/



    }


    public ArrayList<String> subString(String url){
        ArrayList<String> list = new ArrayList<>();
        String staticUrl = "";
        String httpUrl = "";
        if(url.startsWith("static")){
            staticUrl = url.substring(url.indexOf("/")+1,url.indexOf("http"));
        /*    String configRoot = contextFirst.getExternalFilesDir(null).getPath();
            staticUrl = configRoot + staticUrl;*/
            System.out.println("staticUrl:" +staticUrl);

            if(!staticUrl.equals("")){
                File file = new File(staticUrl);
                if(file.exists()){
                    list.add(staticUrl);
                    httpUrl = url.substring(url.indexOf("http"),url.length());
                    if(!httpUrl.equals("")){
                        list.add(httpUrl);
                    }
                } else{
                    httpUrl = url.substring(url.indexOf("http"),url.length());
                    System.out.println("httpUrl:" + httpUrl);
                    if(!httpUrl.equals("")){
                        list.add(httpUrl);
                    }
                    System.out.println("文件不存在");
                }

            }
        }
        if(url.startsWith("http")){
            list.add(url);
        }


        return list;
    }

    public void displayAdImageStart(){
        Bitmap bmp = BitmapFactory.decodeResource(registrar.activity().getResources(),R.drawable.xxx1);;                     //bitmap图片对象
        int primaryWidth;               //原图片宽
        int primaryHeight;              //原图片高
        double scaleWidth, scaleHeight;
        //原始大小
        primaryWidth = bmp.getWidth();
        primaryHeight = bmp.getHeight();
        //初始比例为1
        scaleWidth = scaleHeight = 1;
        //st.mAdImageView.setImageBitmap(bmp);
        // scale(2, 2);
        scaleWidth = scaleWidth * 3.0;  //缩放到原来的*倍
        scaleHeight = scaleHeight * 3.0;

        Matrix matrix = new Matrix();   //矩阵，用于图片比例缩放
        matrix.postScale((float)scaleWidth, (float)scaleHeight);    //设置高宽比例（三维矩阵）

        //缩放后的BitMap
        Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, primaryWidth, primaryHeight, matrix, true);

        //重新设置BitMap
        BitmapDrawable newBitmap = new BitmapDrawable(newBmp);
        video.getSurface().setBackgroundDrawable(newBitmap);
    }


    //设置暂停图片的方法
    public void setPauseImage(){
        if(video != null){
            video.bmp = BitmapFactory.decodeResource(registrar.activity().getResources(),R.drawable.vedio_stop_ad);
            video.displayAd();
        }
    }

    //设置片头图片的方法
    public void setImageAd(){
        video.isImageAd = true;
        video.getSurface().setBackground(registrar.activity().getResources().getDrawable(com.shuyu.gsyvideoplayer.R.drawable.xxx1));
   }


    public void startPlay(){
        //暂停广告，片头视频广告，片头图片广告的连接地址都为空
        if(TextUtils.isEmpty(video.getVideoAdUrl()) && TextUtils.isEmpty(video.getPauseAdImageUrl())){
            if(urls.size() >= 2){
                urls.remove(urls.get(0));
            }
            video.setAdUp(urls, true, 0);
            video.startPlayLogic();
        }
        //片头视频广告，片头图片广告的连接地址都为空 ,暂停广告的连接地址不为空
        if(TextUtils.isEmpty(video.getVideoAdUrl()) && !TextUtils.isEmpty(video.getPauseAdImageUrl())){
            setPauseImage();
            if(urls.size() >= 2){
                urls.remove(urls.get(0));
            }
            video.setAdUp(urls, true, 0);
            video.startPlayLogic();
        }
        //暂停广告，片头图片广告的连接地址都为空 ,片头视频广告的连接地址不为空
        if( !TextUtils.isEmpty(video.getVideoAdUrl()) && TextUtils.isEmpty(video.getPauseAdImageUrl())){
            video.setAdUp(urls, true, 0);
            video.startPlayLogic();
        }


        //暂停广告，片头视频广告的连接地址都不为空 ,片头图片广告的连接地址为空
        if(!TextUtils.isEmpty(video.getVideoAdUrl()) && !TextUtils.isEmpty(video.getPauseAdImageUrl())){
            setPauseImage();
            video.setAdUp(urls, true, 0);
            video.startPlayLogic();
        }






    }


}
