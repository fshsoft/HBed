package com.java.health.care.bed.model;

import com.java.health.care.bed.service.DataReaderService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataTransmitter {

    private static DataTransmitter instance = new DataTransmitter();

    /**
     * 数据接收器列表
     */
    private List<DataReceiver> dataReceivers = new ArrayList<DataReceiver>();

    //DataReaderService status
    private AtomicBoolean bDeviceConnected = new AtomicBoolean(false);
    private AtomicBoolean bServiceRunning = new AtomicBoolean(false);


    public void setDeviceConnected(boolean conneted) {
        bDeviceConnected.set(conneted);
    }

    public boolean getDeviceConnected() {
        //System.out.println("jl getDeviceConnected: " + bDeviceConnected.get());
        return bDeviceConnected.get();
    }

    public void setServiceRunning(boolean servicerunning) {
        bServiceRunning.set(servicerunning);
    }

    public boolean getServiceRunning() {
        //System.out.println("jl getServiceRunning: " + bServiceRunning.get());
        return bServiceRunning.get();
    }


    public static DataTransmitter getInstance() {
        return instance;
    }

    /**
     * 添加一个数据接收器
     */
    public void addDataReceiver(DataReceiver dataReceiver) {
        synchronized (dataReceivers) {
            if (!dataReceivers.contains(dataReceiver)) {
                dataReceivers.add(dataReceiver);
            }
        }
    }

    /**
     * 删除一个数据接收器
     *
     * @param dataReceiver
     */
    public void removeDataReceiver(DataReceiver dataReceiver) {
        synchronized (dataReceivers) {
            dataReceivers.remove(dataReceiver);
        }
    }

    /**
     * 删除数据接收器
     */
    public void clearDataReceivers() {
        synchronized (dataReceivers) {
            dataReceivers.clear();
        }
    }

    public void sendData(DevicePacket packet, int battery) {

        try {

            // 通知接收者
            synchronized (dataReceivers) {
                for (DataReceiver dataReceiver : dataReceivers) {
                    dataReceiver.onDataReceived(packet, battery);
                }
            }

        } catch (Exception ex) {
            //System.out.println("jl DataTransmitter sendData Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    public void sendData(DevicePacket packet) {
        try {

            // 通知接收者
            synchronized (dataReceivers) {
                for (DataReceiver dataReceiver : dataReceivers) {
                    dataReceiver.onDataReceived(packet);
                }
            }

        } catch (Exception ex) {
            //System.out.println("jl DataTransmitter sendData Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void sendData(byte[] packet) {
        try {

            // 通知接收者
            synchronized (dataReceivers) {
                for (DataReceiver dataReceiver : dataReceivers) {
                    dataReceiver.onDataReceived(packet);
                }
            }

        } catch (Exception ex) {
            //System.out.println("jl DataTransmitter sendData Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    public void sendData(BPDevicePacket packet) {
        try {

            // 通知接收者
            synchronized (dataReceivers) {
                for (DataReceiver dataReceiver : dataReceivers) {
                    dataReceiver.onDataReceived(packet);
                }
            }

        } catch (Exception ex) {
            //System.out.println("jl DataTransmitter sendData Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    public void sendData(EstimateRet ret) {
        try {

            // 通知接收者
            synchronized (dataReceivers) {
                for (DataReceiver dataReceiver : dataReceivers) {
                    dataReceiver.onDataReceived(ret);
                }
            }

        } catch (Exception ex) {
            //System.out.println("jl DataTransmitter sendData Exception: " + ex.getMessage());
            ex.printStackTrace();
        }

    }


    /**
     * 通知连接与否
     *
     * @param flag
     */
    public void notifyStatus(int flag, long startTime) {
        try {
            synchronized (dataReceivers) {
                switch (flag) {
                    case DataReaderService.FLAG_START_TO_CONNECT:
                        for (DataReceiver dataReceiver : dataReceivers) {
                            dataReceiver.onStartToConnect();
                        }
                        break;
                    case DataReaderService.FLAG_DISCONNECTED:
                        setDeviceConnected(false);
                        for (DataReceiver dataReceiver : dataReceivers) {
                            dataReceiver.onDeviceDisConnected();
                        }
                        break;
                    case DataReaderService.FLAG_CONNECTED:
                        setDeviceConnected(true);
                        for (DataReceiver dataReceiver : dataReceivers) {
                            dataReceiver.onDeviceConnected(startTime);
                        }
                        break;
                }
            }

        } catch (Exception ex) {
            //System.out.println("jl DataTransmitter notifyStatus Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
