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
		//Ū��������O�_���b�����
		InputStream XMLinputStream1=null;
		try 
		{
			XMLinputStream1 = this.openFileInput("CamTalk.xml");
			userinfo=ReadXML.readXML(XMLinputStream1,"UserInfo");
			
			userAcc=userinfo[0];
			userPwd=userinfo[1];
			
			Log.d(TAG, userAcc+","+userPwd);
			
			
			
			//��XML����ƴN�n�J����
			String s = DoLogin.doLogin(userAcc,userPwd);
			
			//�n�J���\���HOME
			if(s.equals("true"))
			{
				finish();
    			Intent intent=new Intent();
    			intent.setClass(Start.this, Home.class);
    			startActivity(intent);
			}
			//�n�J�������LOGIN
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
        	
        	//XML��ƿ��~�N�n�D�ϥΪ̿�J�b���K�X
        	finish();
        	Intent intent=new Intent();
        	intent.setClass(Start.this, Login.class);
		    startActivity(intent);
		}
		
	}
	
	
}
