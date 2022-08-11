package com.java.health.care.bed.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;

import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

/**
 * Copyright (C), 2018-2019, 重庆咕点科技有限公司
 * FileName: ImageLoadUtils
 * Author: Vincent
 * Date: 2019/7/22 23:59
 * Description: 描述
 * History:
 */
public class ImageLoadUtils {

    private static ImageLoadUtils instance;
    private static final String TAG = "图片加载工具类";

    public static ImageLoadUtils getInstance() {
        if (instance == null) instance = new ImageLoadUtils();
        return instance;
    }

    /**
     * 加载图片
     *
     * @param mContext
     * @param url
     * @param imageView
     */
    public static void loadImg(Context mContext, String url, ImageView imageView) {
        Glide.with(mContext).load(url).into(imageView);
    }


    /**
     * 加载图片
     *
     * @param mContext
     * @param url
     * @param imageView
     */
    public static void loadImg(Context mContext, String url, int defaultImgId, ImageView imageView) {
//        Glide.with(mContext).load(url).into(imageView);
        Glide.with(mContext).
                load(url).error(defaultImgId) //异常时候显示的图片
                .placeholder(defaultImgId) //加载成功前显示的图片
                .fallback(defaultImgId) //url为空的时候,显示的图片
                .into(imageView);//在RequestBuilder 中使用自定义的ImageViewTarge
    }

    /**
     * 加载图片
     *
     * @param mContext
     * @param url
     * @param imageView
     */
    public static void loadImg(Context mContext, int failImgResId, String url, ImageView imageView) {

        RequestOptions requestOptions = new RequestOptions();
        //加载失败
        requestOptions.error(failImgResId);
        //占位图
        requestOptions.placeholder(failImgResId);
       /* ```		//缓存在sd
		requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        版本低话就直接在后面写.error 和.placeholder就行了
        */

        Glide.with(mContext).load(url).apply(requestOptions).into(imageView);
    }

    /**
     * 播放gif
     *
     * @param mContext
     * @param gifResId
     * @param imageView
     */
    public static void loadGifToImg(Context mContext, int gifResId, ImageView imageView) {
        loadGifToImg(mContext, gifResId, -1, imageView);
    }

    /**
     * 加载gif
     *
     * @param mContext
     * @param gifResId  gif资源
     * @param playCount 播放次数 -1 循环播放 0 默认次数 大于0 时为指定次数
     * @param imageView 显示gif的控件
     */
    public static void loadGifToImg(Context mContext, int gifResId, int playCount, ImageView imageView) {
        Glide.with(mContext).asGif().load(gifResId).listener(new RequestListener<GifDrawable>() {
            @Override
            public boolean onLoadFailed( GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                if (resource instanceof GifDrawable) {
                    //加载一次
                    ((GifDrawable) resource).setLoopCount(playCount);
                }
                return false;
            }
        }).into(imageView);
    }


    /**
     * 停止gif播放
     *
     * @param mContext
     * @param imageView
     */
    public static void stopGif(Context mContext, ImageView imageView) {
        if (imageView != null) {
            GifDrawable gifDrawable = (GifDrawable) imageView.getDrawable();
            if (gifDrawable.isRunning()) {
                gifDrawable.stop();
            } else {
//                gifDrawable.stop();
            }
        }
    }

    private ObjectAnimator animator;
    private ViewGroup.LayoutParams params;
    private float ratio;
    private int width;
    private int height;

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    /**
     * 加载GIF图片到ImageView 并放大到自身的一倍
     *
     * @param mContext
     * @param gifResId
     */
    public void loadingGifToImageViewForAmplification(Context mContext, int gifResId, long time, ImageView imageView) {
        params = imageView.getLayoutParams();
        params.width = (ScreenUtils.getScreenWidth(mContext) - DpUtil.dp2px(mContext, 60)) / 2;
        params.height = (ScreenUtils.getScreenWidth(mContext) - DpUtil.dp2px(mContext, 60)) / 2;
        imageView.setLayoutParams(params);
        Glide.with(mContext).asGif().load(gifResId).into(imageView);
        width = params.width;
        height = params.height;
        // mCurrentCircleRadius 表示为插值器在使用的值
        animator = ObjectAnimator.ofFloat(this, "ratio", 0, 1.0f);
        //animator = ObjectAnimator.ofFloat(this, "mTempCircleRadius", 0.01f, 1.0f,0.01f);//也可以写成这样
        animator.setDuration(time);
//        animator.setRepeatMode(ValueAnimator.RESTART);//无限循环模式 此方式无效
//        animator.setRepeatCount(ValueAnimator.INFINITE);//这个方式有效
        animator.addUpdateListener(animation -> {
            ratio = (float) animation.getAnimatedValue();
//            MyLogUtils.d(TAG,"onAnimationUpdate: "+ratio);
            params = imageView.getLayoutParams();
//            MyLogUtils.d(TAG,"loadGifToImg: "+params.width + "    "+params.height);
            params.width = (int) (width * (1 + ratio));
            params.height = (int) (height * (1 + ratio));
            imageView.setLayoutParams(params);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
//                animation.start();
            }
        });
        animator.start();
    }

    /**
     * 缩小到自身的一半
     *
     * @param mContext
     * @param gifResId
     * @param time
     * @param imageView
     */
    public void loadingGifToImageViewForShrink(Context mContext, int gifResId, long time, ImageView imageView) {
        params = imageView.getLayoutParams();
        params.width = ScreenUtils.getScreenWidth(mContext) - DpUtil.dp2px(mContext, 60);
        params.height = ScreenUtils.getScreenWidth(mContext) - DpUtil.dp2px(mContext, 60);
        imageView.setLayoutParams(params);
        Glide.with(mContext).asGif().load(gifResId).into(imageView);
        width = params.width;
        height = params.height;
        // mCurrentCircleRadius 表示为插值器在使用的值
        animator = ObjectAnimator.ofFloat(this, "ratio", 0, 0.5f);
        //animator = ObjectAnimator.ofFloat(this, "mTempCircleRadius", 0.01f, 1.0f,0.01f);//也可以写成这样
        animator.setDuration(time);
//        animator.setRepeatMode(ValueAnimator.RESTART);//无限循环模式 此方式无效
//        animator.setRepeatCount(ValueAnimator.INFINITE);//这个方式有效
        animator.addUpdateListener(animation -> {
            ratio = (float) animation.getAnimatedValue();
//            MyLogUtils.d(TAG,"onAnimationUpdate: "+ratio);
            params = imageView.getLayoutParams();
//            MyLogUtils.d(TAG,"loadGifToImg: "+params.width + "    "+params.height);
            params.width = (int) (width * (1 - ratio));
            params.height = (int) (height * (1 - ratio));
            imageView.setLayoutParams(params);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
//                animation.start();
            }
        });
        animator.start();
    }

}
