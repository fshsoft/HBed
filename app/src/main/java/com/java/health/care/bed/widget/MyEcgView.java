package com.java.health.care.bed.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.java.health.care.bed.R;
import com.java.health.care.bed.util.DpUtil;

import java.util.ArrayList;
import java.util.List;

public class MyEcgView extends View {
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
        mEcgColor = ContextCompat.getColor(mContext, R.color.color_red_FE647C);
        smailGridWith = DpUtil.dp2px(mContext, smailGridWith);
        mMarginTop = DpUtil.dp2px(mContext, mMarginTop);
        mMarginButtom = DpUtil.dp2px(mContext, mMarginButtom);
        mPaint = new Paint();
        mPaint.setColor(mEcgColor);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mPath = new Path();

    }

    private static final String TAG = "我的心电图 ";
    /**
     * 所有的数据
     */
    private List<Integer> datas = new ArrayList<>();
    //心电图颜色
    private int mEcgColor;
    public void setRespColor()
    {
        mEcgColor = ContextCompat.getColor(mContext, R.color.color_blue_7EC0EE);
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

    /**
     * 添加数据数组
     *
     * @param datas
     */
    public void addAllData(List<Integer> datas) {
        this.datas.addAll(datas);
        if (this.datas.size() > maxSize) {
            for (int i = 0; i < this.datas.size() - maxSize; i++) {
                this.datas.remove(0);
            }
        }
        invalidate();
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
    float xStep = mViewWidth/MAX_RRLIST_LEN;

    private Object lock = new int[1];
    /**
     * 添加数据
     *
     * @param data
     */
    public void addOneData(Integer data,int min,int max) {

        xStep = (float)mViewWidth/MAX_RRLIST_LEN;
        synchronized (lock) {
            datas.add(data);
            if (datas.size() > MAX_RRLIST_LEN) {
                datas.remove(0);
            }
        }
    }

    /**
     * 心电图内容高度
     */
    private float mEcgDataHeight;
    private float mBaseLine;

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isStop && datas.size()>0) {

            int maxValue = 0xF0000000;;
            int minValue = 0x7FFFFFFF;

            canvas.drawColor(Color.WHITE);// 清除画布

            Paint mPaint = new Paint();
            mPaint.setColor(mEcgColor);// 画笔色
            mPaint.setStrokeWidth(2);// 设置画笔粗细

            float y = 0;
            float oldX = 0;
            synchronized (lock){

                for (int i = 0; i < datas.size(); i++) {
                    if (maxValue < datas.get(i)) {
                        maxValue = datas.get(i);
                    }
                    if (minValue > datas.get(i)) {
                        minValue = datas.get(i);
                    }
                }
                float oldY = (maxValue != minValue) ? (datas.get(0) - minValue) * mViewHeight / (maxValue - minValue) : mViewHeight / 2;
                for (int i = 1; i < datas.size(); i++) {// 绘画波形

                    y =(maxValue != minValue) ? mViewHeight -(datas.get(i) - minValue) * mViewHeight / (maxValue - minValue) : mViewHeight / 2;

                    canvas.drawLine(oldX, oldY, i * xStep, y, mPaint);
                    //canvas.drawLine(oldX, oldY, i*xStep, i+1, mPaint);
                    oldX = i * xStep;
                    oldY = y;
                    //System.out.println("oldX:" + oldX +"oldY:"  + oldY);
                }
            }
        }
    }
    float item1Y;
    float dataY;
    /**
     * 把数据转化为对应的坐标  1大格表示的数据值为0.5毫伏，1毫伏= 200(数据) 1大格表示的数据 = 0.5 *200 1小格表示的数据 = 0.5*200/5 = 20
     * 1 小格的数据 表示为20 1小格的高度为16
     *
     * @param data
     * @return
     */
  /*  private float change(Integer data) {
        //数值1在view上表示的高度
        item1Y = mEcgDataHeight /(maxValue - minValue);
        *//*if (data > 0) {
            itemY = mMarginTop + (mEcgDataHeight / 2.0f * (200 - data) / 200.0f);
        } else {
            itemY = mBaseLine - data / 200.0f * mEcgDataHeight / 2.0f;
        }*//*

        dataY = mViewHeight - mMarginButtom - item1Y * (data - minValue);
        return dataY;
    }
*/


  /*  private float change(Integer data) {
        //数值1在view上表示的高度
        item1Y = mEcgDataHeight /(maxValue - minValue);
        if (data > 0) {
            dataY = mMarginTop + (maxValue - data) * item1Y;
        } else {
            dataY = mViewHeight - mMarginButtom - (mEcgDataHeight/2.0f + data * item1Y);
        }

//        dataY = mViewHeight - mMarginButtom - item1Y * (data - minValue);
        return dataY;
    }*/

    private float change(Integer data) {
        //数值1在view上表示的高度
        item1Y = mEcgDataHeight /(maxValue - minValue);
        if (data > 0) {
            dataY = mMarginTop + (maxValue - data) * item1Y;

        } else {
            dataY = mViewHeight - mMarginButtom + (minValue - data) * item1Y;
        }


//        dataY = mViewHeight - mMarginButtom - item1Y * (data - minValue);
        return dataY;
    }
}