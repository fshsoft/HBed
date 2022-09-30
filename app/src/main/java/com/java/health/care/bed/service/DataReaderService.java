package com.java.health.care.bed.service;

import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
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
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.model.BPDevicePacket;
import com.java.health.care.bed.model.DataTransmitter;
import com.java.health.care.bed.model.DevicePacket;
import com.java.health.care.bed.net.RealTimeStatePacket;
import com.java.health.care.bed.util.ByteUtil;
import com.java.health.care.bed.util.DataUtils;
import com.microsenstech.PPG.model.Ucoherence;
import com.microsenstech.ucarerg.EcgPacket;
import com.microsenstech.ucarerg.TlvBox;
import com.microsenstech.ucarerg.device.PacketParse;
import com.microsenstech.ucarerg.process.SignalProcessor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


/**
 * @author fsh
 * @date 2022/08/04 10:51
 * @Description 服务
 */
public class DataReaderService extends Service {

    public static final String TAG = DataReaderService.class.getSimpleName();

    private  float rrValue;
    //获取设备开始时间
    private int startTime;

    private FileOutputStream ecgStream = null;
    private FileOutputStream scoreStream = null;
    private FileOutputStream respStream = null;

    private static final int CM19_BLE_DATA_MSG = 3;
    private static final int CM22_BLE_DATA_MSG = 4;
    //ble
    public static final int FLAG_START_TO_CONNECT = 0;
    public static final int FLAG_CONNECTED = 1;
    public static final int FLAG_DISCONNECTED = 2;

    private int currentOffset = 0;
    private int connOffset = 0;

    private int respRate = 0;
    public int score = 0;

    private AtomicBoolean isReading = new AtomicBoolean(false);

    private String path;

    private long HRcounts = 0;
    private Queue<Integer> hrtRateQue = new LinkedList<Integer>();
    private static final int HrtRateQueSize = 16;

    private Map<String, Object> mapEvent = new HashMap<>();
    /**
     * BleReader 读取数据线程处理
     */
    private HandlerThread readTh;
    private Looper readLooper;
    private Handler readHandler;

    private volatile long mLastReadTime = 0;
    private static final int PACKET_LENGTH = 2000;
    private static final int BUFFER_WINDOW = PACKET_LENGTH * 1;
    private List<Byte> dataBuffer = new LinkedList<>();
    private Queue<byte[]> rdQueue = new LinkedList<>();
    private Queue<byte[]> bpQueue = new LinkedList<>();
    private Object rdSync = new Object();
    private Object rdSyncbp = new Object();

    /**
     * Processor  数据处理
     */
    private HandlerThread prcTh;
    private final int prcThPacketQueMsg = 5;
    private final int prcThPacketQueMsgBP = 55;
    private final int prcThExit = 6;
    private Looper prcLooper;
    private Handler prcHandler;

    /**
     * signalProcessor
     */
    private SignalProcessor signalProcessor = null;
    private DataTransmitter dataTrans = null;

    private String bleCM19Mac;
    private String bleCM22Mac;
    private String bleSPO2Mac;
    private String bleQSMac;
    private String bleIRTMac;
    private String bleKYCMac;
    private BleDevice bleDeviceKYC;
    private BleDevice bleDeviceBP;

    private boolean isSendCompleteData = false;    //是否发送了一个完整的包

    private TlvBox tlvBox;

    private int spo2;
    private double temp;
    private int szPress;
    private int ssPress;
    private int serialNum = 0;

    //患者ID
    private int patientId;

    //判断CM19设备是做生命体征监测还是心肺谐振
    private boolean isLife = false;
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
        Log.d(TAG, "onCreate()");

        EventBus.getDefault().register(this);

//        tlvBox = new TlvBox();
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
    private final class BleReadHandler extends Handler {
        private ByteBuf buffer = Unpooled.buffer(1024 * 1000);

        public BleReadHandler(Looper looper) {
            super(looper);
        }

