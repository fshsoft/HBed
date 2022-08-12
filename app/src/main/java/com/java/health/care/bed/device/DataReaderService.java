package com.java.health.care.bed.device;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.SPUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.java.health.care.bed.constant.Constant;
import com.java.health.care.bed.constant.ImplementConfig;
import com.java.health.care.bed.model.DataTransmitter;
import com.java.health.care.bed.model.DevicePacket;
import com.java.health.care.bed.util.ByteUtil;
import com.microsenstech.PPG.model.Ucoherence;
import com.microsenstech.ucarerg.device.PacketParse;
import com.microsenstech.ucarerg.process.SignalProcessor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author fsh
 * @date 2022/08/04 10:51
 * @Description 服务
 */
public class DataReaderService extends Service {

    public static final String TAG = DataReaderService.class.getSimpleName();
    private static final int BLE_DATA_MSG = 3;
    //ble
    public static final int FLAG_START_TO_CONNECT = 0;
    public static final int FLAG_CONNECTED = 1;
    public static final int FLAG_DISCONNECTED = 2;

    private int currentOffset = 0;
    private int connOffset = 0;

    private int respRate = 0;
    public int score = 0;

    private AtomicBoolean isReading = new AtomicBoolean(false);

//    String path = "/sdcard/Patient/data/"+useId+"-"+dateNowStr+"/";
    private String path = Environment.getExternalStorageDirectory().getPath()+ "/Hbed/data/"+"1022"+"-"+"20220727144102"+"/";

    private long HRcounts = 0;
    private Queue<Integer> hrtRateQue = new LinkedList<Integer>();
    private static final int HrtRateQueSize = 16;

    /**
     *  BleReader 读取数据线程处理
     */
    private HandlerThread readTh;
    private Looper readLooper;
    private Handler readHandler;
    private volatile long mLastReadTime = 0;
    private static final int PACKET_LENGTH = 2000;
    private static final int BUFFER_WINDOW = PACKET_LENGTH * 1;
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

    /**
     * signalProcessor
     */
    private SignalProcessor signalProcessor = null;
    private DataTransmitter dataTrans = null;

