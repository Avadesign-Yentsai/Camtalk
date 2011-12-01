package com.avadesign.camvideo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity implements Runnable
{
	private EditText acc;
	private EditText pwd;
	private Button login;
	private Button register;
	
	private String TAG = "camtalk/Login";
	
	@Override   
	protected void onCreate(Bundle savedInstanceState) 
	{	
		setContentView(R.layout.login);	
		super.onCreate(savedInstanceState); 
		
		acc=(EditText)findViewById(R.id.login_userAcc);
		pwd=(EditText)findViewById(R.id.login_userPwd);
		login=(Button)findViewById(R.id.login_login);
		register=(Button)findViewById(R.id.login_register);
		
		Bundle bundle=this.getIntent().getExtras();
        String state = bundle.getString("KEY_STATE");
       
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
        	acc.setText(obj.get("acc").toString().toLowerCase());
        	pwd.setText(obj.get("pwd").toString().toLowerCase());
        	Log.d(TAG,acc+","+pwd);
        	
		}
		catch (Exception e) //無法讀取
		{
			Log.d(TAG,e.toString());
		}
		
        
        if(state.equals("2"))
        {
        	Toast.makeText(Login.this, "帳號密碼錯誤,請重新輸入", Toast.LENGTH_SHORT).show();
        	acc.setText("");
        	pwd.setText("");
        }
        else if(state.equals("3"))
        {
        	Toast.makeText(Login.this, "Server Error", Toast.LENGTH_SHORT).show();
        }
        else if(state.equals("4"))
        {
        	Toast.makeText(Login.this, "網路未連線", Toast.LENGTH_SHORT).show();
        }
		
		login.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View V)
        	{
        		writeSetting("Camtalk.cfg");
        		finish();
    			Intent intent = new Intent();
    			intent.setClass(Login.this, Start.class); //轉到Start驗證和下載camlist
    			startActivity(intent);
        		/*
        		boolean b=writeXML("CamTalk.xml",PrintXML.outUserInfoXml(acc.getText().toString().toLowerCase(),pwd.getText().toString().toLowerCase()));
        		
        		if(b)
        		{
        			finish();
        			Intent intent = new Intent();
        			intent.setClass(Login.this, Start.class); //轉到Start驗證和下載camlist
        			startActivity(intent);
        		}
        		
        		else
        		{
        			Toast.makeText(Login.this, "無法儲存帳號資料", Toast.LENGTH_SHORT).show();
        		}
        		*/
        	}
        });
		
		register.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View V)
        	{
        		finish();
    			Intent intent=new Intent();
    			intent.setClass(Login.this, Register.class);
    			startActivity(intent);
        	}
        });
		
		register.setEnabled(false);
	}
	
	
	private void writeSetting(String path)
    {
	    try
	    {
		    OutputStream os = openFileOutput(path,MODE_PRIVATE);
		    OutputStreamWriter osw=new OutputStreamWriter(os);
		    osw.write("{\"acc\":\""+acc.getText().toString().toLowerCase()+"\",\"pwd\":\""+pwd.getText().toString().toLowerCase()+"\",\"alert\":true,\"vibrator\":true,\"media\":true}");
		    osw.close();
		    os.close();
	    }
	    catch(Exception e)
	    {
	    	Log.d(TAG,e.toString());
	    }
	   
    }
	
	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		
	}
}
