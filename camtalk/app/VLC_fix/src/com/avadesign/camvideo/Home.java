package com.avadesign.camvideo;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.videolan.vlc.android.VideoPlayerActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avadesign.camtalker.Recorder;
import com.avadesign.codecs.GSM;

public class Home extends Activity 
{
	
	//private Button newCam;
	private ListView listview;
	
	private static Recorder recorder;
	private static boolean isStarting = true;
	
	private String TAG="Home";
	
	private String[][] attr; //[i][j], 第 i 台 IPCAM 的第 j 個屬性
	
	private String[] caminfo = null; //個別一台 IPCAM 的屬性集合
	private String[] userinfo = null; 
	
	private String[] camip;
	private Map<String, String> map; //傳遞給SocketConn.java用,<cam ip ,cam name>
	
	private String userAcc;
	private String userPwd;
	
	@Override   
	protected void onCreate(Bundle savedInstanceState) 
	{	
		setContentView(R.layout.se_ui);	
		super.onCreate(savedInstanceState); 
		
		//newCam=(Button)findViewById(R.id.se_ui_newCam);
        listview=(ListView)findViewById(R.id.se_ui_listView);
        
        init();
        
        //讀取手機內XML帳號密碼
        InputStream XMLinputStream1=null;
		try 
		{
			XMLinputStream1 = this.openFileInput("CamTalk.xml");
			userinfo=ReadXML.readXML(XMLinputStream1,"UserInfo");
			
			userAcc=userinfo[0];
			userPwd=userinfo[1];
			
			Log.d(TAG, userAcc+","+userPwd);
			
			//Yen:需要再次驗證嗎?
			//有XML的資料就登入驗證 
			/*String s = DoLogin.doLogin(userAcc,userPwd);
			
			if(s.equals("true"))
			{
				
			}
			//登入失敗轉到LOGIN
			else
			{
				finish();
	        	Intent intent=new Intent();
	        	intent.setClass(Home.this, Login.class);
			    startActivity(intent);
			}
			*/
			
		}
        catch (Exception e) 
		{
        	Log.d(TAG, e.toString());
        	
        	//XML資料錯誤,轉到登入頁面
        	finish();
        	Intent intent=new Intent();
        	intent.setClass(Home.this, Login.class);
		    startActivity(intent);
		}
        
        //Socket Listener Service
        Intent intent_s = new Intent(Home.this, Socket_service.class);
        intent_s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        startService(intent_s);
        
        String list = LoadCamlist.load(userAcc, userPwd);
        Log.d(TAG,list);
        
        if(!list.equals("[]"))//資料不為空
        {
        	String key[] = {"name","talkac","talkpw","talkport","videoport","videocode","ip"}; //必須和list回傳的資料符合 ,名稱是jsp裡的CamInfo.java設定的,若修改,getItem()也必須改
        	
        	/*分析傳回的資料開始*/
	        list = list.replace("[", "");
	        list = list.replace("]", "");
	        
	        caminfo=list.split("\\},\\{");
	        
	        attr = new String[caminfo.length][key.length];
	        camip = new String[caminfo.length];
	        
	        for(int i=0; i<caminfo.length; i++)
	        {
	        	caminfo[i] = caminfo[i].toString().replace("{", "");
	        	caminfo[i] = caminfo[i].toString().replace("}", "");
	        	StringBuffer sb = new StringBuffer("{");
	        	sb = sb.append(caminfo[i].toString());
	        	sb = sb.append("}");
	        	
	        	Log.d(TAG,sb.toString());
	        	
	        	for(int j=0; j<key.length; j++)
	        	{
		        	JSONParser parser = new JSONParser();
		        	KeyFinder finder = new KeyFinder();
		        	try
		        	{
			        	finder.setMatchKey(key[j]);
		        		while(!finder.isEnd())
		        		{
		        			parser.parse(sb.toString(), finder, true);
		        			if(finder.isFound())
		        			{
		        				finder.setFound(false);
		        				attr[i][j]=finder.getValue().toString();
		        				
		        				if(key[j].equals("ip"))
		        				{
		        					camip[i]=finder.getValue().toString();
		        				}
		        			}
		        		}   
		        		
		        	}
		        	catch(ParseException pe)
		        	{
		        		pe.printStackTrace();
		        	}
	        	}
	        }
	        /*分析傳回的資料結束*/
	        
	        //map是要傳遞給SocketConn.java用
	        map =  new HashMap<String, String>();
	        for(int i=0; i<caminfo.length; i++)
	        {
	        	map.put(attr[i][6],attr[i][0]); // put(cam ip, cam name);
	        	
	        }
        }
       
        //List
        listview.setAdapter(new CamAdapter());
        listview.setTextFilterEnabled(true);
        listview.setOnItemClickListener(new OnItemClickListener() 
        {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
             {
            	final String name = attr[position][0];
    			final String talkac = attr[position][1];
    			final String talkpw = attr[position][2];
    			final String talkport = attr[position][3];
    			final String videoport = attr[position][4];
    			final String videocode = attr[position][5];
    			final String ip = attr[position][6];
    			
            	Intent intent=new Intent();
                intent.setClass(Home.this, VideoPlayerActivity.class);
               
                Bundle bundle =new Bundle();
                bundle.putString("KEY_MICIP", ip);
            	bundle.putInt("KEY_MICPORT", Integer.valueOf(talkport));
            	bundle.putString("KEY_MICAC", talkac);
            	bundle.putString("KEY_MICPW", talkpw);
            	bundle.putString("KEY_UserAcc", userAcc);
            	bundle.putString("KEY_UserPwd", userPwd);
            	
                if(attr[position][5].equals("h264"))
                {
                	bundle.putString("KEY_CAMIP", "rtsp://"+ip+":"+videoport+"/ipcam_h264.sdp");
                }
                else if(attr[position][5].equals("mpeg4"))
                {
                	bundle.putString("KEY_CAMIP", "rtsp://"+ip+":"+videoport+"/ipcam.sdp");
                }
                intent.putExtras(bundle);
               	startActivity(intent);
             }
        });
        
        /*
        newCam.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View V)
        	{
        		
        		//Intent intent=new Intent();
                //intent.setClass(Home.this, NewCam.class);
                //startActivityForResult(intent,0);
                
        		
        		AlertDialog ad = new AlertDialog.Builder(Home.this)
				 .setPositiveButton("Ok", null)
				 .setMessage("這個功能還沒做")
				 .create();
				 ad.show();
				 
        	}
        });
        */
        
	}
	
