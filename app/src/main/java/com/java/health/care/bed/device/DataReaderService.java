package com.java.health.care.bed.device;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.java.health.care.bed.constant.Constant;
import com.java.health.care.bed.constant.ImplementConfig;
import com.java.health.care.bed.util.ByteUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * @author fsh
 * @date 2022/08/04 10:51
 * @Description 服务
 */
public class DataReaderService extends Service {

    public static final String TAG = DataReaderService.class.getSimpleName();
    private static final int BLE_DATA_MSG = 3;
    /**
     *  BleReader 读取数据线程处理
     */
    private HandlerThread readTh;
    private Looper readLooper;
    private Handler readHandler;
    private volatile long mLastReadTime = 0;
    private static final int PACKET_LENGTH = 2000;
    private static final int BUFFER_WINDOW = PACKET_LENGTH * 3;
    private List<Byte> dataBuffer = new LinkedList<>();
    private Queue<byte[]> rdQueue = new LinkedList<>();
    private Object rdSync = new Object();

    /**
     *  Processor  数据处理
     */
    private HandlerThread prcTh;
    private final int prcThPacketQueMsg = 5;
    private final int prcThExit = 6;
    private Looper prcLooper;
    private Handler prcHandler;




    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart()");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);

        /**
         * BleReader
         */
        readTh = new HandlerThread("BleReader",
                Process.THREAD_PRIORITY_BACKGROUND);
        readTh.start();
        readLooper = readTh.getLooper();
        readHandler = new BleReadHandler(readLooper);


        /**
         * Processor
         */
        prcTh = new HandlerThread("Processor",
                Process.THREAD_PRIORITY_BACKGROUND);
        prcTh.start();
        prcLooper = prcTh.getLooper();
        prcHandler = new ProcessorHandler(prcLooper);