        private void readData(byte[] buffer) {
            mLastReadTime = System.currentTimeMillis();
            int num = buffer.length;
            if (num > 0) {
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

        private void readDataBP(byte[] data) {
            if (data != null && data.length > 0) {
                buffer.writeBytes(data);
                //重新组帧
                while (buffer.readableBytes() > 1000) {
                    isSendCompleteData = true;
                    ByteBuf bufTemp = buffer.readBytes(1);
                    byte[] bytesTemp = new byte[1];
                    bufTemp.readBytes(bytesTemp);
                    if (bytesTemp[0] == (byte) 0x02) {
                        buffer.markReaderIndex();     //取后一位
                        ByteBuf bufTemp1 = buffer.readBytes(1);
                        byte[] bytesTemp1 = new byte[1];
                        bufTemp1.readBytes(bytesTemp1);
                        if (bytesTemp1[0] == (byte) 0x01) {
                            buffer.markReaderIndex();     //取后
                            ByteBuf bufTemp2 = buffer.readBytes(2);   //判断两
                            byte[] bytesTemp2 = new byte[2];
                            bufTemp2.readBytes(bytesTemp2);
                            int number = DataUtils.byte2Int(bytesTemp2);
                            ByteBuf bufTemp3 = buffer.readBytes(number - 4);
                            byte[] bytesTemp3 = new byte[number - 4];
                            bufTemp3.readBytes(bytesTemp3);
                            byte[] TlvData = DataUtils.subBytes(bytesTemp3, 12, number - 16);
                            int currentLength = 0;
                            while (currentLength + 4 < number - 16) {
                                byte[] lenghtData = DataUtils.subBytes(TlvData, currentLength + 2, 2);
                                int diTlvlength = DataUtils.byte2Int(lenghtData);
                                if (diTlvlength == 0) {
                                    currentLength = currentLength + 4;
                                } else {
                                    currentLength = currentLength + diTlvlength;
                                }
                            }
                            if (currentLength != number - 16) {
                                Log.i(TAG, "handleMessage: 丢包");

                            } else {
                                byte[] data3 = {0x02, 0x01};
                                byte[] data4 = DataUtils.byteMerger(data3, bytesTemp2);
                                byte[] datas = DataUtils.byteMerger(data4, bytesTemp3);

                                synchronized (rdSyncbp) {
                                    Log.d(TAG + "fshsoft", Arrays.toString(datas));
                                    bpQueue.add(datas);

                                }
                                buffer.discardReadBytes();   //将取出来的这一帧数据在buffer的内存进行清除，释放内存
                            }


                        } else {
                            Log.i(TAG, "handleMessage: 丢包3");
                        }
                    } else {
                        Log.i(TAG, "handleMessage: 丢包2");
                    }
                }
            }

            sendProcMsgBP();
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

/*                        if ((head1 == ImplementConfig.TLV_CODE_SYS_HEAD
                                && (head2 == ImplementConfig.TLV_VERSION_ONE
//                                || head2 == ImplementConfig.TLV_VERSION_TWO
                                || head2 == ImplementConfig.TLV_VERSION_THREE))
                                || (head1 == ImplementConfig.TLV_CODE_SYS_DATA
                                && (head2 == ImplementConfig.TLV_VERSION_ONE
//                                || head2 == ImplementConfig.TLV_VERSION_TWO
                                || head2 == ImplementConfig.TLV_VERSION_THREE))) {*/
                        if(head1 == ImplementConfig.TLV_CODE_SYS_DATA && head2 == ImplementConfig.TLV_VERSION_ONE) {
                            int head3 = dataBuffer.get(2) & 0xff;
                            int head4 = dataBuffer.get(3) & 0xff;
                            packetLen = (head3 << 8) + head4;
                            //packetLen长度918     length从40--938
                            Log.d(TAG + "packetLen0:", packetLen + "====length:" + length);
                            if (packetLen > 8 && packetLen < PACKET_LENGTH) {
                                Log.d(TAG + "packetLen1:", packetLen + "====length:" + length);
                                if (packetLen < length) { //918<938
                                    Log.d(TAG + "packetLen3:", packetLen + "====length:" + length);
                                    flag = true;
                                } else {
                                    Log.d(TAG + "packetLen4:", packetLen + "====length:" + length);
                                    exit = true;
                                }
                                break;
                            }

                        }
                        dataBuffer.remove(0);
                        length--;
                        Log.d(TAG + "dataBuffer+length--:", (length--) + "");

                    }
                    if (flag) {

                        mLastReadTime = System.currentTimeMillis();
                        byte[] buffer = new byte[packetLen];
                        for (int i = 0; i < packetLen; i++) {
                            buffer[i] = dataBuffer.get(0);
                            dataBuffer.remove(0);
                        }

                        Log.d(TAG + "buffer:", Arrays.toString(buffer));

                        if (head1 == ImplementConfig.TLV_CODE_SYS_DATA) {

                            synchronized (rdSync) {
                                rdQueue.add(buffer);
                            }

                            exit = true;

                            Log.d(TAG + "rdQueue:", rdQueue.size() + "");
                        }
                    }

                    if (exit) {
                        break;
                    }

                    length = dataBuffer.size();
                    Log.d(TAG + "dataBuffer+length:", dataBuffer.size() + "");
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

           /*     try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Log.d(TAG, "sendProcMsg");
                    e.printStackTrace();
                }*/
            }
        }

