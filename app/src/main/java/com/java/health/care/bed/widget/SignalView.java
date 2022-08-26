package com.java.health.care.bed.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.java.health.care.bed.R;
import com.java.health.care.bed.model.BPDevicePacket;
import com.java.health.care.bed.model.DevicePacket;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author fsh
 * @date 2022/08/24 15:12
 * @Description
 */
public class SignalView extends SurfaceView implements Callback {
    private static final String TAG = "SignalView";

    private static final int SCALE_INTENSITY = 10;

    // 每32个点存一次，以提高绘图频率（必须是96的约数）
    private static int BUFFER_SPAN = 32;

    // 每100毫秒重绘一次
    private static final int REDRAW_INTERVAL = 106;

    // 可绘制区域的宽度
    private int width = 0;

    // 可绘制区域的高度
    private int height = 0;

    // 画笔
    private Paint paint = null;

    private volatile float VoltageH = (float) 4.0; //4;//mv
    private volatile float VoltageL = (float) -2.0; //-2;//mv
    private volatile float ThrVoltageH = (float) 3.0; //4;//mv
    private volatile float ThrVoltageL = (float) -1.0; //-2;//mv
    //    private static final float GridH = (float) 0.5;
    private static final float GridH = (float) 1.0;

    private float Vperpx = 0;
    // 缓存数据包
    private Queue<Byte> bufferedZeroBias = new LinkedList<>();
    private Queue<Byte> bufferedEcgBase = new LinkedList<>();

    //ECG心电
    private Queue<Byte> bufferedEcg = new LinkedList<>();
    private Queue<Byte> newBufferedEcg = new LinkedList<>();
    private Queue<Byte> storeBufferedEcg = new LinkedList<>();

    //呼吸信号
    private Queue<Integer> bufferedBre = new LinkedList<>();
    private Queue<Integer> newBufferedBre = new LinkedList<>();
    private Queue<Integer> storeBufferedBre = new LinkedList<>();

    private Object lockObj = new Object();

    private Timer timer = null;

    // 是否已缓存够数据包来进行绘制
    private boolean enoughToDraw = false;
    private boolean enoughToDraw1 = false;
    private boolean fullToDraw = false;
    private boolean fullToDraw1 = false;