        Log.d(TAG, "onCreate()");


    }

    /**
     * BleReader
     */
    private final class BleReadHandler extends Handler{

        public BleReadHandler(Looper looper) {
            super(looper);
        }

        private void readData(byte[] buffer){
            mLastReadTime = System.currentTimeMillis();
            int num = buffer.length;
            if (num>0){
                mLastReadTime = System.currentTimeMillis();
                {
                    if (dataBuffer.size() < BUFFER_WINDOW) {
                        for (int i = 0; i < num; i++) {
                            dataBuffer.add(buffer[i]);
                        }
                    }
                }
            }
            readPacketsTlv();
            mLastReadTime = System.currentTimeMillis();
        }

        private void readPacketsTlv() {
            byte head1 = 0;
            byte head2 = 0;

            {
                int length = dataBuffer.size();
                int packetLen = 0;
                while (length > 8) {
                    boolean flag = false;
                    boolean exit = false;
                    for (int i = 0; i < length - 8; i++) {
                        head1 = dataBuffer.get(0);
                        head2 = dataBuffer.get(1);

                        if ((head1 == ImplementConfig.TLV_CODE_SYS_HEAD
                                && (head2 == ImplementConfig.TLV_VERSION_ONE
                                || head2 == ImplementConfig.TLV_VERSION_TWO
                                || head2 == ImplementConfig.TLV_VERSION_THREE))
                                || (head1 == ImplementConfig.TLV_CODE_SYS_DATA
                                && (head2 == ImplementConfig.TLV_VERSION_ONE
                                || head2 == ImplementConfig.TLV_VERSION_TWO
                                || head2 == ImplementConfig.TLV_VERSION_THREE))) {
                            int head3 = dataBuffer.get(2) & 0xff;
                            int head4 = dataBuffer.get(3) & 0xff;
                            packetLen = (head3 << 8) + head4;
                            if (packetLen > 8 && packetLen < PACKET_LENGTH) {
                                if (packetLen < length) {

                                    flag = true;

                                    break;
                                } else {
//									Log.d(TAG,"flag2222===="+flag);
                                    exit = true;
                                    break;
                                }
                            }

                        }
                        dataBuffer.remove(0);
                        length--;

                    }
                    if (flag) {

                        mLastReadTime = System.currentTimeMillis();
                        byte[] buffer = new byte[packetLen];
                        for (int i = 0; i < packetLen; i++) {
                            buffer[i] = dataBuffer.get(0);
                            dataBuffer.remove(0);
                        }
                        length -= packetLen;

                        if (head1 == ImplementConfig.TLV_CODE_SYS_DATA) { //???????

                            synchronized (rdSync) {
                                rdQueue.add(buffer);
                            }

                            exit = true;
                        }
                    }

                    if (exit) {
                        break;
                    }

                    length = dataBuffer.size();

                }
            }

            sendProcMsg();
        }

        private void sendProcMsg() {
            int rdQueueSize;
            synchronized (rdSync) {
                rdQueueSize = rdQueue.size();
            }

            if (rdQueueSize > 0 && !prcHandler.hasMessages(prcThPacketQueMsg)) {

                Message message = Message.obtain();
                message.what = prcThPacketQueMsg;

                prcHandler.sendMessage(message);

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Toast.makeText(DataReaderService.this,"sendProcMsg",Toast.LENGTH_LONG);
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BLE_DATA_MSG:
                    readData((byte[]) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }



    /**
     * Processor
     */
    private final class ProcessorHandler extends Handler {

        public ProcessorHandler(Looper looper) {
            super(looper);
        }
    }








    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Object event) {
        List<BleDevice> deviceList = (List<BleDevice>) event;
        if (deviceList != null) {
            for (BleDevice bleDevice : deviceList) {
                String bleName = bleDevice.getName();
                if (bleName.contains(Constant.CM19)) {
                    getCM19BleDevice(bleDevice);
                } else if (bleName.contains(Constant.SPO2)) {
                    getSpO2BleDevice(bleDevice);
                } else if (bleName.contains(Constant.QIANSHAN)) {
                    writeBPBleDevice(bleDevice);
                } else {

                }
            }
        }

    }

    /**
     * 收到CM19的bleDevice
     */
    private void getCM19BleDevice(BleDevice bleDevice) {
        BleManager.getInstance().notify(
                bleDevice,
                Constant.UUID_SERVICE_CM19,
                Constant.UUID_CHARA_CM19_NOTIFY,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        Log.d(TAG, "打开通知成功");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        Log.d(TAG, exception.toString() + "cm19打开通知失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        Log.d(TAG, Arrays.toString(data));
                        Message message = Message.obtain();
                        message.what = BLE_DATA_MSG;
                        message.obj = data;
                        readHandler.sendMessage(message);

                    }
                });
    }

    /**
     * 收到血氧bleDevice
     */
    private void getSpO2BleDevice(BleDevice bleDevice) {
        BleManager.getInstance().notify(
                bleDevice,
                Constant.UUID_SERVICE_SPO2,
                Constant.UUID_CHARA_SPO2_NOTIFY,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        Log.d(TAG, "打开通知成功");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        Log.d(TAG, exception.toString() + "打开通知失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        Log.d(TAG + "SpO2=======", Arrays.toString(data));
                    }
                });
    }

    /**
     * 收到血压计bleDevice,先写数据
     */
    private void writeBPBleDevice(BleDevice bleDevice) {
        BleManager.getInstance().write(
                bleDevice,
                Constant.UUID_SERVICE_BP,
                Constant.UUID_CHARA_BP_WRITE,
                ByteUtil.HexString2Bytes(Constant.START),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        Log.d(TAG,"current:"+current+"==total:"+total+"===byte[]:"+ Arrays.toString(justWrite));
                        getBPBleDevice(bleDevice);
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.d(TAG,exception.toString());
                    }
                }

        );
    }

    /**
     * 血压计bleDevice,写完数据成功，接收数据
     */

    private void getBPBleDevice(BleDevice bleDevice){
        BleManager.getInstance().notify(
                bleDevice,
                Constant.UUID_SERVICE_BP,
                Constant.UUID_CHARA_BP_NOTIFY,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        Log.d(TAG, "打开通知成功");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        Log.d(TAG, exception.toString() + "打开通知失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        Log.d(TAG + "bp=======", Arrays.toString(data));
                    }
                }

        );

    }

    /**
     * 拿到数据分析数据
     *
     * 原项目CPR_Patient_20210802
     *
     * 1、sendBleData--> message.what = BLE_DATAMSG; 每次20个字节
     *
     * 2、handleMessage -->readData((byte[]) msg.obj);
     *
     * 3、
     */

}
