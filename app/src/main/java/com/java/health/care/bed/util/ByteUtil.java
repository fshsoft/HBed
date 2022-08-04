package com.java.health.care.bed.util;


/**
 * 
 * <ul>
 * <li>文件名称: com.born.util.ByteUtil.java</li>
 * <li>文件描述: byte转换工具</li>
 * <li>版权所有: 版权所有(C)2001-2006</li>
 * <li>公 司: bran</li>
 * <li>内容摘要:</li>
 * <li>其他说明:</li>
 * <li>完成日期：2011-7-18</li>
 * <li>修改记录0：无</li>
 * </ul>
 * 
 * @version 1.0
 * @author 许力多
 */
public class ByteUtil {
	/**
	 * 转换short为byte
	 * 
	 * @param b
	 * @param s
	 *            需要转换的short
	 * @param index
	 */
	public static void putShort(byte b[], short s, int index) {
		b[index + 1] = (byte) ((s >> 8) & 0xff);
		b[index + 0] = (byte) ((s >> 0) & 0xff);
	}

	
	
	/**
	 * 通过从蓝牙读取的ECG byte数组取到short数组
	 * 
	 * @param 
	 * @param 
	 *            
	 * @return
	 */
	public static void bbTobEcg3(byte[] bd, byte[] bs) {		
		int len = bd.length;			
		for(int i = 0; i < len; i ++) {
			int tmp  = (((bs[2*i] & 0xf) << 8) | (bs[2*i +1] & 0xff));
			bd[i] = (byte)(tmp>>4);
		}
	}

	/**
	 * 通过从蓝牙读取的ACC byte数组取到short数组
	 * 
	 * @param 
	 * @param 
	 *            
	 * @return
	 */
	public static void bbTobAcc3(byte[] bd, byte[] bs) {		
		int len = bd.length;			
		for(int i = 0; i < len; i ++) {
			//int tmp  = ((bs[2*i] << 8) | (bs[2*i +1] & 0xff));
			//System.out.println("jl bbTobAcc short: " + tmp);
			//bd[i] = (byte)((tmp >> 8) & 0xff) ;
			bd[i] = bs[2*i+1];
		}
	}
	
	/**
	 * 通过从蓝牙读取的ECG byte数组取到short数组
	 * 
	 * @param 
	 * @param 
	 *            
	 * @return
	 */
	public static int bbTobElec3(byte[] bs) {		
	
		int ret = (((bs[0] << 8)/* & 0xff00*/) | (bs[1] & 0xff));
		return ret;
	    
	}
	
	/**
	 * 通过从蓝牙读取的ECG byte数组取到short数组
	 * 
	 * @param 
	 * @param 
	 *            
	 * @return
	 */
	public static void bbToEcg1(byte[] bd, byte[] bs) {		
		int len = bd.length;			
		for(int i = 0; i < len; i ++) {
			int tmp  = (((bs[2*i] & 0xf) << 8) | (bs[2*i +1] & 0xff));
			bd[i] = (byte)(tmp>>4);
		}
	}
	
	
	public static void bbToAcc1(byte[] bd, byte[] bs) {
		int len = bd.length;			
		for(int i = 0; i < len; i ++) {
			bd[i] = (byte)(bs[2*i +1] & 0xff);
		}
	}
	
	/**
	 * 通过从蓝牙读取的byte数组取到short数组
	 * 
	 * @param 
	 * @param 
	 *            
	 * @return
	 */
	public static void bbToShorts(short[] s, byte[] b) {		
		int len = s.length;			
		for(int i = 0; i < len; i ++) {
			s[i]  =(short) (((b[2*i] << 8) & 0xff00) | (b[2*i +1] & 0xff));	
		}
	}
	
	/**
	 * 转换int为byte数组
	 * 
	 * @param bb
	 * @param x
	 * @param index
	 */
	public static void putInt(byte[] bb, int x, int index) {
		bb[index + 0] = (byte) ((x >> 24)  & 0xff);
		bb[index + 1] = (byte) ((x >> 16)  & 0xff);
		bb[index + 2] = (byte) ((x >> 8)  & 0xff);
		bb[index + 3] = (byte) ((x >> 0)  & 0xff);
	}