        private void sendProcMsgBP() {
            int rdQueueSize;
            synchronized (rdSyncbp) {
                rdQueueSize = bpQueue.size();
            }

            if (rdQueueSize > 0 && !prcHandler.hasMessages(prcThPacketQueMsgBP)) {

                Message message = Message.obtain();
                message.what = prcThPacketQueMsgBP;

                prcHandler.sendMessage(message);

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Log.d(TAG, "sendProcMsg");
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CM19_BLE_DATA_MSG:
                    readData((byte[]) msg.obj);
                    break;

                case CM22_BLE_DATA_MSG:
                    readDataBP((byte[]) msg.obj);
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

        //无创连续血压
        private void processDataTlvBP(Queue<byte[]> packets) {

            if (true) {

                for (byte[] packet : packets) {
                    Log.d("fsh===", Arrays.toString(packet));
                    //cm22进行写入数据
                    FileIOUtils.writeFileFromBytesByStream(path + "ecgData.ecg",packet, true);
                    //发送整包数据，和CM19有区别组包cm22
                    dataTrans.sendData(packet);
                    tlvBox = new TlvBox();
                    int len = tlvBox.decodePacket(packet);
//                    Log.d("fsh===",len+"===len");
                    if (len == 0) {

                        byte[] ecgData = tlvBox.getBytesValue(EcgPacket.Ecg.getType());
                        byte[] ppgData = tlvBox.getBytesValue(EcgPacket.PPG.getType());
                        byte[] heartData = tlvBox.getBytesValue(EcgPacket.HeartRate.getType());
                        byte[] szPressData = tlvBox.getBytesValue(EcgPacket.DiaBp.getType());
                        byte[] ssPressData = tlvBox.getBytesValue(EcgPacket.SysBp.getType());
                        //R波位置
                        byte[] rrData = tlvBox.getBytesValue(EcgPacket.RIndex.getType());

//                        if (ecgData != null) {
//                            Log.d("fsh===", "===ecgData192" + ecgData.length + "===" + Arrays.toString(ecgData));
//                        }
//
//                        if (ppgData != null) {
//                            Log.d("fsh===", "===ppgData192" + ppgData.length + "===" + Arrays.toString(ppgData));
//                        }
//                        if (heartData != null) {
//                            Log.d("fsh===", "===heartData4" + heartData.length + "===" + Arrays.toString(heartData));
//                        }
//
//                        if (szPressData != null) {
//                            Log.d("fsh===", "===szPressData4" + szPressData.length + "===" + Arrays.toString(szPressData));
//                        }
//                        if (ssPressData != null) {
//                            Log.d("fsh===", "===ssPressData4" + ssPressData.length + "===" + Arrays.toString(ssPressData));
//                        }

                        int length = ecgData.length/2;
                        short[] sEcgData = new short[length];
                        short[] sPpgData = new short[length];
                        ByteUtil.bbToShorts(sEcgData, ecgData);
                        ByteUtil.bbToShorts(sPpgData, ppgData);

                        short[] sHeartData = new short[2];
                        short[] sSzPressDataData = new short[2];
                        short[] sSsPressDataData = new short[2];
                        ByteUtil.bbToShorts(sHeartData, heartData);
                        ByteUtil.bbToShorts(sSzPressDataData, szPressData);
                        ByteUtil.bbToShorts(sSsPressDataData, ssPressData);

                        int sEcg = sHeartData[0];
                        int sSzPress = sSzPressDataData[0];
                        int sSsPress = sSsPressDataData[0];
//                        Log.d("fsh===", "===sEcgData0====" + sEcgData.length + "===" + Arrays.toString(sEcgData));
                        if(signalProcessor!=null){
                            signalProcessor.SmoothBaseLine(sEcgData, 96);
                        }

                    /*    byte[] realTimeData = new RealTimeStatePacket(patientId,serialNum++,ecgData,null,ppgData,rrData,szPressData,ssPressData,
                                (short) sEcg,(short) 0,(short) 0,(short) 0, (short) 0,(short) 0,startTime).buildPacket();
                        dataTrans.sendData(realTimeData);*/

                        BPDevicePacket bpDevicePacket = new BPDevicePacket(sEcgData,sPpgData,sEcg,sSzPress,sSsPress);
                        dataTrans.sendData(bpDevicePacket);
//                        Log.d("fsh===", "===sEcgData====" + sEcgData.length + "===" + Arrays.toString(sEcgData));
//                        Log.d("fsh===", "===sPpgData" + sPpgData.length + "===" + Arrays.toString(sPpgData));
//                        Log.d("fsh===", "===sHeartData" + sHeartData.length + "===" + Arrays.toString(sHeartData));
//                        Log.d("fsh===", "===sSzPressDataData" + sSzPressDataData.length + "===" + Arrays.toString(sSzPressDataData));
//                        Log.d("fsh===", "===sSsPressDataData" + sSsPressDataData.length + "===" + Arrays.toString(sSsPressDataData));


                    }

                }
            }
        }

        private void processDataTlv(Queue<byte[]> packets) {

            if (true) {
                for (byte[] packet : packets) {

                    Log.d("fsh===", Arrays.toString(packet));

                    if (PacketParse.parsePacket(packet)) {
                        byte[] ecgData = PacketParse.getTlv(ImplementConfig.TLV_CODE_SYS_DATA_TYPE_ECG);
                        byte[] accData = PacketParse.getTlv(ImplementConfig.TLV_CODE_SYS_DATA_TYPE_ACC);
                        byte[] markingData = PacketParse.getTlv(ImplementConfig.TLV_CODE_SYS_DATA_TYPE_MARKING);
                        byte[] rspData = PacketParse.getTlv(ImplementConfig.TLV_CODE_SYS_DATA_TYPE_RSP);
                        byte[] gyrData = PacketParse.getTlv(ImplementConfig.TLV_CODE_SYS_DATA_TYPE_GYR);
                        byte[] magData = PacketParse.getTlv(ImplementConfig.TLV_CODE_SYS_DATA_TYPE_MAG);


                        /*if (ecgData != null) {
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
                        }*/

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
                        ByteUtil.bbToShorts(secgData, ecgData);
//                        Log.d("fsh===", "===sEcgData1===" + secgData.length + "===" + Arrays.toString(secgData));

                        byte[] becgData = new byte[length];
                        for (int i = 0; i < length; i++) {
                            becgData[i] = (byte) ((secgData[i] >> 4) & 0xff);
                        }

////////////////////////拼接时间戳8字节+96字节ECG心电数据/////////////////////////////////////////////////////////////////////////////////////////////////
                      /*  byte[] baoEcg = new byte[8+96];
                        byte[] contentEcg = becgData;
                        long time = System.currentTimeMillis();
                        ByteUtil.putLong(baoEcg,time,0);
                        System.arraycopy(contentEcg,0,baoEcg,8,96);
                        Log.d("arraycopy====",Arrays.toString(baoEcg));
                        Log.d("arraycopy====1",Arrays.toString(contentEcg));
                        //写入文件ecg
                        FileIOUtils.writeFileFromBytesByStream(path + "ecgData.ecg", baoEcg, true);*/
                        //针对心肺谐振的写入
//                        FileIOUtils.writeFileFromBytesByStream(path + "ecgData.ecg", ByteUtil.get16Bitshort(secgData), true);
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        int[] irspData = new int[rspData.length / 3];
                        ByteUtil.bbToInts(irspData, rspData);

                        Log.i("methodecg_rsp_int", Arrays.toString(irspData));
                        //写入文件rsp //针对心肺谐振的写入
//                        FileIOUtils.writeFileFromBytesByStream(path + "respData.resp", ByteUtil.get16Bitint(irspData), true);
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

                        short[] sbaseData = new short[0];

                        if (baseData != null) {
                            sbaseData = new short[baseData.length / 2];
                            ByteUtil.bbToShorts(sbaseData, baseData);
                            data[4 * length] = sbaseData[0];
                        }

                        short[] sbiaData = new short[0];
                        if (biasData != null) {
                            sbiaData = new short[biasData.length / 2];
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

                                signalProcessor.SmoothBaseLine(secgData, 96);

                                secgnew = ByteUtil.toByteArray(secgData);

                                signalProcessor.processData(data, data.length,
                                        heartRate, activity, abnStates);

                                respRate = signalProcessor.RespProcess(irspData, 96);
                                Log.d("resp======", respRate + "===");

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
                        }



                        DevicePacket devicePacket = new DevicePacket(currentOffset,
                                becgData, secgnew, secgData, irspData, heartRate[0], 1, (char) 26,
                                30, 15, null, 26,
                                getApplicationContext());
                        devicePacket.connOffset = connOffset;

                        if (sbaseData != null && sbaseData.length > 0) {
                            devicePacket.sBaseHeight = sbaseData[0];

                        }
                        if (sbiaData != null && sbiaData.length > 0) {
                            devicePacket.sBiaHeight = sbiaData[0];
                        }


                        devicePacket.resp = respRate;

                        devicePacket.score = score;

                        //实时发送
                        byte[] realTimeData = new RealTimeStatePacket(patientId,serialNum++,ecgData,rspData,null,null,null,null,
                                (short) heartRate[0],(short) spo2,(short) szPress,(short) ssPress, (short) respRate,(short) temp,rrValue,startTime).buildPacket();

                        //实时发送整包数据，组包
                        dataTrans.sendData(realTimeData);


                        if(isLife){

                            //生命体征检测：重新组包，把数据写入文件

                            byte[] realDatas = new RealTimeStatePacket(patientId,serialNum++,ecgData,rspData,(short) heartRate[0],
                                    (short) respRate,devicePacket.rrNew,getSecondTimestamp(new Date())).buildPacket();
                            FileIOUtils.writeFileFromBytesByStream(path + "LIFE.life", realDatas, true);
                        }else {
                            //针对心肺谐振的写入
                            FileIOUtils.writeFileFromBytesByStream(path + "ecgData.ecg", ByteUtil.get16Bitshort(secgData), true);
                            FileIOUtils.writeFileFromBytesByStream(path + "respData.resp", ByteUtil.get16Bitint(irspData), true);
                            if (score > 0) {
                                byte[] baos = new byte[4];
                                ByteUtil.intToByte(baos, score, 0);
                                FileIOUtils.writeFileFromBytesByStream(path + "scoreData.score", baos, true);
                            }

                        }


                        // 向监听器发送数据
                        dataTrans.sendData(devicePacket);

                    } else {

                    }


                }
            }
        }

        @Override
        public void handleMessage(Message msg) {
            Queue<byte[]> packets = null;
            Queue<byte[]> packetsbp = null;
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
                case prcThPacketQueMsgBP:

                    synchronized (rdSyncbp) {
                        packetsbp = bpQueue;
                        bpQueue = new LinkedList<>();
                    }

                    processDataTlvBP(packetsbp);
                    break;

                case prcThExit:
                    getLooper().quit();
                    break;

            }

            super.handleMessage(msg);

        }


        private void handHeartdata(int heart, byte[] data, int battery) {
            Log.d(TAG + "==handHeartdata=", "===battery===" + battery + "byte[] data==" + data[0] + "===heart===" + heart);
            int len = 20;

            boolean[] ppgret = new boolean[2];
            float[] ppgdata = new float[4 * (len - 1)];
            float[] newret = new float[15];

            Ucoherence.parseEcgData(data, ppgret, ppgdata, newret);


            DevicePacket pkt = new DevicePacket();

            if (ppgret[1]) {

                pkt.bHasNew = ppgret[1];

                //计算心率RR间期
                pkt.rrNew = heartRateFilter(heart);
                Log.d(TAG, "RR间期1：" + pkt.rrNew);

                pkt.scoreNew = newret[1];
                score = (int) (newret[1]);

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
                Log.d(TAG, "RR间期0：" + pkt.rrNew);
            }

            rrValue = pkt.rrNew;
            pkt.respRate = respRate;
            dataTrans.sendData(pkt, battery);
            //写RR到文件
            FileIOUtils.writeFileFromString(path+"rrData.rr",String.valueOf(pkt.rrNew)+"\n",true);
        }


        /**
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
        bleCM19Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_CM19_MAC);
        bleCM22Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_CM22_MAC);
        bleSPO2Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_SPO2_MAC);
        bleQSMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_QIANSHAN_MAC);
        bleIRTMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_IRT_MAC);
        bleKYCMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_KYC_MAC);
        if (event instanceof String) {
            String str = (String) event;
            if (bleDeviceKYC != null) {
                writeKYCBleDevice(bleDeviceKYC, str);
            } else {
                Log.d(TAG, "康养床bleDevice为空了");
            }

        }else if(event instanceof List){
            List<BleDevice> deviceList = (List<BleDevice>) event;
            if (deviceList != null) {
                Log.d(TAG,"deviceList=="+deviceList.size());
                for (BleDevice bleDevice : deviceList) {
                    String bleMac = bleDevice.getMac();

                    if (bleCM19Mac != null) {
                        if (bleMac.equals(bleCM19Mac)) {
                            //蓝牙已经连接上了cm19设备，打开通知
                            getCM19BleDevice(bleDevice);
                        }
                    }
                    if (bleDevice != null) {
                        if (bleMac.equals(bleCM22Mac)) {
//                            writeCM22BPBleDevice(bleDevice);
                            getCM22BPBleDevice(bleDevice);
                        }
                    }
                    if (bleSPO2Mac != null) {
                        if (bleMac.equals(bleSPO2Mac)) getSpO2BleDevice(bleDevice);

                    }
                    if (bleQSMac != null) {
                        if (bleMac.equals(bleQSMac))

//                            writeBPBleDevice(bleDevice);
                        bleDeviceBP = bleDevice;
                        getBPBleDevice(bleDeviceBP);
                    }
                    if (bleIRTMac != null) {
                        if (bleMac.equals(bleIRTMac)) {
                            getIRTBleDevice(bleDevice);
                        }

                    }
                    if (bleKYCMac != null) {
                        if (bleMac.equals(bleKYCMac)) {
                            bleDeviceKYC = bleDevice;
                            getKYCBleDevice(bleDeviceKYC);
                        }
                    }
                }
            }
        }
        else if (event instanceof Integer){
            int time = (int) event;
            if(time == 2){
                // 手动控制测量
                writeBPBleDevice(bleDeviceBP);
            }

        }else if(event instanceof Boolean){
            boolean isLifeOrPPG  = (boolean) event;
            if(isLifeOrPPG == true){
                isLife = true;
            }else {
                isLife = false;
            }
        }


    }

    /**
     * 记录读写数据时间和创建文件
     */
    private void recordTimeAndCreateFile(){

        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateNowStr = sdf.format(d);
        SPUtils.getInstance().put(SP.KEY_ECG_FILE_TIME,dateNowStr);
        patientId = SPUtils.getInstance().getInt(SP.PATIENT_ID);
        path = Environment.getExternalStorageDirectory().getPath()+"/HBed/data/"+patientId+"-"+dateNowStr+"/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }


    /**
     * 收到CM19的bleDevice
     */
    private void getCM19BleDevice(BleDevice bleDevice) {
        startTime = getSecondTimestamp(new Date());
        BleManager.getInstance().notify(
                bleDevice,
                Constant.UUID_SERVICE_CM19,
                Constant.UUID_CHARA_CM19_NOTIFY,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        Log.d(TAG, "cm19打开通知成功");
                        recordTimeAndCreateFile();
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
                        message.what = CM19_BLE_DATA_MSG;
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
                        Log.d(TAG, "SpO2打开通知成功");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        Log.d(TAG, exception.toString() + "SpO2打开通知失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        Log.d(TAG + "SpO2=======", Arrays.toString(data));
                        if (data.length == 13) {
                            spo2 = DataUtils.getSPO2Data(data);
                            if (spo2 != 0) {
                                Log.d("fshman", "spo2:" + spo2);
                                mapEvent.put(Constant.SPO2_DATA, spo2);
                                EventBus.getDefault().post(mapEvent);
                            }

                        }

                    }
                });
    }

    /**
     * 收到体温计bleDevice
     */
    private void getIRTBleDevice(BleDevice bleDevice) {
        BleManager.getInstance().notify(
                bleDevice,
                Constant.UUID_SERVICE_IRT,
                Constant.UUID_CHARA_IRT_NOTIFY,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        Log.d(TAG, "IRT体温打开通知成功");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        Log.d(TAG, exception.toString() + "IRT打开通知失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        Log.d(TAG + "IRT=======", Arrays.toString(data));
                        if (data.length == 4) {
                            double dataIRT = DataUtils.getIRTData(data);
                            temp = (double)dataIRT*10;
                            Log.d("fshman" + "IRT=======", "dataIRT:" + dataIRT+"==="+temp);
                            mapEvent.put(Constant.IRT_DATA, dataIRT);
                            EventBus.getDefault().post(mapEvent);
                        }

                    }
                });
    }


