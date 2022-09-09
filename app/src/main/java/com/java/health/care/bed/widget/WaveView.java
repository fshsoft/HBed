package com.java.health.care.bed.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.java.health.care.bed.model.DevicePacket;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @Description: 波形图绘制控件
 * @Author: GiftedCat
 * @Date: 2021-01-01
 */
public class WaveView extends View {

    private final String NAMESPACE = "http://schemas.android.com/apk/res-auto";
    private Object lockObj = new Object();
    private Queue<Short> bufferedEcg = new LinkedList<>();
    private Queue<Short> newBufferedEcg = new LinkedList<>();
    private Queue<Short> storeBufferedEcg = new LinkedList<>();
    // 是否已缓存够数据包来进行绘制
    private boolean enoughToDraw = false;
    private boolean fullToDraw = false;

    /**
     * 宽高
     */
    private float mWidth = 0, mHeight = 0;
    /**
     * 网格画笔
     */
    private Paint mLinePaint;
    /**
     * 数据线画笔
     */
    private Paint mWavePaint;
    /**
     * 线条的路径
     */
    private Path mPath;

    /**
     * 保存已绘制的数据坐标
     */
    private float[] dataArray;

    /**
     * 数据最大值，默认-20~20之间
     */
    private float MAX_VALUE = 127;
    /**
     * 线条粗细
     */
    private float WAVE_LINE_STROKE_WIDTH = 3;
    /**
     * 波形颜色
     */
    private int waveLineColor = Color.parseColor("#EE4000");
    /**
     * 当前的x，y坐标
     */
    private float nowX, nowY;

    private float startY;

    /**
     * 线条的长度，可用于控制横坐标
     */
    private int WAVE_LINE_WIDTH = 10;
    /**
     * 数据点的数量
     */
    private int row;

    private int draw_index;

    private boolean isRefresh;



    /**
     * 网格线条的粗细
     */
    private final int GRID_LINE_WIDTH = 2;

