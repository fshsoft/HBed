package com.java.health.care.bed.widget;

import static java.lang.Float.MAX_VALUE;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.java.health.care.bed.R;
import com.java.health.care.bed.model.DevicePacket;
import com.java.health.care.bed.util.DpUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;

public class MyEcgView extends View { //ECG心电

    public MyEcgView(Context context) {
        super(context);
        init(context);
    }

    public MyEcgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        //关闭硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mEcgColor = ContextCompat.getColor(mContext, R.color.holo_green_dark);
        smailGridWith = DpUtil.dp2px(mContext, smailGridWith);
        mMarginTop = DpUtil.dp2px(mContext, mMarginTop);
        mMarginButtom = DpUtil.dp2px(mContext, mMarginButtom);
        mPaint = new Paint();
        mPaint.setColor(mEcgColor);
        mPaint.setAntiAlias(true); //抗锯齿
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);// 设置画笔粗细
        mPaint.setDither(true); //防抖动
        mPath = new Path();

    }

    private static final String TAG = "我的心电图 ";
    /**
     * 所有的数据
     */
    private List<Integer> datas = new ArrayList<>();
    private List<Integer> dataEcg = new ArrayList<>();
    //心电图颜色
    private int mEcgColor;
    public void setRespColor()
    {
        mEcgColor = ContextCompat.getColor(mContext, R.color.color_red_FE647C);
        mPaint.setColor(mEcgColor);
    }
    private int mViewWidth;
    private int mViewHeight;
    /**
     * 数据绘制举例顶部的间距
     */
    private int mMarginTop = 5;
    /**
     * 距离底部的间距
     */
    private int mMarginButtom = 5;
    private Context mContext;
    private Paint mPaint;
    private Path mPath;
    /**
     * 小格子的宽度
     */
    private float smailGridWith = 4f;
    /**
     * 每个小格子的数据个数
     */
    private int itemGridDataNumber = 20;
    /**
     * 屏幕能够显示的所有的点的个数 注意这个值设置为int会导致这个数值不准确
     */
    private float maxSize = 0;


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
        maxSize = getMaxSize();
    }

    /**
     * 获取屏幕显示的最大的点个数
     *
     * @return
     */
    public float getMaxSize() {
        maxSize = (mViewWidth * (1.0f)) / (smailGridWith / (itemGridDataNumber * 1.0f));
        if (maxSize < 0) {
            maxSize = maxSize * (-1);
        }
//        Log.d(TAG, "getMaxSize: view 宽度[" + mViewWidth + "],每个点的宽度[" + (smailGridWith / (itemGridDataNumber * 1.0f)) + "],屏幕上最多显示 " + maxSize);
        return maxSize;
    }



    //当前点的位置下标
    private int indexs = 0;
    //这是一个标记数据，绘制的时候遇到这个数据就跳过
    private int ecgTag = 100000;

    private boolean isStop = false;

    public void setStop(boolean stop) {
        isStop = stop;
        invalidate();
    }

    public void refreshView(){
        if(!isStop){
            invalidate();
        }
    }

    public void clear() {
        datas.clear();
        invalidate();
    }
    //数据数组中存在的最大值和最小值
    private int minValue =0x7FFFFFFF;
    private int maxValue =0xFFFFFFFF;


    private int MAX_RRLIST_LEN = 1500;
    public void setMaxViewDatalen(int len)
    {
        MAX_RRLIST_LEN = len;
    }
    float xStep ;

    private Object lock = new int[1];

    /**
     * 添加数据
     *
     * @param data
     */

    public void addOneData(Integer data) {
        xStep = (float)mViewWidth/MAX_RRLIST_LEN;
        synchronized (lock) {
            datas.add(data);
            if (datas.size() > MAX_RRLIST_LEN) {

//                datas.remove(0);

            }


        }
    }

    public void addOneData1(Integer data) {
        xStep = (float)mViewWidth/MAX_RRLIST_LEN;
        synchronized (lock) {
            datas.add(data);
            if (datas.size() > MAX_RRLIST_LEN*2) {

                datas.clear();

            }

            drawHeartRefresh();


        }
    }

    int showIndex; //
    int intervalNumHeart =1500;
    private float[] data = new float[1500]; //一排显示的数据
    List<Integer> datass;
    private void drawHeartRefresh() {
        int nowIndex = datas.size(); //当前长度
        if (nowIndex < intervalNumHeart) {
            showIndex = nowIndex - 1;
        } else {
            showIndex = (nowIndex - 1) % intervalNumHeart;
        }
        for (int i = 0; i < intervalNumHeart; i++) {
            if (i > datas.size() - 1) {
                break;
            }
            if (nowIndex <= intervalNumHeart) {
                data[i] = datas.get(i);
            } else {
                int times = (nowIndex - 1) / intervalNumHeart;
                int temp = times * intervalNumHeart + i;
                if (temp < nowIndex) {
                    data[i] = datas.get(temp);
                }
            }
        }
         datass= new ArrayList<>();
        for(int i=0;i<data.length;i++){
            datass.add((int) data[i]);
        }
        Log.d("fshman====s", "data.length:"+data.length+"===datas.size()==="+datas.size()+"datass.size():"+datass.size());

    }



    @Override
    protected void onDraw(Canvas canvas) {
        if (datass == null) {
            return;
        }

        Log.d("fshman========", datass.size() + "===");
        if (!isStop && datass.size() > 0) {

            int maxValue = 0xF0000000;
            ;
            int minValue = 0x7FFFFFFF;

            float y = 0;
            float oldX = 0;

            synchronized (lock) {

                for (int i = 0; i < datass.size(); i++) {

                    if (maxValue < datass.get(i)) {
                        maxValue = datass.get(i);
                    }
                    if (minValue > datass.get(i)) {
                        minValue = datass.get(i);
                    }
                }
                float oldY = (maxValue != minValue) ? (datass.get(0) - minValue) * mViewHeight / (maxValue - minValue) : mViewHeight / 2;
                for (int i = 1; i < datass.size(); i++) {

                    y = (maxValue != minValue) ? mViewHeight - (datass.get(i) - minValue) * mViewHeight / (maxValue - minValue) : mViewHeight / 2;

                    canvas.drawLine(oldX, oldY, i * xStep, y, mPaint);
                    oldX = i * xStep;
                    oldY = y;

                }

            }

   /*         float nowX;
            float nowY;
        int maxValue = 0xF0000000;
            for (int i = 0; i < datass.size(); i++) {
                nowX = i ;
                float dataValue = datass.get(i);
//               if (dataValue > 0) {
//                    if (dataValue > maxValue * 0.8f) {
//                        dataValue = maxValue * 0.8f;
//                    }
//                } else {
//                    if (dataValue < -maxValue * 0.8f) {
//                        dataValue = -(maxValue * 0.8f);
//                    }
//                }
                nowY = mViewHeight / 2 - dataValue ;

                if (i - 1 == showIndex) {
                    mPath.moveTo(nowX, nowY);

                } else {
                    mPath.lineTo(nowX, nowY);
                }

            }

            canvas.drawPath(mPath, mPaint);*/

        }
    }

}