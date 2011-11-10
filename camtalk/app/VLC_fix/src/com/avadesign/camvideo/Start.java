package com.avadesign.camvideo;

import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Start extends Activity implements Runnable
{
	private String[] userinfo = null;
	
	private String userAcc;
	private String userPwd;
	
	private String TAG="Start";
	
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
		//讀取手機內是否有帳號資料
		InputStream XMLinputStream1=null;
		try 
		{
			XMLinputStream1 = this.openFileInput("CamTalk.xml");
			userinfo=ReadXML.readXML(XMLinputStream1,"UserInfo");
			
			userAcc=userinfo[0];
			userPwd=userinfo[1];
			
			Log.d(TAG, userAcc+","+userPwd);
			
			
			
			//有XML的資料就登入驗證
			String s = DoLogin.doLogin(userAcc,userPwd);
			
			//登入成功轉到HOME
			if(s.equals("true"))
			{
				finish();
    			Intent intent=new Intent();
    			intent.setClass(Start.this, Home.class);
    			startActivity(intent);
			}
			//登入失敗轉到LOGIN
			else
			{
				finish();
	        	Intent intent=new Intent();
	        	intent.setClass(Start.this, Login.class);
			    startActivity(intent);
			}
			
		}
        catch (Exception e) 
		{
        	Log.d(TAG, e.toString());
        	
        	//XML資料錯誤就要求使用者輸入帳號密碼
        	finish();
        	Intent intent=new Intent();
        	intent.setClass(Start.this, Login.class);
		    startActivity(intent);
		}
		
	}
	
	
}
