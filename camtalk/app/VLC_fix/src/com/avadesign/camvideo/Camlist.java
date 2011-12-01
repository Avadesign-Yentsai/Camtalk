package com.avadesign.camvideo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.videolan.vlc.android.VideoPlayerActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Camlist extends Activity 
{
	
	private ListView listview;
	private ImageButton reload;
	
	private String TAG="camtalk/Camlist";
	
	private String[][] attr; //[i][j], 第 i 台 IPCAM 的第 j 個屬性
	
	private String userAcc;
	private String userPwd;
	
	private boolean noCam;
	
	private ProgressDialog myDialog;
	
	@Override   
	protected void onCreate(Bundle savedInstanceState) 
	{	
		setContentView(R.layout.camlist);	
		super.onCreate(savedInstanceState); 
		
		listview=(ListView)findViewById(R.id.camlist_listView);
		reload=(ImageButton)findViewById(R.id.camlist_reload);
		
		
		reload.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View arg0) 
			{
				reload.setEnabled(false);
				
				myDialog = new ProgressDialog(Camlist.this);
    	        myDialog.setIndeterminate(true);
    	        myDialog.setCancelable(false);
    	        myDialog.setMessage("重新載入清單...");
    	        myDialog.show();
    	        
				reloadcamlist();
			}
			
		});
        
		//讀取手機內帳號密碼
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
        catch (Exception e) 
		{
        	Log.d(TAG, e.toString());
        	
        	//資料錯誤,轉到登入頁面
        	finish();
        	Intent intent=new Intent();
        	intent.setClass(Camlist.this, Login.class);
		    startActivity(intent);
		}
        
        //從XML讀取取IP Cam的資料
        InputStream XMLinputStream1=null;
        String[] attrName = {"CamID","CamName","CamTalkAc","CamTalkPw","CamIP","CamTalkPort","CamVideoPort","CamVideoCode"};
        try 
		{
        	XMLinputStream1 = null;
			XMLinputStream1 = this.openFileInput("CamInfo.xml");
			attr=ReadXML.readCamInfoXML(XMLinputStream1,"CamInfo",attrName);
			
			XMLinputStream1 = null;
		} 
        catch (FileNotFoundException e) 
		{
			Log.d(TAG, e.toString());
			noCam = true;
		}
       
        if(!noCam)
        {
        	//ListView
            listview.setAdapter(new CamAdapter());
            listview.setTextFilterEnabled(true);
            listview.setOnItemClickListener(new OnItemClickListener() 
            {
                public void onItemClick(AdapterView<?> parent, View v, int position, long ID)
                 {
                	final String id = attr[position][0];
                	final String name = attr[position][1];
        			final String talkac = attr[position][2];
        			final String talkpw = attr[position][3];
        			final String talkport = attr[position][4];
        			final String videoport = attr[position][5];
        			final String videocode = attr[position][6];
        			final String ip = attr[position][7];
        			
                	Intent intent=new Intent();
                    intent.setClass(Camlist.this, VideoPlayerActivity.class);
                   
                    Bundle bundle =new Bundle();
                    bundle.putString("KEY_MICIP", ip);
                	bundle.putInt("KEY_MICPORT", Integer.valueOf(talkport));
                	bundle.putString("KEY_MICAC", talkac);
                	bundle.putString("KEY_MICPW", talkpw);
                	bundle.putString("KEY_UserAcc", userAcc);
                	bundle.putString("KEY_UserPwd", userPwd);
                	
                    if(videocode.equals("h264"))
                    {
                    	bundle.putString("KEY_CAMURL", "rtsp://"+ip+":"+videoport+"/ipcam_h264.sdp");
                    }
                    else if(videocode.equals("mpeg4"))
                    {
                    	bundle.putString("KEY_CAMURL", "rtsp://"+ip+":"+videoport+"/ipcam.sdp");
                    }
                    intent.putExtras(bundle);
                   	startActivity(intent);
                 }
            });
        }
    }
	
	public class CamAdapter extends BaseAdapter
	{
		
		@Override
		public int getCount() {
			return attr.length;
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
			final String name = attr[position][1];
			final String ip = attr[position][7];
			
			View v;
			if(convertView==null)
        	{
				LayoutInflater li = getLayoutInflater();
				v = li.inflate(R.layout.listview_mod, null);
				
				TextView IP = (TextView)v.findViewById(R.id.listview_mod_ip);
				TextView CamName=(TextView)v.findViewById(R.id.listview_mod_ipcamName);
				
				IP.setText(ip);
				CamName.setText(name);
				
        	}
			else
			{
				v = convertView;
			}
			return v;
		}
	}
	
	
	public void onBackPressed() 
	{
		Intent intent=new Intent();
		//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //HTC error?
		intent.setClass(Camlist.this, Home.class);
		startActivity(intent);
	
		
	}
	
	private void reloadcamlist()
	{
		Thread t = new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{
				attr = null;
				
				attr = LoadCamlist.load(userAcc, userPwd);
				
				if(attr!=null)
				{
					 String[] attrName = {"CamID","CamName","CamTalkAc","CamTalkPw","CamIP","CamTalkPort","CamVideoPort","CamVideoCode"};
					 writeXML("CamInfo.xml",PrintXML.outCamInfoXml(attrName,attr));
					 Log.d(TAG,PrintXML.outCamInfoXml(attrName,attr));
					 
					 handler.sendEmptyMessage(1);
				}
				else
				{
					handler.sendEmptyMessage(0);
				}
				
			}
		
		});
		t.start();
		
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
	
	private Handler handler = new Handler()
    {
    	@Override
    	public void handleMessage(Message msg)
    	{
    		switch (msg.what) 
            { 
    			case 1:
    			{
    				myDialog.dismiss();
    				//頁面重整
    				finish();
    				Intent intent=new Intent();
    				intent.setClass(Camlist.this, Camlist.class);
    				startActivity(intent);
    			}
    			break;
    			case 0:
    			{
    				myDialog.dismiss();
    				reload.setEnabled(true);
    				Toast.makeText(Camlist.this, "載入清單失敗", Toast.LENGTH_LONG).show();
    			}
    			break;
    			
            }
    		
    	}
    };

}
