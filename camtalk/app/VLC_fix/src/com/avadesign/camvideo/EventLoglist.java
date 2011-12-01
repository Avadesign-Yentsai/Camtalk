package com.avadesign.camvideo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EventLoglist extends Activity implements Runnable
{
	private String Date;
	private String userAcc;
	private String userPwd;
	private String TAG="camtalk/EventLoglist";
	
	private Vector eventdate=new Vector();
	private Vector eventtime=new Vector();
	private Vector camname=new Vector();
	
	private ListView listview;
	private TextView nodata_txt;
	
	private ProgressDialog myDialog;
	@Override   
	protected void onCreate(Bundle savedInstanceState) 
	{	
		setContentView(R.layout.eventloglist);	
		super.onCreate(savedInstanceState); 
		
		listview = (ListView)findViewById(R.id.eventlog_listView);
		nodata_txt = (TextView)findViewById(R.id.eventlog_nodata_txt);
		
		Bundle bundle = this.getIntent().getExtras();
		Date = bundle.getString("KEY_DATE");
		
		load_ac_pwd();//讀取手機內存的帳號密碼
		
		nodata_txt.setVisibility(View.INVISIBLE);
		
		myDialog = new ProgressDialog(EventLoglist.this);
        myDialog.setIndeterminate(true);
        myDialog.setCancelable(false);
        myDialog.setMessage("Loading...");
        myDialog.show();
		
		Thread thread = new Thread(this);
		thread.start();
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
        	userPwd=obj.get("pwd").toString();
        	
		}
        catch (Exception e) 
		{
        	Log.d(TAG, e.toString());
        	
        	//資料錯誤,強制關閉程式
        	Toast.makeText(EventLoglist.this, "無法讀取資料,請重新執行程式", Toast.LENGTH_LONG).show();
        	android.os.Process.killProcess(android.os.Process.myPid());
		}
		
	}

	@Override
	public void run() 
	{
		String[] input = new String[7];
		
		input[0] = ServerInfo.getWebPath()+"loadeventlist.jsp";
		input[1] = "Email";
		input[2] = userAcc;
		input[3] = "Pwd";
		input[4] = userPwd;
		input[5] = "Date";
		input[6] = Date;
		
		String jsonText = HttpPostResponse.getHttpResponse(input);
		jsonText = EregiReplace.eregi_replace("(\r\n|\r|\n|\n\r| |)", "", jsonText);
		
		if(!jsonText.equals("[]") && !jsonText.equals(""))//Eventlog 資料不為空
        {
			if(Date.equals("group"))
			{
				String key[] = {"eventDate"};
				Vector value[]={eventdate}; //順序和Key一樣
				
				for(int i=0; i<key.length; i++)
				{
					JSONParser parser = new JSONParser();
					KeyFinder finder = new KeyFinder();
					
					finder.setMatchKey(key[i]);
					try
					{
						while(!finder.isEnd())
						{
							parser.parse(jsonText, finder, true);
							if(finder.isFound())
							{
								finder.setFound(false);
								value[i].add(finder.getValue());
							}
							
						}           
					}
					catch(ParseException e)
					{
						Log.d(TAG, e.toString());
					}
				}
				handler.sendEmptyMessage(0);
			}
			else
			{
				String key[] = {"eventDate","eventTime","camName"};
				Vector value[]={eventdate,eventtime,camname}; //順序和Key一樣
				
				for(int i=0; i<key.length; i++)
				{
					JSONParser parser = new JSONParser();
					KeyFinder finder = new KeyFinder();
					
					finder.setMatchKey(key[i]);
					try
					{
						while(!finder.isEnd())
						{
							parser.parse(jsonText, finder, true);
							if(finder.isFound())
							{
								finder.setFound(false);
								value[i].add(finder.getValue().toString());
							}
							
						}           
					}
					catch(ParseException e)
					{
						Log.d(TAG, e.toString());
					}
				}
				handler.sendEmptyMessage(0);
			}
		}
		else //沒資料
		{
			handler.sendEmptyMessage(1);
			
		}
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
					 
					 nodata_txt.setVisibility(View.GONE);
					 
					 listview.setAdapter(new CustAdapter());
					 listview.setOnItemClickListener(new OnItemClickListener() 
					 {
						 public void onItemClick(AdapterView<?> parent, View v, int position, long ID)
			             {
							 if(Date.equals("group"))
							 {
								 Bundle bundle =new Bundle();
								 bundle.putString("KEY_DATE", eventdate.get(position).toString());
				        		
								 Intent intent = new Intent();
								 intent.putExtras(bundle);
								 intent.setClass(EventLoglist.this, EventLoglist.class);
								 startActivity(intent);
							 }
							 
			             }
					 });
					 listview.setOnItemLongClickListener(new OnItemLongClickListener() 
					 {
						 public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long ID) 
						 {
							 AlertDialog.Builder builder = new AlertDialog.Builder(EventLoglist.this); 
							 builder.setTitle("是否刪除此事件串?");
							 builder.setPositiveButton("確定", new DialogInterface.OnClickListener() 
							 {  
								 public void onClick(DialogInterface dialog, int whichButton) 
								 {  
									 if(Date.equals("group"))
									 {
										 Map obj=new LinkedHashMap();
										 obj.put("date",eventdate.get(position).toString());
										 
										 deleteEvent("group",JSONValue.toJSONString(obj));
									 }
									 else
									 {
										 Map obj=new LinkedHashMap();
										 
										 obj.put("date",eventdate.get(position).toString());
										 obj.put("time",eventtime.get(position).toString());
										 obj.put("name",camname.get(position).toString());
										
										 deleteEvent("single",JSONValue.toJSONString(obj));
									 }
								 }

								 private void deleteEvent(String type,String data) 
								 {
									 String[] input = new String[9];
										
									 input[0] = ServerInfo.getWebPath()+"delevent.jsp"; //還沒寫
									 input[1] = "Email";
									 input[2] = userAcc;
									 input[3] = "Pwd";
									 input[4] = userPwd;
									 input[5] = "Type";
									 input[6] = type;
									 input[7] = "Data";
									 input[8] = data;
									 
									 String re = HttpPostResponse.getHttpResponse(input);
									 re=EregiReplace.eregi_replace("(\r\n|\r|\n|\n\r| |)", "", re);
									 
									 if(re.equals("true"))
									 {
										 startActivity(getIntent()); 
										 finish();
									 }
									 else
									 {
										 Toast.makeText(EventLoglist.this, "無法刪除", Toast.LENGTH_SHORT).show();
									 }
								 }  

							 });  
							 builder.setNegativeButton("取消", new DialogInterface.OnClickListener() 
							 {  
								 public void onClick(DialogInterface dialog, int whichButton) 
								 {  

								 }  

							 });
							 builder.create().show();  
							 
							 return true;
						}
						 
					 });
					 break;    
				 }
				 case 1:
				 {
					 myDialog.dismiss();
					 nodata_txt.setVisibility(View.VISIBLE);
					 break;
				 }
				 
			 }
		 }
	 };
	 
	 private class CustAdapter extends BaseAdapter
	 {

		@Override
		public int getCount() 
		{
			return eventdate.size();
		}

		@Override
		public Object getItem(int position) 
		{
			
			return null;
		}

		@Override
		public long getItemId(int position) 
		{
			
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			
			View v;
			
			if(Date.equals("group"))
			{
				LayoutInflater li = getLayoutInflater();
				v = li.inflate(R.layout.eventlist_mod, null);
				
				TextView date = (TextView)v.findViewById(R.id.eventlist_mod_date);
				
				date.setText(eventdate.get(position).toString());
			}
			else
			{
				LayoutInflater li = getLayoutInflater();
				v = li.inflate(R.layout.event_mod, null);
				
				TextView name = (TextView)v.findViewById(R.id.event_mod_camName);
				TextView datetime = (TextView)v.findViewById(R.id.event_mod_datetime);
				
				name.setText(camname.get(position).toString()+"偵測到動態影像");
				datetime.setText(eventdate.get(position).toString()+" "+eventtime.get(position).toString());
				Log.d(TAG,eventdate.get(position).toString()+" "+eventtime.get(position).toString());
			}
				
			return v;
		}
    	
	}
	
}
