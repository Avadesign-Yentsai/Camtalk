package com.avadesign.codecs;

public class GSM {

	static {
		System.loadLibrary("gsm_jni");
	}

 	
	public static native int open();
	public static native int decode(byte encoded[], short lin[], int size);
	public static native int encode(short lin[], int offset, byte encoded[], int size);
	public static native void close();
	

}