	/**
	 * 通过byte数组取到int
	 * 
	 * @param bb
	 * @param index
	 *            第几位开始
	 * @return
	 */
	public static int getInt(byte[] bb, int index) {
		return (int) ((((bb[index + 3] & 0xff) << 24)
				| ((bb[index + 2] & 0xff) << 16)
				| ((bb[index + 1] & 0xff) << 8) | ((bb[index + 0] & 0xff) << 0)));
	}

	/**
	 * 转换long型为byte数组
	 * 
	 * @param bb
	 * @param x
	 * @param index
	 */
	public static void putLong(byte[] bb, long x, int index) {
		bb[index + 7] = (byte) ((x >> 56) & 0xff);
		bb[index + 6] = (byte) ((x >> 48) & 0xff);
		bb[index + 5] = (byte) ((x >> 40) & 0xff);
		bb[index + 4] = (byte) ((x >> 32) & 0xff);
		bb[index + 3] = (byte) ((x >> 24) & 0xff);
		bb[index + 2] = (byte) ((x >> 16) & 0xff);
		bb[index + 1] = (byte) ((x >> 8) & 0xff);
		bb[index + 0] = (byte) ((x >> 0) & 0xff);
	}

	/**
	 * 通过byte数组取到long
	 * 
	 * @param bb
	 * @param index
	 * @return
	 */
	public static long getLong(byte[] bb, int index) {
		return ((((long) bb[index + 7] & 0xff) << 56)
				| (((long) bb[index + 6] & 0xff) << 48)
				| (((long) bb[index + 5] & 0xff) << 40)
				| (((long) bb[index + 4] & 0xff) << 32)
				| (((long) bb[index + 3] & 0xff) << 24)
				| (((long) bb[index + 2] & 0xff) << 16)
				| (((long) bb[index + 1] & 0xff) << 8) | (((long) bb[index + 0] & 0xff) << 0));
	}

	/**
	 * 字符到字节转换
	 * 
	 * @param ch
	 * @return
	 */
	public static void putChar(byte[] bb, char ch, int index) {
		int temp = (int) ch;
		// byte[] b = new byte[2];
		for (int i = 0; i < 2; i++) {
			bb[index + i] = new Integer(temp & 0xff).byteValue(); // 将最高位保存在最低位
			temp = temp >> 8; // 向右移8位
		}
	}

	/**
	 * 字节到字符转换
	 * 
	 * @param b
	 * @return
	 */
	public static char getChar(byte[] b, int index) {
		int s = 0;
		if (b[index + 1] > 0)
			s += b[index + 1];
		else
			s += 256 + b[index + 0];
		s *= 256;
		if (b[index + 0] > 0)
			s += b[index + 1];
		else
			s += 256 + b[index + 0];
		char ch = (char) s;
		return ch;
	}

	/**
	 * float转换byte
	 * 
	 * @param bb
	 * @param x
	 * @param index
	 */
	public static void putFloat(byte[] bb, float x, int index) {
		// byte[] b = new byte[4];
		int l = Float.floatToIntBits(x);
		for (int i = 0; i < 4; i++) {
			bb[index + i] = new Integer(l).byteValue();
			l = l >> 8;
		}
	}

	/**
	 * 通过byte数组取得float
	 * 
	 * @param bb
	 * @param index
	 * @return
	 */
	public static float getFloat(byte[] b, int index) {
		int l;
		l = b[index + 0];
		l &= 0xff;
		l |= ((long) b[index + 1] << 8);
		l &= 0xffff;
		l |= ((long) b[index + 2] << 16);
		l &= 0xffffff;
		l |= ((long) b[index + 3] << 24);
		return Float.intBitsToFloat(l);
	}

