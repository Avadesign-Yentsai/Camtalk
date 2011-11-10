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
        
        
        //�_��
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);  
        long[] pattern = {0,250,125,250,125,250}; // OFF/ON/OFF/ON...  
        vibrator.vibrate(pattern, -1);//-1�����ơA�D-1���qpattern�����w��m�}�l����  
        
        //�q������
        MediaPlayer media = new MediaPlayer();
        media = MediaPlayer.create(DialogActivity.this, R.raw.notify);
        
        if (media != null)
        {
        	media.stop();
        }
        
        try 
        {
			media.prepare();
			//media.start(); //�R��
		} 
        catch (Exception e) 
		{
			e.printStackTrace();
		} 
        
        //Ū�������XML�b���K�X
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
        	//XML��ƿ��~,���n�J����
        	finish();
        	Intent intent=new Intent();
        	intent.setClass(DialogActivity.this, Login.class);
		    startActivity(intent);
		}
        
        //���ǨӦ�Server��SocketServer.java���]�w
        //info[0]~[5]: CamURL,CamTalkIP,CamTalkPort,CamTalkAc,CamTalkPw,CamName
        final String[] info = CamInfo.split(":SPLIT:");
        
        t.setText("�Ӧ�"+info[5]);
        
        check.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) 
			{
				
				Intent intent = new Intent();
				intent.setAction(Socket_service.MY_ACTION);
				sendBroadcast(intent);
				
				finish();     //����Activity�C�եΨ�OnDestroy()��k	
				
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
				
				finish();     //����Activity�C�եΨ�OnDestroy()��k		
				
			}
		});
	}
}
