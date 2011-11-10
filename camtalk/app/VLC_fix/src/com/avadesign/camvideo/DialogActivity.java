package com.avadesign.camvideo;

import java.io.InputStream;

import org.videolan.vlc.android.VideoPlayerActivity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DialogActivity extends Activity 
{

	private String TAG = "DialogActivity";
	private TextView t;
	private Button check, cancel;
	private String CamInfo;
	private String[] userinfo = null; 
	private String userAcc;
	private String userPwd;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogactivity);
        
        Bundle bundle=this.getIntent().getExtras();
        CamInfo=bundle.getString("KEY_INFO");
        
        Log.d(TAG,CamInfo);
        
        t = (TextView)findViewById(R.id.dialogactivity_text);
        check = (Button)findViewById(R.id.dialogactivity_check);
        cancel = (Button)findViewById(R.id.dialogactivity_cancel);
        
        
        //震動
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);  
        long[] pattern = {0,250,125,250,125,250}; // OFF/ON/OFF/ON...  
        vibrator.vibrate(pattern, -1);//-1不重複，非-1為從pattern的指定位置開始重複  
        
        //通知音效
        MediaPlayer media = new MediaPlayer();
        media = MediaPlayer.create(DialogActivity.this, R.raw.notify);
        
        if (media != null)
        {
        	media.stop();
        }
        
        try 
        {
			media.prepare();
			//media.start(); //靜音
		} 
        catch (Exception e) 
		{
			e.printStackTrace();
		} 
        
        //讀取手機內XML帳號密碼
        InputStream XMLinputStream1=null;
		try 
		{
			XMLinputStream1 = this.openFileInput("CamTalk.xml");
			userinfo=ReadXML.readXML(XMLinputStream1,"UserInfo");
			
			userAcc=userinfo[0];
			userPwd=userinfo[1];
			
		}
        catch (Exception e) 
		{
        	//XML資料錯誤,轉到登入頁面
        	finish();
        	Intent intent=new Intent();
        	intent.setClass(DialogActivity.this, Login.class);
		    startActivity(intent);
		}
        
        //順序來自Server的SocketServer.java內設定
        //info[0]~[5]: CamURL,CamTalkIP,CamTalkPort,CamTalkAc,CamTalkPw,CamName
        final String[] info = CamInfo.split(":SPLIT:");
        
        t.setText("來自"+info[5]);
        
        check.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				
				Intent intent = new Intent();
				intent.setAction(Socket_service.MY_ACTION);
				sendBroadcast(intent);
				
				finish();     //結束Activity。調用其OnDestroy()方法	
				
				Intent intent2 = new Intent();
				
				intent2.setClass(DialogActivity.this, VideoPlayerActivity.class);
	               
                Bundle bundle =new Bundle();
                bundle.putString("KEY_MICIP", info[1]);
                bundle.putInt("KEY_MICPORT", Integer.valueOf(info[2]));
             	bundle.putString("KEY_MICAC", info[3]);
             	bundle.putString("KEY_MICPW", info[4]);
             	bundle.putString("KEY_UserAcc", userAcc);
             	bundle.putString("KEY_UserPwd", userPwd);
             	bundle.putString("KEY_CAMIP", info[0]);
             	intent2.putExtras(bundle);
             	startActivity(intent2);
		       
				
			}
		});
        
        cancel.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				
				Intent intent = new Intent();
				intent.setAction(Socket_service.MY_ACTION);
				sendBroadcast(intent);
				
				finish();     //結束Activity。調用其OnDestroy()方法		
				
			}
		});
	}
}