    public void setDataEcg(DevicePacket packet) {
        if (packet.data.length != 96) {
            return;
        }
        // 每32个点存一次，以提高绘图频率（必须是96的约数）96/32
        int times =96/32;
        for (int i = 0; i < times; i++) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (lockObj) {
                enoughToDraw = false;
                fullToDraw = false;
                if (storeBufferedEcg.size() >= 2 * width) {
                    enoughToDraw = true;
                }
                if (bufferedEcg.size() >= 2 * width) {
                    fullToDraw = true;
                }
                if (enoughToDraw) {

                    storeBufferedEcg.clear();
                }
                if (fullToDraw) {
                    bufferedZeroBias.poll();
                    bufferedEcgBase.poll();
                    //ECG
                    for (int j = i * BUFFER_SPAN; j < (i + 1) * BUFFER_SPAN; j++) {

                        storeBufferedEcg.add(packet.data[j]);
                    }
                    for (int a = 0; a < storeBufferedEcg.size(); a++) {
                        bufferedEcg.poll();
                    }


                    newBufferedEcg.clear();
                    newBufferedEcg.addAll(storeBufferedEcg);
                    newBufferedEcg.addAll(bufferedEcg);

                } else {
                    //ECG
                    for (int j = i * BUFFER_SPAN; j < (i + 1) * BUFFER_SPAN; j++) {

                        storeBufferedEcg.add(packet.data[j]);
                    }
                    newBufferedEcg.clear();
                    newBufferedEcg.addAll(storeBufferedEcg);
                }

                bufferedEcg.clear();
                bufferedEcg.addAll(newBufferedEcg);

                bufferedZeroBias.add(packet.ZeroBias);//0mv偏置值
                bufferedEcgBase.add(packet.EcgBase);//基准电压
            }
        }
    }

    public void setDataResp(DevicePacket packet) {
        if (packet.data.length != 96) {
            return;
        }
        int times = 96/32;
        for (int i = 0; i < times; i++) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (lockObj) {
                enoughToDraw1 = false;
                fullToDraw1 = false;
                if (storeBufferedBre.size() >= 2 * width) {
                    enoughToDraw1 = true;
                }
                if (bufferedBre.size() >= 2 * width) {
                    fullToDraw1 = true;
                }
                if (enoughToDraw1) {

                    storeBufferedBre.clear();
                }
                if (fullToDraw1) {
                    bufferedBre.poll();

                    for (int j = i * BUFFER_SPAN; j < (i + 1) * BUFFER_SPAN; j++) {

                        storeBufferedBre.add(packet.irspData[j]);
                    }

                    for (int y = 0; y < storeBufferedBre.size(); y++) {
                        bufferedBre.poll();
                    }

                    newBufferedBre.clear();
                    newBufferedBre.addAll(storeBufferedBre);
                    newBufferedBre.addAll(bufferedBre);


                } else {

                    for (int j = i * BUFFER_SPAN; j < (i + 1) * BUFFER_SPAN; j++) {

                        storeBufferedBre.add(packet.irspData[j]);
                    }

                    newBufferedBre.clear();
                    newBufferedBre.addAll(storeBufferedBre);
                }


                bufferedBre.clear();
                bufferedBre.addAll(newBufferedBre);

            }
        }
    }


    public SignalView(Context context, AttributeSet attrs) {
        super(context, attrs); this.getHolder().addCallback(this);

    }
    // 清除所有数据
    public void clear() {
        synchronized (lockObj) {
            enoughToDraw = false;
            fullToDraw = false;
            bufferedEcg.clear();
            newBufferedEcg.clear();
            storeBufferedEcg.clear();

            bufferedBre.clear();
            newBufferedBre.clear();
            storeBufferedBre.clear();

            bufferedZeroBias.clear();
            bufferedEcgBase.clear();
        }
    }

    int[] bre = null;

    private void drawDataResp() {

        synchronized (lockObj) {
            int breLength = bufferedBre.size();
            int i = 0;
            //呼吸信号
            bre = new int[breLength];
            for (int b : bufferedBre) {
                if (i == breLength)
                    break;
                bre[i++] = b;
            }
        }


    }
    private void drawDataEcg() {

        byte[] ecg = null;
        byte zeroBias = 0, ecgBase = 0;
        synchronized (lockObj) {
            int ecgLength = bufferedEcg.size();

            // 有数据时才考虑显示心电图
            if (ecgLength > 0) {

                ecg = new byte[ecgLength];
                int i = 0;
                for (byte b : bufferedEcg) {
                    if (i == ecgLength)
                        break;
                    ecg[i++] = b;
                }
                if (bufferedZeroBias.size() > 0) {
                    zeroBias = bufferedZeroBias.peek();
                    Log.i("---zeroBias", zeroBias + "");
                }
                if (bufferedEcgBase.size() > 0) {
                    ecgBase = bufferedEcgBase.peek();
                    Log.i("---ecgBase", ecgBase + "");
                }
            }
        }
        drawDataResp();
        drawSignal(ecg,zeroBias,ecgBase);
    }
    public void drawSignal(byte[] ecg,byte zeroBias, byte ecgBase){
        Canvas canvas = this.getHolder().lockCanvas();
        if (canvas != null && width > 0 && height > 0) {
            drawEcg(canvas,ecg,zeroBias, ecgBase);
//            drawResp(bre);
        }
        this.getHolder().unlockCanvasAndPost(canvas);

    }

    //绘制心电图
    private void drawEcg(Canvas canvas,byte[] ecg,byte zeroBias, byte ecgBase){
        byte[] temp = ecg; // 读取ecg数据，防止长时间占用
        if(temp==null){
            return;
        }
        int ecgHeight = height / 2;
        int maxLength = 2 * width;
        int length = temp.length < maxLength ? temp.length : maxLength;
        if (temp != null) {
            int redEnd = storeBufferedEcg.size() - 1;
            int y1 = 0, y2 = 0;
            float voltageTmp = 0;
            for (int i = 2; i < length; i += 2) {
                if (i > redEnd && i < redEnd + 10) {
                    continue;
                }

                voltageTmp = (float)((((int)temp[i - 2] & 0xff) - ((int)(zeroBias) & 0xff)) * 1.0 / ((int)(ecgBase & 0xff)));
                y1 = ecgHeight - (int)(( 10*voltageTmp - VoltageL)  * ecgHeight / (VoltageH - VoltageL));
                voltageTmp = (float)(((int)(temp[i] & 0xff) - (int)(zeroBias & 0xff)) * 1.0 / ((int)(ecgBase & 0xff)));
                y2 = ecgHeight - (int)((10*voltageTmp - VoltageL)  * ecgHeight / (VoltageH - VoltageL));

                canvas.drawLine((i / 2 - 1 ), y1-200, (i / 2 ), y2-200, paint);
                Log.d("fffff====",i+"==="+y1+"===="+y2);

            }
        }


    }
    //绘制呼吸
    private void drawResp(int[] bre) {

    }

    private class DrawSignalTask extends TimerTask {

        @Override
        public void run() {
            drawDataEcg();
        }

    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);// 设置画笔粗细
        paint.setColor(getResources().getColor(R.color.linegreen));
        timer = new Timer();
        timer.schedule(new DrawSignalTask(), 0, REDRAW_INTERVAL);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (timer != null) {
            try {
                timer.cancel();
            } catch (Exception e) {

            }
        }
    }
}
