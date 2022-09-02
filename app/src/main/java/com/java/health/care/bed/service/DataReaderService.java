package com.java.health.care.bed.service;

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
import com.java.health.care.bed.model.BPDevicePacket;
import com.java.health.care.bed.model.DataTransmitter;
import com.java.health.care.bed.model.DevicePacket;
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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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

    //    String path = "/sdcard/Patient/data/"+useId+"-"+dateNowStr+"/";
    private String path = Environment.getExternalStorageDirectory().getPath() + "/Hbed/data/" + "1022" + "-" + "20220727144102" + "/";

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

    private boolean isSendCompleteData = false;    //是否发送了一个完整的包

    private TlvBox tlvBox;

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart()");
        bleCM19Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_CM19_MAC);
        bleCM22Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_CM22_MAC);
        bleSPO2Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_SPO2_MAC);
        bleQSMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_QIANSHAN_MAC);
        bleIRTMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_IRT_MAC);
        bleKYCMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_KYC_MAC);
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
//                    Log.d("fsh===", Arrays.toString(packet));
                    tlvBox = new TlvBox();
                    int len = tlvBox.decodePacket(packet);
//                    Log.d("fsh===",len+"===len");
                    if (len == 0) {

                        byte[] ecgData = tlvBox.getBytesValue(EcgPacket.Ecg.getType());
                        byte[] ppgData = tlvBox.getBytesValue(EcgPacket.PPG.getType());
                        byte[] heartData = tlvBox.getBytesValue(EcgPacket.HeartRate.getType());
                        byte[] szPressData = tlvBox.getBytesValue(EcgPacket.DiaBp.getType());
                        byte[] ssPressData = tlvBox.getBytesValue(EcgPacket.SysBp.getType());
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

                        BPDevicePacket bpDevicePacket = new BPDevicePacket(sEcgData,sPpgData,sEcg,sSzPress,sSsPress);
                        dataTrans.sendData(bpDevicePacket);
//                        Log.d("fsh===", "===sEcgData" + sEcgData.length + "===" + Arrays.toString(sEcgData));
//                        Log.d("fsh===", "===sPpgData" + sPpgData.length + "===" + Arrays.toString(sPpgData));
//                        Log.d("fsh===", "===sHeartData" + sHeartData.length + "===" + Arrays.toString(sHeartData));
//                        Log.d("fsh===", "===sSzPressDataData" + sSzPressDataData.length + "===" + Arrays.toString(sSzPressDataData));
//                        Log.d("fsh===", "===sSsPressDataData" + sSsPressDataData.length + "===" + Arrays.toString(sSsPressDataData));
                        //打印如下：两组数据，失效时为1000。按时间先后填充
                        //===sHeartData2===[75, 1000]
                        //===sSzPressDataData2===[78, 1000]
                        //===sSsPressDataData2===[131, 1000]

                        //===sHeartData2===[1000, 1000]
                        //===sSzPressDataData2===[1000, 1000]
                        //===sSsPressDataData2===[1000, 1000]

                    }

                }
            }
        }

        private void processDataTlv(Queue<byte[]> packets) {

            if (true) {
                for (byte[] packet : packets) {
                    dataTrans.sendData(packet);
                    Log.d(TAG, "===packet" + Arrays.toString(packet));
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

                        byte[] becgData = new byte[length];
                        for (int i = 0; i < length; i++) {
                            becgData[i] = (byte) ((secgData[i] >> 4) & 0xff);
                        }

////////////////////////拼接时间戳8字节+96字节ECG心电数据/////////////////////////////////////////////////////////////////////////////////////////////////
                        byte[] baoEcg = new byte[8+96];
                        byte[] contentEcg = becgData;
                        long time = System.currentTimeMillis();
                        ByteUtil.putLong(baoEcg,time,0);
                        System.arraycopy(contentEcg,0,baoEcg,8,96);
                        Log.d("arraycopy====",Arrays.toString(baoEcg));
                        Log.d("arraycopy====1",Arrays.toString(contentEcg));
                        //写入文件ecg
                        FileIOUtils.writeFileFromBytesByStream(path + "ecgData.ecg", baoEcg, true);
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        int[] irspData = new int[rspData.length / 3];
                        ByteUtil.bbToInts(irspData, rspData);

                        Log.i("methodecg_rsp_int", Arrays.toString(irspData));
                        //写入文件rsp
                        FileIOUtils.writeFileFromBytesByStream(path + "rspData.resp", ByteUtil.get16Bitint(irspData), true);
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

                          /*  if (!getReadingFlag()) {
                                break;
                            }*/
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

                        if (score > 0) {
                            //写入文件score
                            byte[] baos = new byte[4];
                            ByteUtil.intToByte(baos, score, 0);
                            FileIOUtils.writeFileFromBytesByStream(path + "scoreData.score", baos, true);

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
            pkt.respRate = respRate;
            dataTrans.sendData(pkt, battery);

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
        if (event instanceof String) {
            String str = (String) event;
            if (bleDeviceKYC != null) {
                writeKYCBleDevice(bleDeviceKYC, str);
            } else {
                Log.d(TAG, "康养床bleDevice为空了");
            }

        } else{
            List<BleDevice> deviceList = (List<BleDevice>) event;
            if (deviceList != null) {
                Log.d(TAG,"deviceList=="+deviceList.size());
                for (BleDevice bleDevice : deviceList) {
                    String bleMac = bleDevice.getMac();

                    if (bleCM19Mac != null) {
                        if (bleMac.equals(bleCM19Mac)) getCM19BleDevice(bleDevice);

                    }
                    if (bleDevice != null) {
                        if (bleMac.equals(bleCM22Mac)) writeCM22BPBleDevice(bleDevice);
                    }
                    if (bleSPO2Mac != null) {
                        if (bleMac.equals(bleSPO2Mac)) getSpO2BleDevice(bleDevice);

                    }
                    if (bleQSMac != null) {
                        if (bleMac.equals(bleQSMac)) writeBPBleDevice(bleDevice);

                    }
                    if (bleIRTMac != null) {
                        if (bleMac.equals(bleIRTMac)) getIRTBleDevice(bleDevice);
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
                        Log.d(TAG, "cm19打开通知成功");
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
                            int spo2 = DataUtils.getSPO2Data(data);
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
                            Log.d("fshman" + "IRT=======", "dataIRT:" + dataIRT);
                            mapEvent.put(Constant.IRT_DATA, dataIRT);
                            EventBus.getDefault().post(mapEvent);
                        }

                    }
                });
    }


    /**
     * 收到CM22无创连续血压计bleDevice,先写数据
     */

    private void writeCM22BPBleDevice(BleDevice bleDevice) {
        BleManager.getInstance().write(
                bleDevice,
                Constant.UUID_SERVICE_CM22,
                Constant.UUID_CHARA_CM22_WRITE,
                ByteUtil.HexString2Bytes(Constant.Order_BeginMeasure),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        Log.d(TAG, "current:" + current + "==total:" + total + "===byte[]:" + Arrays.toString(justWrite));
                        getCM22BPBleDevice(bleDevice);
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
                        Log.d(TAG, "current:" + current + "==total:" + total + "===byte[]:" + Arrays.toString(justWrite));
                        getBPBleDevice(bleDevice);
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.d(TAG, exception.toString());
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
                        Log.d(TAG, "bp打开通知成功");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        Log.d(TAG, exception.toString() + "bp打开通知失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        Log.d(TAG + "bp=======", Arrays.toString(data));
                        if (data.length == 10) {
                            String bpString = DataUtils.getSBPData(data);
                            Log.d("fshman", "BP===" + bpString);
                            mapEvent.put(Constant.BP_DATA, bpString);
                            EventBus.getDefault().post(mapEvent);
                        } else if (data.length == 6) {
                            //测量失败
                            Log.d("fshman", "BP===测量失败");
                            mapEvent.put(Constant.BP_DATA_ERROR, "BP_ERROR");
                            EventBus.getDefault().post(mapEvent);
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


}