    private String bleCM19Mac;
    private String bleSPO2Mac;
    private String bleQSMac;
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart()");
        bleCM19Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_CM19_MAC);
        bleSPO2Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_SPO2_MAC);
        bleQSMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_QIANSHAN_MAC);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        EventBus.getDefault().register(this);

        /**
         * BleReader 读取数据
         */
        readTh = new HandlerThread("BleReader",
                Process.THREAD_PRIORITY_BACKGROUND);
        readTh.start();
        readLooper = readTh.getLooper();
        readHandler = new BleReadHandler(readLooper);


        /**
         * Processor 解析数据
         */
        prcTh = new HandlerThread("Processor",
                Process.THREAD_PRIORITY_BACKGROUND);
        prcTh.start();
        prcLooper = prcTh.getLooper();
        prcHandler = new ProcessorHandler(prcLooper);

        /**
         * signalProcessor 库文件算法
         */
        signalProcessor = new SignalProcessor();
        Log.i(TAG, "Service onCreate");
        dataTrans = DataTransmitter.getInstance();
        dataTrans.setServiceRunning(true);
    }

    /**
     * BleReader 读取数据
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
                            //packetLen长度918     length从40--938
                            Log.d(TAG+"packetLen0:",packetLen+"====length:"+length);
                            if (packetLen > 8 && packetLen < PACKET_LENGTH) {
                                Log.d(TAG+"packetLen1:",packetLen+"====length:"+length);
                                if (packetLen < length) { //918<938
                                    Log.d(TAG+"packetLen3:",packetLen+"====length:"+length);
                                    flag = true;
                                } else {
                                    Log.d(TAG+"packetLen4:",packetLen+"====length:"+length);                                    exit = true;
                                }
                                break;
                            }

                        }
                        dataBuffer.remove(0);
                        length--;
                        Log.d(TAG+"dataBuffer+length--:",(length--)+"");

                    }
                    if (flag) {

                        mLastReadTime = System.currentTimeMillis();
                        byte[] buffer = new byte[packetLen];
                        for (int i = 0; i < packetLen; i++) {
                            buffer[i] = dataBuffer.get(0);
                            dataBuffer.remove(0);
                        }

                        Log.d(TAG+"buffer:",Arrays.toString(buffer));

                        if (head1 == ImplementConfig.TLV_CODE_SYS_DATA) {

                            synchronized (rdSync) {
                                rdQueue.add(buffer);
                            }

                            exit = true;

                            Log.d(TAG+"rdQueue:",rdQueue.size()+"");
                        }
                    }

                    if (exit) {
                        break;
                    }

                    length = dataBuffer.size();
                    Log.d(TAG+"dataBuffer+length:",dataBuffer.size()+"");
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
                    Log.d(TAG,"sendProcMsg");
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
     * Processor 解析数据
     */
    private final class ProcessorHandler extends Handler {

        public ProcessorHandler(Looper looper) {
            super(looper);
        }

        private void processDataTlv(Queue<byte[]> packets) {

            if(true){
                for (byte[] packet:packets){
                    if(PacketParse.parsePacket(packet)){
                        byte[] ecgData = PacketParse.getTlv(ImplementConfig.TLV_CODE_SYS_DATA_TYPE_ECG);
                        byte[] accData = PacketParse.getTlv(ImplementConfig.TLV_CODE_SYS_DATA_TYPE_ACC);
                        byte[] markingData = PacketParse.getTlv(ImplementConfig.TLV_CODE_SYS_DATA_TYPE_MARKING);
                        byte[] rspData = PacketParse.getTlv(ImplementConfig.TLV_CODE_SYS_DATA_TYPE_RSP);
                        byte[] gyrData = PacketParse.getTlv(ImplementConfig.TLV_CODE_SYS_DATA_TYPE_GYR);
                        byte[] magData = PacketParse.getTlv(ImplementConfig.TLV_CODE_SYS_DATA_TYPE_MAG);


                        if (ecgData != null) {
                            Log.d(TAG, "===ecgData.length =192" + ecgData.length);
                        }
                        if (accData != null) {
                            Log.d(TAG, "===accData.length =192" + accData.length);
                        }
                        if (markingData!=null){
                            Log.d(TAG, "===markingData.length =" + markingData.length);
                        }
                        if (rspData != null) {
                            Log.d(TAG, "===rspData.length ===288" + rspData.length);
                        }
                        if (gyrData != null) {
                            Log.d(TAG, "===gyrData.length ===192" + gyrData.length);
                        }
                        if (magData!=null){
                            Log.d(TAG, "===magData.length ===" + magData.length);
                        }

                        if ((ecgData == null)
                                || (accData == null)
                                || (rspData == null) || (gyrData == null)
                                || (ecgData.length != 192)
                                || (accData.length != 192)
                                || (rspData.length != 288) || (gyrData.length != 192)
                        ) {

                            continue;
                        }

                        currentOffset++;
                        connOffset++;
                        int length = ecgData.length / 2;
                        short[] smarkingData = null;
                        if (markingData != null) {
                            smarkingData = new short[1];
                            ByteUtil.bbToShorts(smarkingData, markingData);
                            if (smarkingData[0] > 0) {
                            }
                        }

                        short[] secgData = new short[length];
                        short[] sevgDataView = new  short[length];
                        ByteUtil.bbToShorts(secgData, ecgData);
                        ByteUtil.bbToShorts(sevgDataView,ecgData);


                        //写入文件ecg
                        FileIOUtils.writeFileFromBytesByStream(path+"ecgData.ecg",ByteUtil.get16Bitshort(secgData),true);


                        int[] irspData = new int[rspData.length / 3];
                        ByteUtil.bbToInts(irspData, rspData);

                        Log.i("methodecg_rsp_int",Arrays.toString(irspData));
                        //写入文件rsp
                        FileIOUtils.writeFileFromBytesByStream(path+"rspData.resp",ByteUtil.get16Bitint(irspData),true);
                        short[] saccData = new short[length];
                        ByteUtil.bbToShorts(saccData, accData);

                        short[] sgyrDatas = new short[length];
                        ByteUtil.bbToShorts(sgyrDatas, gyrData);
                        gyrData = null;


                        short[] sgyrData = null;
                        if (gyrData != null) {
                            sgyrData = new short[length];
                            for (int i = 0; i < length; i++) {
                                sgyrData[i] = (short) (gyrData[i] & 0xff);
                            }
                        }

                        short[] smagData = null;
                        if (magData != null) {
                            smagData = new short[length];
                            for (int i = 0; i < length; i++) {
                                smagData[i] = (short) (magData[i] & 0xff);
                            }

                        }


                        short[] data = new short[4 * length + 1];

                        System.arraycopy(secgData, 0, data, length, length);


                        if (sgyrData != null) {
                            System.arraycopy(sgyrData, 0, data, 2 * length, length);

                        } else {
                            for (int i = 0; i < length; i++) {
                                data[i + 2 * length] = 0;
                            }
                        }

                        if (smagData != null) {
                            System.arraycopy(smagData, 0, data, 3 * length, length);

                        } else {
                            for (int i = 0; i < length; i++) {
                                data[i + 3 * length] = 0;
                            }
                        }

                        //1mv
                        data[4 * length] = ImplementConfig.EcgBase * 16; //12bit
                        byte[] baseData = PacketParse.getTlv(ImplementConfig.TLV_CODE_SYS_DATA_TYPE_ECG_BASE);
                        byte[] biasData = PacketParse.getTlv(ImplementConfig.TLV_CODE_SYS_DATA_TYPE_ZERO_BIAS);

                        short[] sbaseData = new short[0] ;

                        if (baseData != null) {
                            sbaseData  = new short[baseData.length / 2];
                            ByteUtil.bbToShorts(sbaseData, baseData);
                            data[4 * length] = sbaseData[0];
                        }

                        short[] sbiaData = new short[0];
                        if (biasData != null)
                        {
                            sbiaData  = new short[biasData.length/2];
                            ByteUtil.bbToShorts(sbiaData, biasData);
                        }

                        byte[] elecData = PacketParse.getTlv(ImplementConfig.TLV_CODE_SYS_DATA_TYPE_ELECTRICITY);
                        int battery = 0;
                        if (elecData != null) {
                            short[] selecData = new short[elecData.length / 2];
                            ByteUtil.bbToShorts(selecData, elecData);
                            battery = (selecData[0] * 6) / 100;
                        }

                        double[] activity = new double[15];
                        int[] heartRate = new int[1];
                        int[] abnStates = new int[8];

                        byte[] secgnew = new byte[192];

                        if (signalProcessor != null) {

                            try {

                                signalProcessor.SmoothBaseLine(secgData,96);

                                secgnew =  ByteUtil.toByteArray(secgData);

                                signalProcessor.processData(data, data.length,
                                        heartRate, activity, abnStates);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Log.i(TAG + "tag", Arrays.toString(activity));
                            Log.i(TAG + "????", activity[0] + "");
                            Log.i(TAG + "???", activity[1] + "");
                            Log.i(TAG + "???", activity[2] + "");
                            Log.i(TAG + "????", activity[4] + "");
                            Log.i(TAG + "????", activity[5] + "");

                            if (activity[5] > 0) {
                                handHeartdata(heartRate[0], ByteUtil.intToByte1(heartRate[0]), battery);
                            }

                          /*  if (!getReadingFlag()) {
                                break;
                            }*/
                        }

                        DevicePacket devicePacket = new DevicePacket(currentOffset,
                                sevgDataView, secgnew, secgData,irspData, heartRate[0] , 1, (char) 26,
                                30, 15, null, 26,
                                getApplicationContext());
                        devicePacket.connOffset = connOffset;

                        if (sbaseData != null&& sbaseData.length >0)
                        {
                            devicePacket.sBaseHeight = sbaseData[0];

                        }
                        if (sbiaData != null && sbiaData.length>0)
                        {
                            devicePacket.sBiaHeight = sbiaData[0];
                        }


                        devicePacket.resp = 0;
                        respRate = devicePacket.resp;

                        devicePacket.score = score;

                        if (score > 0)
                        {
                            //写入文件score
                            byte[] baos = new byte[4];
                            ByteUtil.intToByte(baos,score,0);
                            FileIOUtils.writeFileFromBytesByStream(path+"scoreData.score",baos,true);

                        }


                        // 向监听器发送数据
                        dataTrans.sendData(devicePacket);

                    }else {

                    }


                }
            }
        }

        @Override
        public void handleMessage (Message msg){
            Queue<byte[]> packets = null;
            switch (msg.what) {

                case prcThPacketQueMsg:

                   /* if (!getReadingFlag()){
                        break;
                    }
*/

                    synchronized (rdSync) {
                        packets = rdQueue;
                        rdQueue = new LinkedList<>();
                    }

                    processDataTlv(packets);
                    break;
                case prcThExit:
                    getLooper().quit();
                    break;

            }

            super.handleMessage(msg);

        }


        private void handHeartdata( int heart, byte[] data , int battery){
            Log.d(TAG+"==handHeartdata=","===battery==="+battery+"byte[] data=="+data[0]+"===heart==="+heart);
            int len = 20;

            boolean[] ppgret = new boolean[2];
            float[] ppgdata = new float[4 * (len - 1)];
            float[] newret = new float[15];

            Ucoherence.parseEcgData(data, ppgret, ppgdata, newret);


            DevicePacket pkt = new DevicePacket();

            if (ppgret[1]) {

                pkt.bHasNew = ppgret[1];

                pkt.rrNew = heartRateFilter(heart);

                pkt.scoreNew = newret[1];
                score = (int)(newret[1]);

                pkt.sdnn = newret[2];
                pkt.sdsd = newret[3];
                pkt.lf = newret[4];
                pkt.hf = newret[5];
                pkt.lh = newret[6];

                pkt.psdH = newret[7];
                pkt.psdM = newret[8];
                pkt.psdL = newret[9];

                pkt.coherenceH = newret[10];
                pkt.coherenceM = newret[11];
                pkt.coherenceL = newret[12];

                pkt.maxPsd = newret[13];

            } else {
                pkt.rrNew = heartRateFilter(heart);
            }
            pkt.respRate = respRate;
            dataTrans.sendData(pkt, battery);

        }


        /**
         *
         * @param heartRate
         * @return 计算心率，心率值
         */
        private int heartRateFilter(int heartRate) {

            int tmp = heartRate;
            if (tmp < 0) {
                tmp = 0;
            }

            if (tmp > 0 && HRcounts < 3)//前3个心跳干扰几率过大，丢掉
            {
                HRcounts++;
                tmp = 0;
            }
            if (hrtRateQue.size() >= HrtRateQueSize) {
                hrtRateQue.poll();
            }
            hrtRateQue.offer(tmp);
            int sum = 0;
            int NoZeroValueCounts = 0;
            for (Integer hrt : hrtRateQue) {
                sum += hrt.intValue();
                if (hrt.intValue() != 0) {
                    NoZeroValueCounts++;
                }
            }
            if (NoZeroValueCounts != 0) {
                return (sum / NoZeroValueCounts);
            } else {
                return 0;
            }

        }

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        readTh.getLooper().quit();
        prcTh.getLooper().quit();
        signalProcessor.recycle();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {
        List<BleDevice> deviceList = (List<BleDevice>) event;
        if (deviceList != null) {
            for (BleDevice bleDevice : deviceList) {
                String bleMac = bleDevice.getMac();

                if (bleCM19Mac!=null) {
                    if( bleMac.equals(bleCM19Mac)) getCM19BleDevice(bleDevice);

                } else if (bleSPO2Mac!=null) {
                    if(bleMac.equals(bleSPO2Mac)) getSpO2BleDevice(bleDevice);

                } else if (bleQSMac!=null) {
                    if(bleMac.equals(bleQSMac)) writeBPBleDevice(bleDevice);
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
//                        Log.d(TAG, Arrays.toString(data));
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





}
