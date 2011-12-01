package com.avadesign.camtalker;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Base64;
import android.util.Log;

import com.avadesign.codecs.GSM;

public class Recorder extends Thread {
	
	
	private AudioRecord recorder;
	/*
	 * True if thread is running, false otherwise.
	 * This boolean is used for internal synchronization.
	 */
	private boolean isRunning = false;	
	/*
	 * True if thread is safely stopped.
	 * This boolean must be false in order to be able to start the thread.
	 * After changing it to true the thread is finished, without the ability to start it again.
	 */
	private boolean isFinishing = false;
	private boolean isHttpGet = false;

	private Socket socket;
	private short[] pcmFrame = new short[160];
	private byte[] encodedFrame;
	private int iReadSize = 0;
	private int iEncSize = 0;
	
	private String IP;
	private String Account;
	private String Password;
	private int Port;

	public void run() {
		// Set audio specific thread priority
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		Log.d("Recorder", "running!");

		
		while(!isFinishing()) {		
			init();
			while(isRunning()) {
				if(!isHttpGet) //send a http get
				{
					HttpGet_ipcam();
					isHttpGet = true;
				}
				try {		
					// Read PCM from the microphone buffer & encode it
					iReadSize = recorder.read(pcmFrame, 0, 160);
					iEncSize = GSM.encode(pcmFrame, 0, encodedFrame, iReadSize);
					
					OutputStream out = socket.getOutputStream();
					PrintStream ps = new PrintStream(out);
					ps.write(encodedFrame);

				}
				catch(IOException e) {
					Log.d("Recorder", e.toString());
				}	
			}		
		
			release();	
			/*
			 * While is not running block the thread.
			 * By doing it, CPU time is saved.
			 */
			synchronized(this) {
				try {	
					if(!isFinishing())
						this.wait();
				}
				catch(InterruptedException e) {
					Log.d("Recorder", e.toString());
				}
			}					
		}							
	}
	
	private String getB64Auth (String login, String pass) {
		String source=login+":"+pass;
		String ret="Basic "+Base64.encodeToString(source.getBytes(),Base64.URL_SAFE|Base64.NO_WRAP);
		return ret;
	} 
	 
	private void HttpGet_ipcam()
	{
		try {
			InetAddress serverAddr = InetAddress.getByName(IP);
			socket = new Socket(serverAddr, Port);
			OutputStream out = socket.getOutputStream();
			PrintStream ps = new PrintStream(out);
			String strGet = "GET /camera-cgi/audio/transmit.cgi HTTP/1.1\r\nConnection: Keep-Alive\r\nCache-Control: no-cache\r\nAuthorization: " 
						+ getB64Auth(Account, Password) 
						+ "\r\n\r\n";
			ps.write(strGet.getBytes());

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	private void init() {
		encodedFrame = new byte[33];
		isHttpGet = false;
		
    	recorder = new AudioRecord(
    			AudioSource.MIC, 
    			8000, 
    			AudioFormat.CHANNEL_CONFIGURATION_MONO, 
    			AudioFormat.ENCODING_PCM_16BIT, 
    			RECORD_BUFFER_SIZE);		
		recorder.startRecording();                             
	}		


	private void release() {			
		if(recorder!=null) {
			recorder.stop();
			recorder.release();
			
		}
	}
	
	public synchronized boolean isRunning() {
		return isRunning;
	}
	
	public synchronized void resumeAudio(String IP,int Port,String Account,String Password) {	
		this.IP=IP;
		this.Account=Account;
		this.Password=Password;
		this.Port=Port;
		isRunning = true;
		this.notify();
	}
		
	public synchronized void pauseAudio() {				
		isRunning = false;	
		try {
			if(socket.isConnected())
			{
				Log.d("1111111","true");
			}
			socket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	 
		
	public synchronized boolean isFinishing() {
		return isFinishing;
	}
	
	public synchronized void finish() {
		pauseAudio();
		isFinishing = true;		
		this.notify();
	}

	public static final int RECORD_BUFFER_SIZE = Math.max(
			8000, 
			ceil(AudioRecord.getMinBufferSize(
					8000, 
					AudioFormat.CHANNEL_CONFIGURATION_MONO, 
					AudioFormat.ENCODING_PCM_16BIT)));	
	
	private static int ceil(int size) {
		return (int) Math.ceil( ( (double) size / 160 )) * 160;
	}
}
