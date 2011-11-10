package com.avadesign.camvideo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class Socket_service extends Service implements Runnable
{
	private Thread thread;
	private static String TAG = "SocketConn_service";
	
	private int port = 9000; //接受server訊息的port
	
	private static final int UPDATE_SETTING_SUCCESS = 0x0001; 
	private String from_Address;
	private String Server_Address="centos64.dyndns-free.com";
	private static boolean toNotify=false;
	private String CamInfo;
	
	final static String MY_ACTION = "Socket_service.MY_ACTION";
	
	final Handler handler = new Handler() 
	{
		public void handleMessage(Message msg) 
		{
			switch (msg.what) 
            {  
            	case UPDATE_SETTING_SUCCESS:  
            	{
            		if(!toNotify)
					{
            			
            			toNotify=true; //避免密集傳送 //這樣處理會有漏洞
    					
            			Bundle bundle =new Bundle();
    		            bundle.putString("KEY_INFO", CamInfo);
    		                
                		Intent dialogIntent  = new Intent(getBaseContext(), DialogActivity.class);
                		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                		dialogIntent.putExtras(bundle);
                		
                		getApplication().startActivity(dialogIntent);
                		
                		
					}
            		break;  
            	}
            }  
            super.handleMessage(msg);  
		}

	};
	
	
	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onStart(Intent intent, int startId) 
	{
		super.onStart(intent, startId);
		
		thread = new Thread(this);
		thread.start();
		
		
	}
	
	@Override
	public void onDestroy() 
	{
		
		super.onDestroy();
	}

	@Override
	public void run() 
	{
		Log.d(TAG,"Socket Listener Start");
		toNotify=false;
		try 
		{
			Socket sk = null;
			
			ServerSocket ss = new java.net.ServerSocket(port);
			
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			
			while (true)
			{
				// 等待連入
				System.out.println("waiting...");
				
				// 取得連線Socket 
				sk = ss.accept();
				
				Log.d(TAG,sk.toString());
				
				// 取得來源Address
				from_Address=(sk.getInetAddress().toString()).replace("/", "");
								
				BufferedReader br = new BufferedReader(new InputStreamReader(sk.getInputStream(),"UTF8"));
				CamInfo = br.readLine(); 
				
				// 如果來源是伺服器ip才發送訊息
				if(from_Address.equals(InetAddress.getByName(Server_Address).getHostAddress()))
				{
					Message m = new Message();  
					m=handler.obtainMessage(UPDATE_SETTING_SUCCESS,sk.getInetAddress().toString());
					handler.sendMessage(m);  
				}
				sk.close();
				
			}
			
        } 
		catch(Exception e)
		{
			Log.d(TAG,e.toString());
		}
		
	}
	
	public void onCreate()
	{
		registerReceiver(myBroadcastReceiver, new IntentFilter(MY_ACTION));
	}
	
	public BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			Log.d(TAG,"broadcast receive");
			toNotify = false;
		}        
	};

	

}
