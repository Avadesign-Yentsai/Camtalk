package com.avadesign.camvideo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.videolan.vlc.android.VideoPlayerActivity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DialogActivity extends Activity 
{

	private String TAG = "camtalk/DialogActivity";
	private TextView t;
	private Button check, cancel;
	private String msg;
	private String CamInfo;
	private String[] userinfo = null; 
	private String userAcc;
	private String userPwd;
	private MediaPlayer media;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogactivity);
        
        t = (TextView)findViewById(R.id.dialogactivity_text);
        check = (Button)findViewById(R.id.dialogactivity_check);
        cancel = (Button)findViewById(R.id.dialogactivity_cancel);
        
        WindowManager.LayoutParams lp = getWindow().getAttributes(); 
        float brightness = 10;// we change change brightness strength here
        lp.screenBrightness = brightness;
        getWindow().setAttributes(lp); 
        
        //�_��
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);  
        long[] pattern = {0,250,125,250,125,250}; // OFF/ON/OFF/ON...  
        vibrator.vibrate(pattern, -1);//-1�����ơA�D-1���qpattern�����w��m�}�l����  
        
        Bundle bundle=this.getIntent().getExtras();
        
        msg = bundle.getString("KEY_INFO");
        if(msg.equals("Server:mult-loging"))
        {
        	t.setText("     �b���w�b��L�˸m�n�J,�a�x�O���q���_�u     ");
        	check.setText("�T�w");
        	check.setOnClickListener(new OnClickListener()
            {
    			@Override
    			public void onClick(View v) 
    			{
    				finish();     //����Activity�C�եΨ�OnDestroy()��k	
    			}
    		});
        	cancel.setVisibility(View.GONE);
            cancel.setOnClickListener(new OnClickListener()
            {
    			@Override
    			public void onClick(View v) 
    			{
    				finish();     //����Activity�C�եΨ�OnDestroy()��k		
    			}
    		});
        }
        else
        {
        	CamInfo = msg;
            
            Log.d(TAG,CamInfo);
            
            //�q������
            media = new MediaPlayer();
            media = MediaPlayer.create(DialogActivity.this, R.raw.notify);
            
            if (media != null)
            {
            	media.stop();
            }
            
            try 
            {
            	media.prepare();
    			media.setLooping(true);//���Ƽ���
    			media.start(); //�R��
    		} 
            catch (Exception e) 
    		{
    			e.printStackTrace();
    		} 
            
            //Ū�������XML�b���K�X
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
    		catch (Exception e) //�L�kŪ��
    		{
    			finish();
            	Intent intent=new Intent();
            	intent.setClass(DialogActivity.this, Login.class);
    		    startActivity(intent);
    		}
           
            
            
            final JSONObject obj=(JSONObject) JSONValue.parse(CamInfo);
            t.setText("     "+obj.get("camName").toString()+"������ʺA�v��     ");
            
            check.setOnClickListener(new OnClickListener()
            {
    			@Override
    			public void onClick(View v) 
    			{
    				Bundle bundle =new Bundle();
                    bundle.putString("KEY_MICIP", obj.get("camIP").toString());
                    bundle.putInt("KEY_MICPORT", Integer.valueOf(obj.get("camTalkPort").toString()));
                 	bundle.putString("KEY_MICAC", obj.get("camTalkAc").toString());
                 	bundle.putString("KEY_MICPW", obj.get("camTalkPw").toString());
                 	bundle.putString("KEY_UserAcc", userAcc);
                 	bundle.putString("KEY_UserPwd", userPwd);
                 	bundle.putString("KEY_CAMURL", obj.get("camURL").toString());
                 	
    				Intent intent = new Intent();
    				intent.setClass(DialogActivity.this, VideoPlayerActivity.class);
    	            intent.putExtras(bundle);
                 	startActivity(intent);
                 	
                 	stopAlert();
                 	
                 	finish();
    		       
    				
    			}
    		});
            cancel.setOnClickListener(new OnClickListener()
            {
    			@Override
    			public void onClick(View v) 
    			{
    				stopAlert();
    				
    				finish();
    			}
    		});
    		
            /*
            //���ǨӦ�Server��SocketServer.java���]�w
            //info[0]~[5]: CamURL,CamTalkIP,CamTalkPort,CamTalkAc,CamTalkPw,CamName
            final String[] info = CamInfo.split(":SPLIT:");
            
            t.setText("     "+info[5]+"������ʺA�v��     ");
            
            check.setOnClickListener(new OnClickListener()
            {
    			@Override
    			public void onClick(View v) 
    			{
    				
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
                 	bundle.putString("KEY_CAMURL", info[0]);
                 	intent2.putExtras(bundle);
                 	startActivity(intent2);
                 	
                    
                 	
                 	////
    				//�[�W�o�q�]���bSetting��������alert��,������DialogActivity�|�d�bpause,�L�kdestroy,����setting���������~�|destroy
    				if (media != null)
    			    {
    					media.stop();
    					media.release();
    					media = null;
    			    }
    				
    				Intent intent = new Intent();
    				intent.setAction(Socket_service.MY_ACTION);
    				sendBroadcast(intent);
    				////
    		       
    				
    			}
    		});
            cancel.setOnClickListener(new OnClickListener()
            {
    			@Override
    			public void onClick(View v) 
    			{
    				////
    				//�[�W�o�q�]���bSetting��������alert��,������DialogActivity�|�d�bpause,�L�kdestroy,����setting���������~�|destroy
    				if (media != null)
    			    {
    					media.stop();
    					media.release();
    					media = null;
    			    }
    				
    				Intent intent = new Intent();
    				intent.setAction(Socket_service.MY_ACTION);
    				sendBroadcast(intent);
    				////
    				
    				finish();
    			}
    		});
    		*/
        }
	}
	@Override
	public void onBackPressed() 
	{
		stopAlert();
		
		finish();
	}
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
	}
	
	private void stopAlert() 
	{
		if (media != null)
	    {
			media.stop();
			media.release();
			media = null;
	    }
		
		//alert�ŵ��]�w,�o�O�|�}��
		Intent intent = new Intent();
		intent.setAction(Socket_service.MY_ACTION);
		sendBroadcast(intent);
		
		
	}
}