    /**
     * 收到CM22无创连续血压计bleDevice,先写数据 开始
     */

    private void writeCM22BPBleDevice(BleDevice bleDevice) {
        //获取时间
        startTime = getSecondTimestamp(new Date());
        BleManager.getInstance().write(
                bleDevice,
                Constant.UUID_SERVICE_CM22,
                Constant.UUID_CHARA_CM22_WRITE,
                ByteUtil.HexString2Bytes(Constant.Order_BeginMeasure),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        Log.d("cm22========2", "current:" + current + "==total:" + total + "===byte[]:" + Arrays.toString(justWrite));
                        recordTimeAndCreateFile();
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.d(TAG, exception.toString());
                    }
                }

        );
    }

    private void writeCM22BPBleDevicePatientInfo(BleDevice bleDevice) {
        BleManager.getInstance().write(
                bleDevice,
                Constant.UUID_SERVICE_CM22,
                Constant.UUID_CHARA_CM22_WRITE,
                patientInfo(),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        Log.d("cm22========0", "current:" + current + "==total:" + total + "===byte[]:" + Arrays.toString(justWrite));
                        //需要先写入个人信息和标定值，最后点击开始
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        writeCM22BPBleDevicePatientID(bleDevice);
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.d(TAG, exception.toString());
                    }
                }

        );
    }

    private void writeCM22BPBleDevicePatientID(BleDevice bleDevice) {
        BleManager.getInstance().write(
                bleDevice,
                Constant.UUID_SERVICE_CM22,
                Constant.UUID_CHARA_CM22_WRITE,
                patientID(),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        Log.d("cm22========0", "current:" + current + "==total:" + total + "===byte[]:" + Arrays.toString(justWrite));
                        //需要先写入个人信息和标定值，最后点击开始
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        writeCM22BPBleDeviceTime(bleDevice);
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.d(TAG, exception.toString());
                    }
                }

        );
    }

    private void writeCM22BPBleDeviceTime(BleDevice bleDevice) {
        BleManager.getInstance().write(
                bleDevice,
                Constant.UUID_SERVICE_CM22,
                Constant.UUID_CHARA_CM22_WRITE,
                writeSyncTime(),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        Log.d("cm22========0", "current:" + current + "==total:" + total + "===byte[]:" + Arrays.toString(justWrite));
                        //需要先写入个人信息和标定值，最后点击开始
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        writeCM22BPBleDeviceBD(bleDevice);
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.d(TAG, exception.toString());
                    }
                }

        );
    }

    private void writeCM22BPBleDeviceBD(BleDevice bleDevice) {
        BleManager.getInstance().write(
                bleDevice,
                Constant.UUID_SERVICE_CM22,
                Constant.UUID_CHARA_CM22_WRITE,
                bdValue(),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        Log.d("cm22========1", "current:" + current + "==total:" + total + "===byte[]:" + Arrays.toString(justWrite));
                        //需要先写入个人信息和标定值，最后点击开始
                        writeCM22BPBleDevice(bleDevice);
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.d(TAG, exception.toString());
                    }
                }

        );
    }

    private void getCM22BPBleDevice(BleDevice bleDevice) {
        BleManager.getInstance().notify(
                bleDevice,
                Constant.UUID_SERVICE_CM22,
                Constant.UUID_CHARA_CM22_NOTIFY,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        Log.d(TAG, "cm22bp打开通知成功");

                        writeCM22BPBleDevicePatientInfo(bleDevice);
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        Log.d(TAG, exception.toString() + "cm22bp打开通知失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        Log.d(TAG + "cm22bp=======", Arrays.toString(data));
                        Message message = Message.obtain();
                        message.what = CM22_BLE_DATA_MSG;
                        message.obj = data;
                        readHandler.sendMessage(message);
                    }
                }

        );
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
                        Log.d("BP====", "current:" + current + "==total:" + total + "===byte[]:" + Arrays.toString(justWrite));

                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.d("BP=======", exception.toString());
                    }
                }

        );
    }

    /**
     * 血压计bleDevice,写完数据成功，接收数据
     */

    private void getBPBleDevice(BleDevice bleDevice) {
        BleManager.getInstance().notify(
                bleDevice,
                Constant.UUID_SERVICE_BP,
                Constant.UUID_CHARA_BP_NOTIFY,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        Log.d("BP====", "bp打开通知成功");

                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        Log.d("BP===", exception.toString() + "bp打开通知失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        Log.d("BP====", Arrays.toString(data));
                        if (data.length == 10) {
                            ssPress = data[6] & 0xff;
                            szPress = data[8] & 0xff;
                            String bpString = DataUtils.getSBPData(data);

                            mapEvent.put(Constant.BP_DATA, bpString);
                            EventBus.getDefault().post(mapEvent);
                            Log.d(TAG + "bp=======2", Arrays.toString(data));
                        } else if (data.length == 6) {
                            //测量失败
//                            mapEvent.put(Constant.BP_DATA_ERROR, "BP_ERROR");
//                            EventBus.getDefault().post(mapEvent);
                        }

                    }
                }

        );

    }


    private void getKYCBleDevice(BleDevice bleDevice) {
        BleManager.getInstance().notify(
                bleDevice,
                Constant.UUID_SERVICE_KYC,
                Constant.UUID_CHARA_KYC_NOTIFY,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        Log.d(TAG, "kyc打开通知成功");

                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        Log.d(TAG, exception.toString() + "kyc打开通知失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        Log.d("fshman" + "kyc=======", Arrays.toString(data));
                    }
                }

        );
    }

    private void writeKYCBleDevice(BleDevice bleDevice, String bleKYCWrite) {
        BleManager.getInstance().write(
                bleDevice,
                Constant.UUID_SERVICE_KYC,
                Constant.UUID_CHARA_KYC_WRITE,
                ByteUtil.HexString2Bytes(bleKYCWrite),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        Log.d(TAG, "kyc-current:" + current + "==total:" + total + "===byte[]:" + Arrays.toString(justWrite));

                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.d(TAG, exception.toString());
                    }
                }

        );
    }


    //CM22 点击开始前，需要写入一些个人信息
    private byte[] patientInfo(){
        //个人信息从接口获取  身高，体重，臂长，性别，年龄{height,weight,armLength,sex,age}
        byte height = (byte) 170;
        byte weight = (byte)80;
        byte armLength = (byte)65;
     /*   int sexValue =SPUtils.getInstance().getString(SP.PATIENT_SEX).equals("男")? 1: 0;
        byte sex = (byte) sexValue;
        String ageValue = SPUtils.getInstance().getString(SP.PATIENT_AGE);
        byte age = (byte) Integer.parseInt(ageValue);*/
        byte sex = (byte)1;
        byte age = (byte)32;
        byte[] patientBytes = {height,weight,armLength,sex,age};
        String HexPersonStr = ByteUtil.bytesToHexString(patientBytes);
        return ByteUtil.HexString2Bytes(Constant.Order_PersonInfo+HexPersonStr);
    }

    //CM22 用户ID
    private byte[] patientID(){
        byte[] IDbytes = new byte[4];
        Log.d(TAG,"patientId:"+patientId);
        ByteUtil.putIntBig(IDbytes,patientId,0);
        String HexID = ByteUtil.bytesToHexString(IDbytes);
        return ByteUtil.HexString2Bytes(Constant.Order_PersonID+HexID);
    }

    //cm22 发送开始时间
    private byte[] writeSyncTime(){
        int curTimestamp =  getSecondTimestamp(new Date());
        byte[] timeByte = new byte[4];
        ByteUtil.putIntBig(timeByte,curTimestamp,0);
        String HexTime =  ByteUtil.bytesToHexString(timeByte);
        return ByteUtil.HexString2Bytes(Constant.Order_BeginTime+HexTime);

    }

    //CM22无创连续血压值标定
    private byte[] bdValue(){
        String bd = SPUtils.getInstance().getString(SP.WCPRESSVALUE);
        String[] bp = bd.split("/");
        int ss = Integer.parseInt(bp[0]);
        int sz = Integer.parseInt(bp[1]);

        byte[] ssBytes =new byte[2];
        byte[] szBytes =new byte[2];
        ByteUtil.putInttoTwoSmart(ssBytes,ss,0);
        ByteUtil.putInttoTwoSmart(szBytes,sz,0);
        String HexDia = ByteUtil.bytesToHexString(ssBytes);
        String HexSys = ByteUtil.bytesToHexString(szBytes);
        return ByteUtil.HexString2Bytes(Constant.Order_Calibration+HexDia+HexSys);
    }

 /*   //写入个人信息
    private void writePersonInfo(BluetoothGattCharacteristic characteristic){
        Log.i(TAG, "血压计个人信息");
        UserModel user = BloodPressApplication.getInstance().getMyUser();
        Byte height =(byte)(Integer.parseInt(user.getHeight()));
        Byte weight =(byte)(Integer.parseInt(user.getWeight()));
        Byte armlen =(byte)(Integer.parseInt(user.getArteriallen()));
        int sexValue = user.getSex()?1:0;
        Byte sex =(byte)(sexValue);
        Byte age =(byte)(Integer.parseInt(user.getAge()));
        byte[] personBytes ={height,weight,armlen,sex,age};
        String HexPersonStr = ByteUtil.bytesToHexString(personBytes);
        characteristic.setValue(ByteUtil.HexString2Bytes(Order_PersonInfo+HexPersonStr));
        Log.i(TAG, "writePersonInfo: "+Order_PersonInfo+HexPersonStr);
    }
    //同步时间
    private  void writeSyncTime(BluetoothGattCharacteristic characteristic){
        Log.i(TAG, "同步时间");
        int curTimestamp =  getSecondTimestamp(new Date());
        byte[] timeByte = new byte[4];
        ByteUtil.putIntBig(timeByte,curTimestamp,0);
        String HexTime =  ByteUtil.bytesToHexString(timeByte);
        characteristic.setValue(ByteUtil.HexString2Bytes(Order_BeginTime+HexTime));
        Log.i(TAG, "writeSyncTime: "+Order_BeginTime+HexTime);
    };

    //写入标定
    private  void writeBiaoding(BluetoothGattCharacteristic characteristic){
        Log.i(TAG, "写入标定");
        BiaodingModel biaodingModel = BloodPressApplication.getInstance().getMyBiaoding();
        int dia,sys;
        if (biaodingModel!=null){
            dia =Integer.parseInt(biaodingModel.getDiapress());
            sys =Integer.parseInt(biaodingModel.getSyspress());
        }else{
            dia = 120;
            sys = 80;
        }
        byte[] diabytes =new byte[2];
        byte[] sysbytes =new byte[2];
        ByteUtil.putInttoTwoSmart(diabytes,dia,0);
        ByteUtil.putInttoTwoSmart(sysbytes,sys,0);
        String Hexdia = ByteUtil.bytesToHexString(diabytes);
        String Hexsys = ByteUtil.bytesToHexString(sysbytes);
        characteristic.setValue(ByteUtil.HexString2Bytes(Order_Calibration+Hexdia+Hexsys));
        Log.i(TAG, "writeBiaoding: "+Order_Calibration+Hexdia+Hexsys);
    }

    private  void writePersonID(BluetoothGattCharacteristic characteristic){
        Log.i(TAG, "个人id");

        UserModel user = BloodPressApplication.getInstance().getMyUser();
        int personID =  user.getId();
        byte[] IDbytes = new byte[4];
        ByteUtil.putIntBig(IDbytes,personID,0);
        String HexID = ByteUtil.bytesToHexString(IDbytes);
        characteristic.setValue(ByteUtil.HexString2Bytes(Order_PersonID+HexID));
        Log.i(TAG, "writePersonID: "+Order_PersonID+HexID);
    }
    //停止测试
    private  void sendStopBroadcast(BluetoothGattCharacteristic characteristic){
        Log.i(TAG, "writeBlueCharacteristic: "+"停止测试");
        characteristic.setValue(ByteUtil.HexString2Bytes(Order_StopMeasure));
        if (BloodPressApplication.getInstance().broadcase ==BloodPressApplication.Broadcase.measureFragment) {
            Intent intent = new Intent(
                    MeasureFragment.BP_STOP_SEND);
            sendBroadcast(intent);
        }else if(BloodPressApplication.getInstance().broadcase ==BloodPressApplication.Broadcase.CalibrationActivity){
            Intent intent = new Intent(
                    CalibrationActivity.BP_STOP_SEND);
            sendBroadcast(intent);
        }
    }*/
 //获取当前时间戳
 public static int getSecondTimestamp(Date date){
     if (null == date) {
         return 0;
     }
     String timestamp = String.valueOf(date.getTime());
     int length = timestamp.length();
     if (length > 3) {
         return Integer.valueOf(timestamp.substring(0,length-3));
     } else {
         return 0;
     }
 }
}