	public class CamAdapter extends BaseAdapter
	{
		
		@Override
		public int getCount() {
			return caminfo.length;
		}

		@Override
		public Object getItem(int position) {
			
			return position;
		}

		@Override
		public long getItemId(int position) {
			
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) 
		{
			
			//必須和key順序相符
			final String name = attr[position][0];
			
			final String ip = attr[position][6];
			
			View v;
			if(convertView==null)
        	{
				LayoutInflater li = getLayoutInflater();
				v = li.inflate(R.layout.listview_mod, null);
				
				TextView IP = (TextView)v.findViewById(R.id.listview_mod_ip);
				TextView CamName=(TextView)v.findViewById(R.id.listview_mod_ipcamName);
				
				IP.setText(ip);
				CamName.setText(name);
				/*
				Submit.setOnClickListener(new Button.OnClickListener()
		        {
		        	public void onClick(View V)
		        	{
		        		Intent intent=new Intent();
		                intent.setClass(Home.this, VideoPlayerActivity.class);
		               
		                Bundle bundle =new Bundle();
		                bundle.putString("KEY_MICIP", ip);
	                	bundle.putInt("KEY_MICPORT", Integer.valueOf(talkport));
	                	bundle.putString("KEY_MICAC", talkac);
	                	bundle.putString("KEY_MICPW", talkpw);
	                	bundle.putString("KEY_UserAcc", userAcc);
	                	bundle.putString("KEY_UserPwd", userPwd);
	                	
		                if(attr[position][5].equals("h264"))
		                {
		                	bundle.putString("KEY_CAMIP", "rtsp://"+ip+":"+videoport+"/ipcam_h264.sdp");
		                }
		                else if(attr[position][5].equals("mpeg4"))
		                {
		                	bundle.putString("KEY_CAMIP", "rtsp://"+ip+":"+videoport+"/ipcam.sdp");
		                }
		                intent.putExtras(bundle);
		               	startActivity(intent);
		        	}
		        });
		        */
				
				
        	}
			else
			{
				v = convertView;
			}
			return v;
		}
	}
	
	
	private void init() 
	{

    	// When the volume keys will be pressed the audio stream volume will be changed. 
		// setVolumeControlStream(AudioManager.STREAM_MUSIC);
    	    	    	
   	    	    	
    	if(isStarting) 
    	{    		
    		    		     		    	
    		recorder = new Recorder();
    		
    		recorder.start(); 
    		
    		isStarting = false;    		
    	}
    }
    
    @Override
    public void onStart() 
    {
    	super.onStart();
    	
    	// Initialize codec 
    	GSM.open();
    	
    }
    
    public void onStop() 
    {
    	super.onStop();
    	
       	//recorder.pauseAudio();    	
    	
    	// Release codec resources
    	GSM.close();
    } 
    
    @Override
    public void onDestroy() 
    {
    	super.onDestroy();  
    	release();    	
    }
    
    @Override
    public void onPause() 
    {
    	super.onPause();
    	
    }
    
    @Override
    public void onResume() 
    {
    	super.onResume();  
    	
    }
    
    private void release() 
    {    	
    	// If the back key was pressed.
    	if(isFinishing()) 
    	{
  
    		// Force threads to finish.
  		    		
    		recorder.finish();
    		
    		try 
    		{
      			recorder.join();
    		}
    		catch(InterruptedException e) 
    		{
    			Log.d("Camtalker", e.toString());
    		}

    		recorder = null;
    	    		
    		// Resetting isStarting.
    		isStarting = true;     		
    	}
    }
    
    //New完新的CAMERA後返回HOME重整
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
    	switch (resultCode)
    	{
    		case RESULT_OK:
	    		Intent intent = new Intent();
	    		intent.setClass(this, Home.class);
	    		startActivity(intent);
	    		finish();
	    		break;
    		
    		default:
    			break;
    	}
    }
    
  
}