	/**
	 * double转换byte
	 * 
	 * @param bb
	 * @param x
	 * @param index
	 */
	public static void putDouble(byte[] bb, double x, int index) {
		// byte[] b = new byte[8];
		long accum = Double.doubleToRawLongBits(x);
		bb[7 + index] = (byte) (accum & 0xFF);
		bb[6 + index] = (byte) ((accum >> 8) & 0xFF);
		bb[5 + index] = (byte) ((accum >> 16) & 0xFF);
		bb[4 + index] = (byte) ((accum >> 24) & 0xFF);
		bb[3 + index] = (byte) ((accum >> 32) & 0xFF);
		bb[2 + index] = (byte) ((accum >> 40) & 0xFF);
		bb[1 + index] = (byte) ((accum >> 48) & 0xFF);
		bb[0 + index] = (byte) ((accum >> 56) & 0xFF);
	}

	/**
	 * 通过byte数组取得float
	 * 
	 * @param bb
	 * @param index
	 * @return
	 */
	public static double getDouble(byte[] b, int index) {
		long l;
		l = b[0];
		l &= 0xff;
		l |= ((long) b[1] << 8);
		l &= 0xffff;
		l |= ((long) b[2] << 16);
		l &= 0xffffff;
		l |= ((long) b[3] << 24);
		l &= 0xffffffffL;
		l |= ((long) b[4] << 32);
		l &= 0xffffffffffL;
		l |= ((long) b[5] << 40);
		l &= 0xffffffffffffL;
		l |= ((long) b[6] << 48);
		l &= 0xffffffffffffffL;
		l |= ((long) b[7] << 56);
		return Double.longBitsToDouble(l);
	}
	
	/** 
	   * @param b byte[] 
	   * @return String 
	   */  
	   public static String Bytes2HexString(byte[] b) {  
	    String ret = "";  
	    for (int i = 0; i < b.length; i++) {  
	     String hex = Integer.toHexString(b[i] & 0xFF);  
	     if (hex.length() == 1) {  
	      hex = '0' + hex;  
	     }  
	     ret += hex.toUpperCase();  
	    }  
	    return ret;  
	   }  
	   /** 
	   * 将两个ASCII字符合成一个字节； 如："EF"--> 0xEF 
	   * @param src0 byte 
	   * @param src1 byte 
	   * @return byte 
	   */  
	   public static byte uniteBytes(byte src0, byte src1) {  
	    byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))  
	      .byteValue();  
	    _b0 = (byte) (_b0 << 4);  
	    byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))  
	      .byteValue();  
	    byte ret = (byte) (_b0 ^ _b1);  
	    return ret;  
	   }  
	   /** 
	   * 将指定字符串src，以每两个字符分割转换为16进制形式 如："2B44EFD9" --> byte[]{0x2B, 0x44, 0xEF, 0xD9} 
	   * @param src String 
	   * @return byte[] 
	   */  
	   public static byte[] HexString2Bytes(String src) {  
	    byte[] ret = new byte[src.length()/2];  
	    byte[] tmp = src.getBytes();  
	    for (int i = 0; i < ret.length; i++) {  
	     ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);  
	    }  
	    return ret;  
	   }
	   
		public static String getIsoString(byte[] b) {
			String srt = null;
			try {
				srt = new String(b, "ISO-8859-1");				
			} catch ( Exception e) {
				e.printStackTrace();
			}
			return srt;
		}

		public static byte[] putIsoString(String str) {
			byte[] b = null;
			try {
				b = str.getBytes("ISO-8859-1");				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return b;
		}
	

	public static String getString(byte[] b) {
		String srt = null;
		try {
			//srt = new String(b, "ISO-8859-1");
			srt = Bytes2HexString(b);
		} catch ( Exception e) {
			e.printStackTrace();
		}
		return srt;
	}

	public static byte[] putString(String str) {
		byte[] b = null;
		try {
			//b = str.getBytes("ISO-8859-1");
			b = HexString2Bytes(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return b;
	}

	/**
	 * 将16进制字符串转换为byte[]
	 *
	 * @param str
	 * @return
	 */
	public static byte[] toBytes(String str) {
		if(str == null || str.trim().equals("")) {
			return new byte[0];
		}

		byte[] bytes = new byte[str.length() / 2];
		for(int i = 0; i < str.length() / 2; i++) {
			String subStr = str.substring(i * 2, i * 2 + 2);
			bytes[i] = (byte) Integer.parseInt(subStr, 16);
		}

		return bytes;
	}
}
