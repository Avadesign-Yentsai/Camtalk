package com.avadesign.camvideo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

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
	
	private String response;
	
	private String TAG = "Login";
	
	@Override   
	protected void onCreate(Bundle savedInstanceState) 
	{	
		setContentView(R.layout.login);	
		super.onCreate(savedInstanceState); 
		
		acc=(EditText)findViewById(R.id.login_userAcc);
		pwd=(EditText)findViewById(R.id.login_userPwd);
		login=(Button)findViewById(R.id.login_login);
		register=(Button)findViewById(R.id.login_register);
		
		login.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View V)
        	{
        		response = DoLogin.doLogin(acc.getText().toString(),pwd.getText().toString());
        		if(response.equals("true"))
        		{
        			//帳號密碼正確,寫入XML存到手機
        			boolean b=writeXML("CamTalk.xml",PrintXML.outXml(acc.getText().toString(),pwd.getText().toString()));
        			
        			Log.d("TAG","Write XML:"+b);
        			
        			finish();
        			Intent intent=new Intent();
        			intent.setClass(Login.this, Home.class);
        			startActivity(intent);
        		}
        		else if(response.equals("false"))
        		{
        			Toast.makeText(Login.this, "Invalid Account or password", Toast.LENGTH_SHORT).show();
        			acc.requestFocus();
        		}
        		else
        		{
        			Toast.makeText(Login.this, "Server Error", Toast.LENGTH_SHORT).show();
        		}
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
	}
	
	private boolean writeXML(String path,String txt)
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

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
