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

public class Register extends Activity 
{
	private EditText acc;
	private EditText pwd;
	private EditText rePwd;
	private Button register;
	
	private String TAG = "Register";
	
	@Override   
	protected void onCreate(Bundle savedInstanceState) 
	{	
		setContentView(R.layout.register);	
		super.onCreate(savedInstanceState); 
		
		acc=(EditText)findViewById(R.id.register_userAcc);
		pwd=(EditText)findViewById(R.id.register_userPwd);
		rePwd=(EditText)findViewById(R.id.register_reuserPwd);
		register=(Button)findViewById(R.id.register_register);

		
		register.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View V)
        	{
        		//未加上輸入限制和驗證
        		
        		if((pwd.getText().toString()).equals(rePwd.getText().toString()) || pwd.getText().toString().equals(""))//兩次輸入密碼相等
        		{
        			String response=doRegister(acc.getText().toString(),pwd.getText().toString());
            		
            		if(response.equals("true"))
            		{
            			boolean b=writeXML("CamTalk.xml",PrintXML.outUserInfoXml(acc.getText().toString(),pwd.getText().toString()));
            			
            			Log.d(TAG,"Write XML:"+b);
            			
            			finish();
            			Intent intent=new Intent();
            			intent.setClass(Register.this, Start.class);
            			startActivity(intent);
            		}
            		else if(response.equals("invaidEmail"))
            		{
            			Toast.makeText(Register.this, "Email has registered", Toast.LENGTH_LONG).show();
            			acc.requestFocus();
            		}
            		else
            		{
            			Toast.makeText(Register.this, "Server Error", Toast.LENGTH_LONG).show();
            		}
        		}
        		else//兩次輸入密碼不相等
        		{
        			Toast.makeText(Register.this, "Recomfirm Password", Toast.LENGTH_LONG).show();
        			pwd.setText("");
        			rePwd.setText("");
        			pwd.requestFocus();
        		}
        		
        	}
        });
	}
	
	private String doRegister(String Email, String Pwd) 
	{
		String TAG="doRegister";
		
		String[] input = new String[5];
		
		input[0] = ServerInfo.getWebPath()+"register.jsp";
		input[1] = "Email";
		input[2] = Email;
		input[3] = "Pwd";
		input[4] = Pwd;
		
		String re = HttpPostResponse.getHttpResponse(input);
		re=EregiReplace.eregi_replace("(\r\n|\r|\n|\n\r| |)", "", re);
		
		if(re.equals("invaidEmail"))
		{
			Log.d(TAG, Email+" has registered");
			return "invaidEmail";
		}
		else if(re.equals("true"))
		{
			Log.d(TAG, Email+" regist success");
			return "true";
		}
		else
		{
			Log.d(TAG, "Server Error:"+re);
			return "error";
		}
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
}
