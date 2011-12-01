package com.avadesign.camvideo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class Home extends Activity 
{
	
	private String TAG="camtalk/Home";
	
	private String userAcc;
	
	private ImageButton camlist_btn;
	private ImageButton setting_btn;
	private ImageButton eventlog_btn;
	private ImageButton info_btn;
	private ImageButton exit_btn;
	
	private ImageView ss;
	
	private TextView mail_txt; //顯示目前使用者的帳號
	private TextView ec; //event_count
	
	private Handler handler = new Handler();
	
	@Override   
	protected void onCreate(Bundle savedInstanceState) 
	{	
		setContentView(R.layout.home);	
		super.onCreate(savedInstanceState); 
		
		handler.post(check_ss);//查看Socket狀態
		handler.post(check_ec);//查看無通知的事件數量
		
		setting_btn=(ImageButton)findViewById(R.id.home_setting);
		camlist_btn=(ImageButton)findViewById(R.id.home_camlist);
		eventlog_btn=(ImageButton)findViewById(R.id.home_eventlog);
		info_btn=(ImageButton)findViewById(R.id.home_info);
		exit_btn=(ImageButton)findViewById(R.id.home_exit);
		mail_txt=(TextView)findViewById(R.id.home_mail);
		ss=(ImageView)findViewById(R.id.home_ss);
		ec=(TextView)findViewById(R.id.home_event_count);
		
		load_ac_pwd();//讀取手機內存的帳號密碼
		load_last_reconn_time();//讀取最後的重連時間//20111201 debug用
		
		mail_txt.setText(userAcc);
        
        camlist_btn.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View V)
        	{
        		Intent intent = new Intent();
        		intent.setClass(Home.this, Camlist.class);
        		startActivity(intent);
        	}
        });
        
        setting_btn.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View V)
        	{
        		Intent intent = new Intent();
        		intent.setClass(Home.this, Setting.class);
        		startActivity(intent);
        	}
        });
        
        eventlog_btn.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View V)
        	{
        		Bundle bundle =new Bundle();
        		bundle.putString("KEY_DATE", "group");
        		
        		Intent intent = new Intent();
        		intent.putExtras(bundle);
        		intent.setClass(Home.this, EventLoglist.class);
        		startActivity(intent);
        	}
        });
        
        info_btn.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View V)
        	{
        		AlertDialog.Builder builder = new AlertDialog.Builder(Home.this); 
        		builder.setIcon(R.drawable.icon);
        		builder.setTitle("About HomeGuard");
        		builder.setMessage(" ");
        		builder.setPositiveButton("確定", new DialogInterface.OnClickListener() 
        		{  
        			public void onClick(DialogInterface dialog, int whichButton) 
        			{  

        	        }  

        	    });  
        		builder.create().show();  
        	}
        });
        
        exit_btn.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View V)
        	{
        		Home.this.moveTaskToBack(true);
        	}
        });
        
	}
	
	
    
	private void load_last_reconn_time() 
	{
		InputStream is=null;
        try 
		{
        	is=this.openFileInput("Last_reconn_time.tmp");
        	
        	BufferedReader br = new BufferedReader(new InputStreamReader(is));
        	
        	StringBuffer sb = new StringBuffer("");
        	
        	String line = null;
        	
        	while((line = br.readLine()) != null)
        	{
        		sb.append(line);
        	}
        	
        	Log.d(TAG, "reConn_Time:"+sb.toString());
        	
		}
        catch (Exception e) 
		{
        	Log.d(TAG, e.toString());
        	
        	finish();
        	Intent intent=new Intent();
        	intent.setClass(Home.this, Login.class);
		    startActivity(intent);
		}
	}



	private void load_ac_pwd() 
	{
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
        	userAcc=obj.get("acc").toString();
        	
		}
        catch (Exception e) 
		{
        	Log.d(TAG, e.toString());
        	
        	finish();
        	Intent intent=new Intent();
        	intent.setClass(Home.this, Login.class);
		    startActivity(intent);
		}
	}

	private Runnable check_ss = new Runnable() 
	{
        public void run() 
        {
        	if(Socket_service.isConnecting)
    		{
    			ss.setBackgroundResource(R.drawable.server_on);
    		}
    		else
    		{
    			ss.setBackgroundResource(R.drawable.server_off);
    		}
        	
        	handler.postDelayed(this, 1000);
        }
	};
	
	private Runnable check_ec = new Runnable() 
	{
        public void run() 
        {
        	if(Socket_service.event_count>0)
        	{
        		ec.setText(String.valueOf(Socket_service.event_count));
        	}
        	handler.postDelayed(this, 1000);
        }
	};

	public void onBackPressed() 
	{
		Home.this.moveTaskToBack(true);
	}
	
	
}
