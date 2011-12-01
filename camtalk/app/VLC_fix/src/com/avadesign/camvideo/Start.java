package com.avadesign.camvideo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class Start extends Activity implements Runnable
{
	private String userAcc;
	private String userPwd;
	private String mds;
	
	private String TAG="camtalk/Start";
	
	private String[] userinfo = null;
	private String[][] camattr; //[i][j], 第 i 台 IPCAM 的第 j 個屬性
	private String[] caminfo = null; //個別一台 IPCAM 的屬性集合
	
	private boolean network;

	@Override   
	protected void onCreate(Bundle savedInstanceState) 
	{	
		setContentView(R.layout.start);	
		super.onCreate(savedInstanceState); 
		
		Thread thread = new Thread(this);
		thread.start();
	}
	
	@Override
	public void run() 
	{
		//刪除舊有的CAM資料 //無效
		File path=getExternalFilesDir(null);
		File f = new File(path, "/CamInfo.xml");
		Log.d(TAG, String.valueOf(f.exists()));
		if(f.exists())
		{
			f.delete();
		}
		
		network = haveInternet(); //檢查網路連接狀態
		
		if(!network)
		{
			finish();
			
			Bundle bundle =new Bundle();
        	bundle.putString("KEY_STATE", "4");
        	
        	Intent intent=new Intent();
        	intent.setClass(Start.this, Login.class);
        	intent.putExtras(bundle);
		    startActivity(intent);
		}
		else
		{
			//從手機讀取帳號密碼資料
			InputStream is=null;
			try 
			{
	        	is=this.openFileInput("Camtalk.cfg");
	        	BufferedReader br = new BufferedReader(new InputStreamReader(is));
	        	
	        	StringBuffer sb = new StringBuffer("");
	        	
	        	String line = null;
	        	
	        	while((line = br.readLine()) != null)
	        	{
	        		sb.append(line);
	        	}
	        	
	        	JSONObject obj=(JSONObject) JSONValue.parse(sb.toString());
	        	userAcc = obj.get("acc").toString().toLowerCase();
	        	userPwd = obj.get("pwd").toString().toLowerCase();
	        	
	        	Log.d(TAG, userAcc+","+userPwd);
	        	
	        	String s = DoLogin.doLogin(userAcc,userPwd);
	        	
	        	//驗證成功
				if(s.equals("true"))
				{
					//Socket Listener Service
			        Intent intent_s = new Intent(Start.this, Socket_service.class);
			        intent_s.putExtra("usermail", userAcc);
			        intent_s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			        startService(intent_s);
			        
					//從資料庫讀取IP Cam的資料
					camattr = LoadCamlist.load(userAcc, userPwd);
			       
					if(camattr!=null)
					{
						 String[] attrName = {"CamID","CamName","CamTalkAc","CamTalkPw","CamIP","CamTalkPort","CamVideoPort","CamVideoCode"};
						 writeXML("CamInfo.xml",PrintXML.outCamInfoXml(attrName,camattr));
						 Log.d(TAG,PrintXML.outCamInfoXml(attrName,camattr));
					}
					
					loadSetting();
					
					//頁面轉到Home
					finish();
	    			Intent intent=new Intent();
	    			intent.setClass(Start.this, Home.class);
	    			startActivity(intent);
	    			
				}
				//登入失敗轉到LOGIN
				else if(s.equals("false"))
				{
					finish();
					
					Bundle bundle =new Bundle();
		        	bundle.putString("KEY_STATE", "2");
		        	
		        	Intent intent=new Intent();
		        	intent.setClass(Start.this, Login.class);
		        	intent.putExtras(bundle);
				    startActivity(intent);
				}
				else
				{
					finish();
					
					Bundle bundle =new Bundle();
		        	bundle.putString("KEY_STATE", "3");
		        	Intent intent=new Intent();
		        	intent.setClass(Start.this, Login.class);
		        	intent.putExtras(bundle);
				    startActivity(intent);
				}
				
			}
			catch (Exception e) //無法讀取
			{
				Log.d(TAG, e.toString());
	        	
	        	finish();
	        	
	        	Bundle bundle =new Bundle();
	        	bundle.putString("KEY_STATE", "1");
	        	 
	        	Intent intent=new Intent();
	        	intent.setClass(Start.this, Login.class);
	        	intent.putExtras(bundle);
			    startActivity(intent);
			}
			/*
			//讀取手機內是否有帳號資料
			InputStream XMLinputStream1=null;
			try 
			{
				XMLinputStream1 = this.openFileInput("CamTalk.xml");
				userinfo=ReadXML.readUserInfoXML(XMLinputStream1,"UserInfo");
				
				userAcc=userinfo[0];
				userPwd=userinfo[1];
				
				Log.d(TAG, userAcc+","+userPwd);
				
				//有XML的資料就登入驗證
				String s = DoLogin.doLogin(userAcc,userPwd);
				
				//驗證成功
				if(s.equals("true"))
				{
					//Socket Listener Service
			        Intent intent_s = new Intent(Start.this, Socket_service.class);
			        intent_s.putExtra("usermail", userAcc);
			        intent_s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			        startService(intent_s);
			        
					//從資料庫讀取IP Cam的資料
					camattr = LoadCamlist.load(userAcc, userPwd);
			       
					if(camattr!=null)
					{
						 String[] attrName = {"CamID","CamName","CamTalkAc","CamTalkPw","CamIP","CamTalkPort","CamVideoPort","CamVideoCode"};
						 writeXML("CamInfo.xml",PrintXML.outCamInfoXml(attrName,camattr));
						 Log.d(TAG,PrintXML.outCamInfoXml(attrName,camattr));
					}
					
					loadSetting();
					
					//頁面轉到Home
					finish();
	    			Intent intent=new Intent();
	    			intent.setClass(Start.this, Home.class);
	    			startActivity(intent);
	    			
				}
				//登入失敗轉到LOGIN
				else if(s.equals("false"))
				{
					finish();
					
					Bundle bundle =new Bundle();
		        	bundle.putString("KEY_STATE", "2");
		        	
		        	Intent intent=new Intent();
		        	intent.setClass(Start.this, Login.class);
		        	intent.putExtras(bundle);
				    startActivity(intent);
				}
				else
				{
					finish();
					
					Bundle bundle =new Bundle();
		        	bundle.putString("KEY_STATE", "3");
		        	Intent intent=new Intent();
		        	intent.setClass(Start.this, Login.class);
		        	intent.putExtras(bundle);
				    startActivity(intent);
				}
				
			}
	        catch (Exception e) //無法讀取XML
			{
	        	Log.d(TAG, e.toString());
	        	
	        	finish();
	        	
	        	Bundle bundle =new Bundle();
	        	bundle.putString("KEY_STATE", "1");
	        	 
	        	Intent intent=new Intent();
	        	intent.setClass(Start.this, Login.class);
	        	intent.putExtras(bundle);
			    startActivity(intent);
			}
			*/
		}
	}
	
	public boolean writeXML(String path,String txt)
    {
	    try
	    {
		    OutputStream os = openFileOutput(path,MODE_PRIVATE);
		    OutputStreamWriter osw=new OutputStreamWriter(os);
		    osw.write(txt);
		    osw.close();
		    os.close();
	    }
	    catch(FileNotFoundException e)
	    {
	    	return false;
	    }catch(IOException e)
	    {
	    	return false;
	    }
	    return true;
    }
	
	private boolean haveInternet()
    {
	     boolean result = false;
	     ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	     NetworkInfo info=connManager.getActiveNetworkInfo();
	     if (info == null || !info.isConnected())
	     {
	    	 result = false;
	     }
	     else 
	     {
	    	 if (!info.isAvailable())
		     {
		    		 result =false;
		     }
		     else
		     {
		    	 result = true;
		     }
	     }
    
     	return result;
    }
	private void loadSetting() 
	{
		
		String[] input = new String[5];
        input[0]="http://centos64.dyndns-free.com:8080/camtalk/getMDS.jsp";
        input[1]="Email";
        input[2]=userAcc;
        input[3]="Pwd";
        input[4]=userPwd;
        
        mds = EregiReplace.eregi_replace("(\r\n|\r|\n|\n\r| |)", "",HttpPostResponse.getHttpResponse(input));
        
        if(mds.equals("on"))
        {
        	Intent intent = new Intent();
    		intent.setAction(Socket_service.SS_CONN); 
    		sendBroadcast(intent);
        }
        else
        {
        	Intent intent = new Intent();
    		intent.setAction(Socket_service.SS_DISCONN);
    		sendBroadcast(intent);
        }
		        
	}
}
