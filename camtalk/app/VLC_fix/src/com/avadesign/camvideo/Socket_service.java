package com.avadesign.camvideo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class Socket_service extends Service implements Runnable
{
	private Thread thread;
	private static String TAG = "camtalk/SocketConn_service";
	
	private static final int GET_MOTIOIN_DETECTION_EVENT = 0x0001; 
	private static final int GET_MUTI_LOGING_EVENT = 0x0002;
	
	private int port = 9000; 
	private String Server_Address="centos64.dyndns-free.com";
	
	final static String MY_ACTION = "Socket_service.MY_ACTION";
	public final static String CH_WATCHING = "Socket_service.CH_WATCHING";
	public final static String EVENT_COUNT = "Socket_service.EVENT_COUNT";
	public final static String SS_CONN = "Socket_service.SS_CONN";
	public final static String SS_DISCONN = "Socket_service.SS_DISCONN";
	
	private Socket clientSocket=null;
	private String tmp;
	private String usermail;
	
	private String Watching="";//正在觀看的IPCAM URL
	
	private int closeMD_time = 11000; //收到通知後多久不再彈出通知視窗 /*VLC若遇到沒回應的RTSP必須10秒後才會停止執行緒,在這之間若被onPause會出現錯誤*/
	private int send_test_time = 3000; //多久送出字串測試socket是否存活
	private int recive_test_time = 5000; //送出測試字串後必須在多久的時間內收到回應
	private int reConn_time = 5000; //斷線後多久重新連線
	//private int MDinterval_time = 1000; 
	
	public static int event_count = 0; //已收到但沒通知的事件數量
	public static boolean isConnecting;//是否建立SOCKET連線
	
	private boolean allow_conn_to_socket=false;//允許建立SOCKET
	private static boolean toNotify=false; //是否正收到通知視窗
	private boolean reConn = true; //是否重新連線
	private boolean firstConn = true; //是否第一次連線
	//private boolean inMDinterval = false; //離上一次收到event的時間距離是否< MDinterval_time
	
	private static SimpleDateFormat sdTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private WakeLock wakeLock;
	
	final Handler handler = new Handler() 
	{
		public void handleMessage(Message msg) 
		{
			switch (msg.what) 
            {  
            	case GET_MOTIOIN_DETECTION_EVENT:  
            	{
            		Log.d(TAG,msg.obj.toString());
            		
            		JSONObject obj=(JSONObject) JSONValue.parse(msg.obj.toString());
            		
            		//if(!inMDinterval)
            		//{
            			if(!toNotify)
    					{
                			if(!(obj.get("camURL").toString()).equals(Watching))
                			{
                				toNotify=true; 
                				//inMDinterval=true;
                				
                				//handler.postDelayed(chIMDI, MDinterval_time);
                				
                				Bundle bundle =new Bundle();
            		            bundle.putString("KEY_INFO", msg.obj.toString());
            		              
                        		Intent dialogIntent  = new Intent(getBaseContext(), DialogActivity.class);
                        		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        		dialogIntent.putExtras(bundle);
                        		
                        		getApplication().startActivity(dialogIntent);
                        	}
                		}
            			else
            			{
            				event_count++;
            				
            				//inMDinterval=true;
            				
            				//handler.postDelayed(chIMDI, MDinterval_time);
            				
            				/*
            				Intent intent = new Intent();
            				intent.setAction(Socket_service.EVENT_COUNT);
            				intent.putExtra("EVENT_COUNT", event_count);
            				sendBroadcast(intent);
            				
            				Log.d(TAG,"send event_count "+event_count);
            				*/
            			}
    					
            		//}
            		break; 
					
            	}
            	case GET_MUTI_LOGING_EVENT:  
            	{
            		Bundle bundle =new Bundle();
		            bundle.putString("KEY_INFO", msg.obj.toString());
		              
            		Intent dialogIntent  = new Intent(getBaseContext(), DialogActivity.class);
            		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            		dialogIntent.putExtras(bundle);
            		
            		getApplication().startActivity(dialogIntent);
            		
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
	public void run() 
	{
		isConnecting = false;
		while(reConn)
		{
			write_last_reconn_time(sdTime.format(Calendar.getInstance().getTime()));//記錄最後的重連時間
			
			if(allow_conn_to_socket)
			{
				InetAddress serverIp;
				try
				{
					if(!firstConn) //不是第一次連
					{
						Thread.sleep(reConn_time);//停止一秒再重連
					}
		            
		            serverIp = InetAddress.getByName(Server_Address);
					
					clientSocket = new Socket();
					
					clientSocket.connect(new InetSocketAddress(serverIp, port));
					
			        clientSocket.setSoTimeout(recive_test_time);
			        
					if (!clientSocket.getKeepAlive())
					{
						clientSocket.setKeepAlive(true);
					}
					
					if(clientSocket.isConnected())
		            {
		            	Log.d(TAG,"clientSocket connect");
		            	
		            	isConnecting = true;
		            	
		            	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		            	bw.write("phone:SPLIT:"+usermail+"\n");
		            	bw.flush();
		                
		            }
		            
		            handler.postDelayed(s, send_test_time);
		            
		            // 取得網路輸入串流
		            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		            while (clientSocket.isConnected()) 
		            {
		            	tmp = br.readLine();

		                if(tmp!=null && !tmp.equals("test"))
		                {
		                	/*
		                	if(tmp.equals("Server:mult-loging"))
		                	{
		                		Message m = new Message();  
								m=handler.obtainMessage(GET_MUTI_LOGING_EVENT, tmp);
			                	handler.sendMessage(m);
		                	}
		                	else
		                	{
			                	Message m = new Message();  
								m=handler.obtainMessage(GET_MOTIOIN_DETECTION_EVENT, tmp);
			                	handler.sendMessage(m);
		                	}
		                	*/
		                	
		                	Message m = new Message();  
		                	m=handler.obtainMessage(GET_MOTIOIN_DETECTION_EVENT, tmp);
		                	handler.sendMessage(m);
		                	
		                }
		                else if(tmp.equals("test"))
		                {
		                	Log.d(TAG, "reply from server");
		                }
		                else
		                {
		                	break;
		                }
		            }
		            
		            clientSocket.close();
		            
		            br.close();
		           
		            firstConn=false;
		            
		            handler.removeCallbacks(s);
		            
		            isConnecting = false;
		            
		            Log.d(TAG,"clientSocket close");
		        } 
				catch (Exception e) 
		        {
					isConnecting = false;
					
					firstConn=false;
					
					handler.removeCallbacks(s);
					
					Log.d(TAG,e.toString());
		        }
			}
			else
			{
				try 
				{
					Thread.sleep(1000);
				} catch (InterruptedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private void write_last_reconn_time(String time) 
	{
		try
	    {
		    OutputStream os = openFileOutput("Last_reconn_time.tmp",MODE_PRIVATE);
		    OutputStreamWriter osw=new OutputStreamWriter(os);
		    osw.write(time);
		    osw.close();
		    os.close();
	    }
	    catch(Exception e)
	    {
	    	Log.d(TAG,e.toString());
	    }
		
	}

	public void onCreate()
	{	
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Service");
		wakeLock.acquire();
		
		registerReceiver(myBroadcastReceiver, new IntentFilter(MY_ACTION));
		registerReceiver(myBroadcastReceiver2, new IntentFilter(CH_WATCHING));
		registerReceiver(socket_connect, new IntentFilter(SS_CONN));
		registerReceiver(socket_disconnect, new IntentFilter(SS_DISCONN));
		
	}
	
	public BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			Log.d(TAG,"broadcast receive");
			
			handler.postDelayed(chTN, closeMD_time);
		}        
	};
	
	public BroadcastReceiver myBroadcastReceiver2 = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			Watching = intent.getStringExtra("URL");
			Log.d(TAG,"broadcast2 receive");
			Log.d(TAG,Watching);
		}        
	};
	
	public BroadcastReceiver socket_connect = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			Log.d(TAG,"broadcast socket_connect receive");
			Log.d(TAG,"reConn:"+String.valueOf(reConn));
			
			allow_conn_to_socket = true;
		}        
	};
	
	public BroadcastReceiver socket_disconnect = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			Log.d(TAG,"broadcast socket_disconnect receive");
			allow_conn_to_socket = false;
			try
			{	
				if(clientSocket!=null)
				{
					clientSocket.close();
					clientSocket=null;
				}
			} 
			catch (IOException e) 
			{
				Log.d(TAG, e.toString());
			}
		}        
	};
	
	
	private Runnable chTN = new Runnable() 
	{
        public void run() 
        {
        	Log.d(TAG,"change toNotify to false");
        	
        	toNotify = false;
        }
    };
    /*
    private Runnable chIMDI = new Runnable() 
	{
        public void run() 
        {
        	Log.d(TAG,"change inMDinterval  to false");
        	
        	inMDinterval = false;
        }
    };
	*/
    private Runnable s = new Runnable() //send test to server
	{
        public void run() 
        {
        	Log.d(TAG,"Timer: send msg to server");
        	
        	BufferedWriter bw;
			try 
			{
				bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
				bw.write("test"+"\n");
				bw.flush();
			} 
			catch (IOException e) 
			{
				Log.d(TAG, e.toString());
			}
        	
			handler.postDelayed(this, send_test_time);
        	
        }
    };
    
    @Override
	public void onStart(Intent intent, int startId) 
	{
		super.onStart(intent, startId);
		
		usermail = intent.getStringExtra("usermail");
		
		thread = new Thread(this);
		thread.start();
	}
	
	@Override
	public void onDestroy() 
	{
		try 
		{
			reConn = false;
			handler.removeCallbacks(s);
			clientSocket.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		if (wakeLock != null) 
		{
		    wakeLock.release();
		    wakeLock = null;
		}
		
		super.onDestroy();
	}

	

}
