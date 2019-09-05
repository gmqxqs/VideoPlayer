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
    MySelfGSYVideoPlayer.TimeCount timeCount;
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
        mOrientationUtils = new OrientationUtils(registrar.activity(), video);
        video.getBackButton().setVisibility(View.GONE);
        video.setFullHideStatusBar(true);
        video.setFullHideActionBar(true);
        mOrientationUtils.setEnable(false);
        timeCount = video.new TimeCount(10000,1000);
        urls = video.getUrls();
        //广告1
        urls.add(new MySelfGSYVideoPlayer.GSYADVideoModel("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4",
                "", MySelfGSYVideoPlayer.GSYADVideoModel.TYPE_AD));
        //正式内容1
        urls.add(new MySelfGSYVideoPlayer.GSYADVideoModel("http://v1.bjssmd.net/20190715/yXfbhmdr/index.m3u8",
                "测试视频", MySelfGSYVideoPlayer.GSYADVideoModel.TYPE_NORMAL));
        //video.setAdUp(urls, true, 0);
        video.setAutoFullWithSize(true);
        video.setShowFullAnimation(false);
         parent = (ViewGroup) video.getParent();
        //  videoPlayer.getSurface().setBackground(getResources().getDrawable(R.drawable.xxx1));
        video.measure(0,0);
        width = video.getMeasuredWidth(); ;
        height = video.getLayoutParams().height;

        video.setAutoFullWithSize(true);
        video.setShowFullAnimation(false);


       // video.setImageAdUrl("http://www.baidu.com/");
       video.setVideoAdUrl("http://xm.ganji.com/");
        //设置暂停图片广告的跳转地址
      //  video.setPauseAdImageUrl("https://www.suning.com/");
      //  video.getMadImageView().setBackground(registrar.activity().getResources().getDrawable(R.drawable.vedio_stop_ad));
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

                if(video.isIfCurrentIsFullscreen()){

                    if(video.isImageAd){
                        System.out.println("退出");
                        video.getLockScreen().setVisibility(View.GONE);
                        video.getControllerbottom().setVisibility(View.GONE);
                        video.getPlaystart().setVisibility(View.VISIBLE);
                        video.getCurrentTimeTextView().setVisibility(View.VISIBLE);
                        video.getTotalTimeTextView().setVisibility(View.VISIBLE);
                        video.getFullscreenButton().setVisibility(View.VISIBLE);

                        //backWindow(video);
                      //  mOrientationUtils.backToProtVideo();
                        //mOrientationUtils.resolveByClick();
                      //  backWindow(video);
                      //  backWindow(video);
                      //  video.clearFullscreenLayout();
                        return;
                    }

                } else{
                    if(timeCount != null){
                        timeCount.cancel();
                    }
                }
               // registrar.activity().onBackPressed();
                // videoPlayer.getMadImageView().setLayoutParams(new RelativeLayout.LayoutParams(150,118));
            }
        });
        video.getAdimage_skip().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeCount.onFinish();
                timeCount.cancel();
            }
        });

        //解决拖动视频会弹回来,因为ijk的FFMPEG对关键帧问题。
        VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);

        GSYVideoManager.instance().setOptionModelList(list);
        PlayerFactory.setPlayManager(Exo2PlayerManager.class);
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

    public void fullWindow(MySelfGSYVideoPlayer video){
        Constructor<MySelfGSYVideoPlayer> constructor;
        gsyVideoPlayer = video;


        registrar.activity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        video.mSystemUiVisibility = (registrar.activity()).getWindow().getDecorView().getSystemUiVisibility();
        video.getBackButton().setVisibility(View.VISIBLE);
        CommonUtil.hideSupportActionBar(registrar.activity(),true,true);


        /****************************/
        MySelfGSYVideoPlayer video2 = new MySelfGSYVideoPlayer(context);


        /***************************/



        ViewGroup vp = video.getViewGroup();
        video.removeVideo(vp, video.getFullId());

        FrameLayout.LayoutParams lpParent = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Color.BLUE);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(video.getWidth(), video.getHeight());
        System.out.println("vide.getParent1:"+video.getParent());
        video2 = video;
      //  video.cloneParams(video,video2);

        ViewGroup parent = (ViewGroup) video.getParent();
        if (parent != null) {
            parent.removeAllViews();
        }
        System.out.println("vide.getParent2:"+video.getParent());
        //video.removeAllViews();
        // frameLayout.addView(video2, lpParent);
        try{
            if(video.getParent() == null){
                vp.addView(video, lpParent);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
     /*   video.setVisibility(View.INVISIBLE);
        frameLayout.setVisibility(View.INVISIBLE);*/

    //    video.resolveFullVideoShow(registrar.activity(), video, frameLayout);
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


    public void startPlay() {
        //暂停广告，片头视频广告，片头图片广告的连接地址都为空
        if (TextUtils.isEmpty(video.getImageAdUrl()) && TextUtils.isEmpty(video.getVideoAdUrl()) && TextUtils.isEmpty(video.getPauseAdImageUrl())) {
            if (urls.size() >= 2) {
                urls.remove(urls.get(0));
            }
            video.setAdUp(urls, true, 0);
            video.startPlayLogic();
        }
        //片头视频广告，片头图片广告的连接地址都为空 ,暂停广告的连接地址不为空
        if (TextUtils.isEmpty(video.getImageAdUrl()) && TextUtils.isEmpty(video.getVideoAdUrl()) && !TextUtils.isEmpty(video.getPauseAdImageUrl())) {
            setPauseImage();
            if (urls.size() >= 2) {
                urls.remove(urls.get(0));
            }
            video.setAdUp(urls, true, 0);
            video.startPlayLogic();
        }
        //暂停广告，片头图片广告的连接地址都为wf空 ,片头视频广告的连接地址不为空
        if (TextUtils.isEmpty(video.getImageAdUrl()) && !TextUtils.isEmpty(video.getVideoAdUrl()) && TextUtils.isEmpty(video.getPauseAdImageUrl())) {
            video.setAdUp(urls, true, 0);
            video.startPlayLogic();
        }

        //暂停广告，片头视频广告的连接地址都为空 ,片头图片广告的连接地址不为空
        if (!TextUtils.isEmpty(video.getImageAdUrl()) && TextUtils.isEmpty(video.getVideoAdUrl()) && TextUtils.isEmpty(video.getPauseAdImageUrl())) {
            if (urls.size() >= 2) {
                urls.remove(urls.get(0));
            }
            setImageAd();
            timeCount.start();
        }

        //暂停广告，片头视频广告的连接地址都不为空 ,片头图片广告的连接地址为空
        if (TextUtils.isEmpty(video.getImageAdUrl()) && !TextUtils.isEmpty(video.getVideoAdUrl()) && !TextUtils.isEmpty(video.getPauseAdImageUrl())) {
            setPauseImage();
            video.setAdUp(urls, true, 0);
            video.startPlayLogic();
        }

        //暂停广告,片头图片广告的连接地址为不空，片头视频广告的连接地址为空
        if (!TextUtils.isEmpty(video.getImageAdUrl()) && TextUtils.isEmpty(video.getVideoAdUrl()) && !TextUtils.isEmpty(video.getPauseAdImageUrl())) {
            setPauseImage();
            if (urls.size() >= 2) {
                urls.remove(urls.get(0));
            }
            setImageAd();
            timeCount.start();
        }

        //片头图片广告，片头视频广告的连接地址不为空，暂停广告的连接地址为空
        if (!TextUtils.isEmpty(video.getImageAdUrl()) && !TextUtils.isEmpty(video.getVideoAdUrl()) && TextUtils.isEmpty(video.getPauseAdImageUrl())) {

            Random random = new Random();
            int result = random.nextInt(10);
            result += 1;
            if (result % 2 == 0) {
                setImageAd();
                if (urls.size() >= 2) {
                    urls.remove(urls.get(0));
                }
                video.isImageAd = true;
              //  video.setBackground(getResources().getDrawable(com.shuyu.gsyvideoplayer.R.drawable.xxx1));
                timeCount.start();
                // videoPlayer.setAdUp(urls, true, 0);
            } else {
                video.getSurface().setBackgroundColor(Color.BLACK);
                video.isImageAd = false;
                video.setAdUp(urls, true, 0);
                video.startPlayLogic();
            }
        }


        //片头图片广告，片头视频广告,暂停广告的连接地址不为空
        if (!TextUtils.isEmpty(video.getImageAdUrl()) && !TextUtils.isEmpty(video.getVideoAdUrl()) && !TextUtils.isEmpty(video.getPauseAdImageUrl())) {
            setPauseImage();
            Random random = new Random();
            int result = random.nextInt(10);
            result += 1;
            if (result % 2 == 0) {
                setImageAd();
                if (urls.size() >= 2) {
                    urls.remove(urls.get(0));
                }
                video.isImageAd = true;
               // video.setBackground(registrar.activity().getResources().getDrawable(com.shuyu.gsyvideoplayer.R.drawable.xxx1));
                timeCount.start();
                // videoPlayer.setAdUp(urls, true, 0);

            } else {
                video.getSurface().setBackgroundColor(Color.BLACK);
                video.isImageAd = false;
                video.setAdUp(urls, true, 0);
                video.startPlayLogic();
            }
        }

    }


}
