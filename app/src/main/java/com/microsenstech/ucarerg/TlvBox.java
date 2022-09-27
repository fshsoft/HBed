
package com.microsenstech.ucarerg;

import com.java.health.care.bed.util.ByteUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TlvBox {

    private static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    
    private HashMap<Integer, byte[]> mObjects;
    private HashMap<Integer, List<byte[]>> mMuliObjects;
   
    private int mTotalBytes = 0;
    
    private int serial = 0;
    private int patientId = 1188;

    public TlvBox() {
        mObjects = new HashMap<Integer, byte[]>();
        mMuliObjects = new HashMap<Integer, List<byte[]>>();  //存储一个key 对应多个值的情况,第一个之外的其他值（第一个存储在mObjects中）
    }
  
    /*
     *  verison   code    length
     *  char      char    short
     *  preserve          check code
     *  char              short
     * */
    public int decodePacket(byte[] packet)
    {    	
    	int len = ByteBuffer.wrap(packet,2, 2).order(DEFAULT_BYTE_ORDER).getShort();
    	serial =  ByteBuffer.wrap(packet,4, 4).order(DEFAULT_BYTE_ORDER).getInt();
    	patientId = ByteBuffer.wrap(packet,8, 4).order(DEFAULT_BYTE_ORDER).getInt();
    	parse(packet,16,len);    	
		return 0;    	
    }
    
    public int getPacketSerial()
    {
    	return serial;
    }
    
    public int getPatientId()
    {
    	return patientId;
    }
    
    
    static public int getPacketlen(byte[] packet)
    {
    	return ByteBuffer.wrap(packet,2, 2).order(DEFAULT_BYTE_ORDER).getShort();
    }
    
    
    static public boolean isDataPacket(byte[] packet)
    {
    	return (packet[1]==0x2)?true:false;
    }
    
    
    public int parse(byte[] buffer, int offset, int length) {
        
        //TlvBox box = new TlvBox();
        
        int parsed = 0;
        while (parsed +offset < length) {

            short type = ByteBuffer.wrap(buffer,offset + parsed, 2).order(DEFAULT_BYTE_ORDER).getShort();            
            parsed += 2;
            short size = (short) (ByteBuffer.wrap(buffer,offset + parsed, 2).order(DEFAULT_BYTE_ORDER).getShort()-4);
            //System.out.println("type "+ type +" size "+ size);
            parsed += 2;
            byte[] value = new byte[size];
            System.arraycopy(buffer, offset+parsed, value, 0, size);
            if(this.mObjects.containsKey(type))
            {
            	List<byte[]> vlist = this.mMuliObjects.get(type);
            	if(vlist == null)
            	{
            		vlist = new ArrayList<byte[]>();            		
            		vlist.add(value);
            	}
            	else
            	{
            		vlist.add(value);
            	}
            	this.mMuliObjects.put((int) type, vlist);
            }
            else
            {
            	this.putBytesValue(type, value);
            }
            parsed += size;
        }        
        return 0;
    }
    
    public byte[] serialize() {
        int offset = 0;
        byte[] result = new byte[mTotalBytes];                
        Set<Integer> keys = mObjects.keySet();        
        for ( int key : keys) {
            byte[] bytes = mObjects.get(key);
            byte[] type   = ByteBuffer.allocate(2).order(DEFAULT_BYTE_ORDER).putShort((short)key).array();
            byte[] length = ByteBuffer.allocate(2).order(DEFAULT_BYTE_ORDER).putShort((short) (bytes.length+4)).array();
            System.arraycopy(type, 0, result, offset, type.length);
            offset += 2;
            System.arraycopy(length, 0, result, offset, length.length);
            offset += 2;
            System.arraycopy(bytes, 0, result, offset, bytes.length);
            offset += bytes.length;
        }
        
        //多值情况
        keys = mMuliObjects.keySet();        
        for ( int key : keys) {
            List<byte[]> listbytes = mMuliObjects.get(key);
            for(byte[] bytes : listbytes)
            {
	            byte[] type   = ByteBuffer.allocate(2).order(DEFAULT_BYTE_ORDER).putShort((short)key).array();
	            byte[] length = ByteBuffer.allocate(2).order(DEFAULT_BYTE_ORDER).putShort((short) (bytes.length+4)).array();
	            System.arraycopy(type, 0, result, offset, type.length);
	            offset += 2;
	            System.arraycopy(length, 0, result, offset, length.length);
	            offset += 2;
	            System.arraycopy(bytes, 0, result, offset, bytes.length);
	            offset += bytes.length;
            }
        }
        
        
        return result;
    }
    
    public void putByteValue(int type, byte value) {
        byte[] bytes = new byte[1];        
        bytes[0] = value;
        putBytesValue(type, bytes);
    }
        
    public void putShortValue(int type, short value) {
        byte[] bytes = ByteBuffer.allocate(2).order(DEFAULT_BYTE_ORDER).putShort(value).array();
        putBytesValue(type, bytes);
    }
    
    public void putIntValue(int type, int value) {
        byte[] bytes = ByteBuffer.allocate(4).order(DEFAULT_BYTE_ORDER).putInt(value).array();
        putBytesValue(type, bytes);
    }
    
    public void putLongValue(int type, long value) {
        byte[] bytes = ByteBuffer.allocate(8).order(DEFAULT_BYTE_ORDER).putLong(value).array();
        putBytesValue(type, bytes);
    }
    
    public void putFloatValue(int type, float value) {
        byte[] bytes = ByteBuffer.allocate(4).order(DEFAULT_BYTE_ORDER).putFloat(value).array();
        putBytesValue(type, bytes);
    }
    
    public void putDoubleValue(int type, double value) {
        byte[] bytes = ByteBuffer.allocate(8).order(DEFAULT_BYTE_ORDER).putDouble(value).array();
        putBytesValue(type, bytes);
    }
    
    public void putStringValue(int type, String value) {         
        putBytesValue(type, value.getBytes());        
    }

    public void putObjectValue(int type, TlvBox value) {        
        putBytesValue(type, value.serialize());
    }
    
    public void putBytesValue(int type, byte[] value) {
    	if(mObjects.containsKey(type))
    	{
    		List<byte[]> vlist = this.mMuliObjects.get(type);
        	if(vlist == null)
        	{
        		vlist = new ArrayList<byte[]>();            		
        		vlist.add(value);
        	}
        	else
        	{
        		vlist.add(value);
        	}
        	this.mMuliObjects.put((int) type, vlist);
    	}
    	else
    	{
    		 mObjects.put(type, value);
    	     mTotalBytes += value.length + 4;
    	}
       
    }
    
    public Byte getByteValue(int type) {
        byte[] bytes = mObjects.get(type);
        if (bytes == null) {
            return null;
        }
        return bytes[0];
    }
        
    public Short getShortValue(int type) {
        byte[] bytes = mObjects.get(type);
        if (bytes == null) {
            return null;
        }
        return ByteBuffer.wrap(bytes).order(DEFAULT_BYTE_ORDER).getShort();
    }
    
    public Integer getIntValue(int type) {
        byte[] bytes = mObjects.get(type);
        if (bytes == null) {
            return null;
        }
        return ByteBuffer.wrap(bytes).order(DEFAULT_BYTE_ORDER).getInt();
    }
    
    public Long getLongValue(int type) {
        byte[] bytes = mObjects.get(type);
        if (bytes == null) {
            return null;
        }
        return ByteBuffer.wrap(bytes).order(DEFAULT_BYTE_ORDER).getLong();
    }
    
    public Float getFloatValue(int type) {
        byte[] bytes = mObjects.get(type);
        if (bytes == null) {
            return null;
        }
        return ByteBuffer.wrap(bytes).order(DEFAULT_BYTE_ORDER).getFloat();
    }
    
    public Double getDoubleValue(int type) {
        byte[] bytes = mObjects.get(type);
        if (bytes == null) {
            return null;
        }
        return ByteBuffer.wrap(bytes).order(DEFAULT_BYTE_ORDER).getDouble();
    }
    
    /*public TlvBox getObjectValue(int type) {
        byte[] bytes = mObjects.get(type);
        if (bytes == null) {
            return null;
        }
        return TlvBox.parse(bytes, 0, bytes.length);
    }*/
    
    public String getStringValue(int type) {
        byte[] bytes = mObjects.get(type);
        if (bytes == null) {
            return null;
        }
        return new String(bytes).trim();
    }
    
    public byte[] getBytesValue(int type) {
        byte[] bytes = mObjects.get(type);
        return bytes;
    }
    
    public byte[] getByteArrayValue(int type)
    {
    	byte[] bytes = mObjects.get(type);
    	return bytes;
    }
    
    public short[] getShortArrayValue(int type)
    {
    	byte[] bytes = mObjects.get(type);
    	short[] ret = new short[bytes.length/2];
    	for(int i=0;i<bytes.length/2;i++)
    	{
    		byte[] tmp = new byte[2];
    		tmp[0]= bytes[i*2];
    		tmp[1]= bytes[i*2+1];
    		ret[i] = ByteBuffer.wrap(tmp).order(DEFAULT_BYTE_ORDER).getShort();
    	}
    	
    	return ret;
    }
    
    public int[] getIntArrayValue(int type)
    {
    	byte[] bytes = mObjects.get(type);
    	int[] ret = new int[bytes.length/4];
    	for(int i=0;i<bytes.length/4;i++)
    	{
    		byte[] tmp = new byte[4];
    		tmp[0]= bytes[i*2];
    		tmp[1]= bytes[i*2+1];
    		tmp[2]= bytes[i*2+2];
    		tmp[3]= bytes[i*2+3];
    		ret[i] = ByteBuffer.wrap(tmp).order(DEFAULT_BYTE_ORDER).getInt();
    	}
    	
    	return ret;
    }
    
    public List<byte[]> getBytesValues(int type)
    {
    	List<byte[]> vlist = this.mMuliObjects.get(type);
    	if(vlist == null)
    	{
    		vlist = new ArrayList<byte[]>();            		
    		vlist.add(mObjects.get(type));
    	}
    	else
    	{
    		vlist.add(mObjects.get(type));
    	}
    	return vlist;
    }
    
    
    public List<Byte> getByteValues(int type) {
    	List<byte[]> vlist = getBytesValues(type);
    	List<Byte> list = new ArrayList<Byte>();    
    	for(byte[] v : vlist)
    	{
    		list.add(v[0]);
    	}
        return list;
    }
        
    public List<Short> getShortValues(int type) {
    	List<byte[]> vlist = getBytesValues(type);
    	List<Short> list = new ArrayList<Short>();    
    	for(byte[] v : vlist)
    	{
    		list.add( ByteBuffer.wrap(v).order(DEFAULT_BYTE_ORDER).getShort());
    	}
        return list;        
    }
    
    public List<Integer> getIntValues(int type) {
    	List<byte[]> vlist = getBytesValues(type);
    	List<Integer> list = new ArrayList<Integer>();    
    	for(byte[] v : vlist)
    	{
    		list.add( ByteBuffer.wrap(v).order(DEFAULT_BYTE_ORDER).getInt());
    	}
        return list;        
    }
    
    public List<Long> getLongValues(int type) {
    	List<byte[]> vlist = getBytesValues(type);
    	List<Long> list = new ArrayList<Long>();    
    	for(byte[] v : vlist)
    	{
    		list.add( ByteBuffer.wrap(v).order(DEFAULT_BYTE_ORDER).getLong());
    	}
        return list;          
    }
    
    public List<Float> getFloatValues(int type) {
        List<byte[]> vlist = getBytesValues(type);
    	List<Float> list = new ArrayList<Float>();    
    	for(byte[] v : vlist)
    	{
    		list.add( ByteBuffer.wrap(v).order(DEFAULT_BYTE_ORDER).getFloat());
    	}
        return list; 
    }
    
    public List<Double> getDoubleValues(int type) {
        List<byte[]> vlist = getBytesValues(type);
    	List<Double> list = new ArrayList<Double>();    
    	for(byte[] v : vlist)
    	{
    		list.add( ByteBuffer.wrap(v).order(DEFAULT_BYTE_ORDER).getDouble());
    	}
        return list; 
    }
    
    public List<String> getStringValues(int type) {
        List<byte[]> vlist = getBytesValues(type);
    	List<String> list = new ArrayList<String>();    
    	for(byte[] v : vlist)
    	{
    		list.add( new String(v).trim());
    	}
    	return list; 
    }
    
    
}
