package com.avadesign.camvideo;



import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class Setting extends Activity 
{
	
	private String userAcc;
	private String userPwd;
	
	private String mds;
	private ProgressDialog myDialog;
	
	private String re;
	private String TAG = "camtalk/Setting";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        
        //讀取手機內XML帳號密碼
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
        	userPwd=obj.get("pwd").toString();
        	
		}
		catch (Exception e) //無法讀取
		{
			finish();
        	Intent intent=new Intent();
        	intent.setClass(Setting.this, Login.class);
		    startActivity(intent);
		}
       
        myDialog = new ProgressDialog(Setting.this);
        myDialog.setIndeterminate(true);
        myDialog.setCancelable(false);
        myDialog.setMessage("Loading...");
        myDialog.show();
        
        loadSetting();
             
     }
    
    private Handler handler = new Handler()
    {
    	@Override
    	public void handleMessage(Message msg)
    	{
    		switch (msg.what) 
            { 
    			case 0:
    			{
    				myDialog.dismiss();
    	    		
    	    		ImageButton ch_recive_btn = (ImageButton)findViewById(R.id.ch_recive_btn);
    	            TextView ch_recive_text = (TextView)findViewById(R.id.ch_recive_text);
    	            /*
    	            ImageButton ch_dia_btn = (ImageButton)findViewById(R.id.ch_dia_btn);
    	            TextView ch_dia_text = (TextView)findViewById(R.id.ch_dia_text);
    	            
    	            ch_dia_btn.setBackgroundResource(R.drawable.set_off_btn);
	            	ch_dia_text.setText("警告視窗");
    	             */
    	          
    	            if (mds.equals("on"))
    	            {
    	            	ch_recive_btn.setBackgroundResource(R.drawable.set_off_btn);
    	            	ch_recive_text.setText("關閉家庭保全");
    	            }
    	            else
    	            {
    	            	ch_recive_btn.setBackgroundResource(R.drawable.set_on_btn);
    	            	ch_recive_text.setText("開啟家庭保全");
    	            	/*
    	            	ch_dia_text.setVisibility(View.GONE);
    	            	ch_dia_btn.setVisibility(View.GONE);
    	            	*/
    	            }
    	          
    	            ch_recive_btn.setOnTouchListener(new Button.OnTouchListener()
    	            {

    	    			@Override
    	    			public boolean onTouch(View v, MotionEvent event) 
    	    			{
    	    				switch( event.getAction() ) 
    	    				{
    	    					case MotionEvent.ACTION_DOWN:
    	    					Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);  
    	    	        		vibrator.vibrate(100);
    	            		break;
    	    				}
    	    				return false;
    	    			}
    	            	
    	            });
    	            ch_recive_btn.setOnClickListener(new Button.OnClickListener()
    	            {
    	            	public void onClick(View V)
    	            	{
    	            		myDialog = new ProgressDialog(Setting.this);
    	        	        myDialog.setIndeterminate(false);
    	        	        myDialog.setCancelable(false);
    	        	        myDialog.setMessage("儲存設定中...");
    	        	        myDialog.show();
    	        	        
    	        	        saveSetting();
    	        	    }
    	            });
    	            
    				break;
    			}
    			case 1:
    			{
    				myDialog.dismiss();
    				
    				if(re.equals("true"))
	    	        {
	    	        	myDialog.dismiss();
	    	        	
	    	        	//Toast.makeText(Setting.this, "儲存設定成功", Toast.LENGTH_SHORT).show();//容易當機
	    	        	
	    	        	finish();
	    	        	
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
	    	        	
	    	        	Intent intent = new Intent();
	            		intent.setClass(Setting.this, Setting.class);
	            		startActivity(intent);
	    	        	
	    	        }
	    	        else
	    	        {
	    	        	myDialog.dismiss();
	    	        	Toast.makeText(Setting.this, "儲存設定失敗", Toast.LENGTH_LONG).show();
	    	        }
    				 
    				break;
    			}
            }
    		
    	}
    };
    
    private void loadSetting() 
	{
		Thread t = new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{
				String[] input = new String[5];
		        input[0]="http://centos64.dyndns-free.com:8080/camtalk/getMDS.jsp";
		        input[1]="Email";
		        input[2]=userAcc;
		        input[3]="Pwd";
		        input[4]=userPwd;
		        
		        mds = EregiReplace.eregi_replace("(\r\n|\r|\n|\n\r| |)", "",HttpPostResponse.getHttpResponse(input));
		        
		        handler.sendEmptyMessage(0);
			}
		});
		t.start();
	}
	
	private void saveSetting() 
	{
		Thread t = new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{
				if(mds.equals("on"))
        		{
        			mds = "off";
        		}
        		else
        		{
        			mds = "on";
        		}	
        		String[] input = new String[7];
        		input[0]="http://centos64.dyndns-free.com:8080/camtalk/changeMDS.jsp";
    	        input[1]="Email";
    	        input[2]=userAcc;
    	        input[3]="Pwd";
    	        input[4]=userPwd;
    	        input[5]="mds";
    	        input[6]=mds;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
    	        
    	        re =EregiReplace.eregi_replace("(\r\n|\r|\n|\n\r| |)", "",HttpPostResponse.getHttpResponse(input));
    	        
    	        handler.sendEmptyMessage(1);
    	       
			}
		});
		t.start();
	}

	
	
}