    private Timer timer = null;
    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);
        timer = new Timer();
        timer.schedule(new DrawSignalTask(), 500, 10);
    }

    private void init(AttributeSet attrs) {
        MAX_VALUE = attrs.getAttributeIntValue(NAMESPACE, "max_value", 20);
        WAVE_LINE_WIDTH = attrs.getAttributeIntValue(NAMESPACE, "wave_line_width", 10);
        WAVE_LINE_STROKE_WIDTH = attrs.getAttributeIntValue(NAMESPACE, "wave_line_stroke_width", 3);



        String wave_line_color = attrs.getAttributeValue(NAMESPACE, "wave_line_color");
        if (wave_line_color != null && !wave_line_color.isEmpty()) {
            waveLineColor = Color.parseColor(wave_line_color);
        }


        String wave_background = attrs.getAttributeValue(NAMESPACE, "wave_background");
        if (wave_background != null && !wave_background.isEmpty()) {
            setBackgroundColor(Color.parseColor(wave_background));
        }


        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(GRID_LINE_WIDTH);
        /** 抗锯齿效果*/
        mLinePaint.setAntiAlias(true);

        mWavePaint = new Paint();
        mWavePaint.setStyle(Paint.Style.STROKE);
        mWavePaint.setColor(waveLineColor);
        mWavePaint.setStrokeWidth(WAVE_LINE_STROKE_WIDTH);
        /** 抗锯齿效果*/
        mWavePaint.setAntiAlias(true);

        mPath = new Path();

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        /** 获取控件的宽高*/
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();


        /** 根据线条长度，最多能绘制多少个数据点*/
        row = (int) (mWidth / WAVE_LINE_WIDTH);
        dataArray = new float[row];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawWaveLineLoop(canvas);
        draw_index += 1;
        if (draw_index >= row) {
            draw_index = 0;
        }
    }



    /**
     * 循环模式绘制折线
     *
     * @param canvas
     */
    private void drawWaveLineLoop(Canvas canvas) {
        drawPathFromDatas(canvas, (row - 1) - draw_index > 8 ? 0 : 8 - ((row - 1) - draw_index), draw_index);
        drawPathFromDatas(canvas, Math.min(draw_index + 8, row - 1), row - 1);
    }

    /**
     * 取数组中的指定一段数据来绘制折线
     *
     * @param start 起始数据位
     * @param end   结束数据位
     */
    private void drawPathFromDatas(Canvas canvas, int start, int end) {
        mPath.reset();
        startY = mHeight / 2 - dataArray[start] * (mHeight / (MAX_VALUE * 2));
        mPath.moveTo(start * WAVE_LINE_WIDTH, startY);
        for (int i = start + 1; i < end + 1; i++) {
            if (isRefresh) {
                isRefresh = false;
                return;
            }
            nowX = i * WAVE_LINE_WIDTH;
            float dataValue = dataArray[i];
            /** 判断数据为正数还是负数  超过最大值的数据按最大值来绘制*/
            if (dataValue > 0) {
                if (dataValue > MAX_VALUE) {
                    dataValue = MAX_VALUE;
                }
            } else {
                if (dataValue < -MAX_VALUE) {
                    dataValue = -MAX_VALUE;
                }
            }
            nowY = mHeight / 2 - dataValue * (mHeight / (MAX_VALUE * 2));
            mPath.lineTo(nowX, nowY);
        }
        canvas.drawPath(mPath, mWavePaint);
    }



    /**
     * 添加新的数据
     */
    public void showLine(float line) {
        dataArray[draw_index] = line;
//        postInvalidate();
        invalidate();
    }






    public WaveView setMaxValue(int max_value) {
        this.MAX_VALUE = max_value;
        return this;
    }

    public WaveView setWaveLineWidth(int width) {
        draw_index = 0;
        this.WAVE_LINE_WIDTH = width;
        row = (int) (mWidth / WAVE_LINE_WIDTH);
        isRefresh = true;
        dataArray = new float[row];
        return this;
    }

    public WaveView setWaveLineStrokeWidth(int width) {
        this.WAVE_LINE_WIDTH = width;
        return this;
    }

    public WaveView setWaveLineColor(String colorString) {
        this.waveLineColor = Color.parseColor(colorString);
        return this;
    }


    public WaveView setWaveBackground(String colorString) {
        setBackgroundColor(Color.parseColor(colorString));
        return this;
    }


    private class DrawSignalTask extends TimerTask {

        @Override
        public void run() {
            drawData();
        }
    }

    float[] floats;
    public void setData(DevicePacket packet) {


        if (packet.secgdata.length != 96) {
            return;
        }
        for (int i = 0; i < 3; i++) {

            synchronized (lockObj) {
                enoughToDraw = false;
                fullToDraw = false;
                if (storeBufferedEcg.size() >= 2 * mWidth) {
                    enoughToDraw = true;
                }
                if (bufferedEcg.size() >= 2 * mWidth) {
                    fullToDraw = true;
                }
                if (enoughToDraw) {
                    storeBufferedEcg.clear();
                }
                if (fullToDraw) {
                    for (int j = i * 32; j < (i + 1) * 32; j++) {

                        storeBufferedEcg.add(packet.secgdata[j]);
                    }
                    for (int a = 0; a < storeBufferedEcg.size(); a++) {
                        bufferedEcg.poll();
                    }
                    newBufferedEcg.clear();
                    newBufferedEcg.addAll(storeBufferedEcg);
                    newBufferedEcg.addAll(bufferedEcg);
                } else {
                    for (int j = i * 32; j < (i + 1) * 32; j++) {

                        storeBufferedEcg.add(packet.secgdata[j]);
                    }
                    newBufferedEcg.clear();
                    newBufferedEcg.addAll(storeBufferedEcg);
                }
                bufferedEcg.clear();
                bufferedEcg.addAll(newBufferedEcg);
            }

        }

    }

    private void drawData(){
        synchronized (lockObj) {

            int length = bufferedEcg.size();
            floats = new float[length];
            int i = 0;
            for (short b : bufferedEcg) {
                showLine(b);
//                if (i == length) break;
//                floats[i++] = b;

            }

//            for (float f : floats) {
//                showLine(f);
//            }

        }
    }

}

