package com.avadesign.camvideo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.widget.Button;
import android.widget.EditText;

public class NewCam extends Activity 
{
	
	private EditText CamName,CamIP;
	private Button add,test;
	
	private String TAG = "NewCam";
	
	private String[] camname_ar;
	private String[] camip_ar;
	
	Intent intent;
	
	@Override   
	protected void onCreate(Bundle savedInstanceState) 
	{	
		setContentView(R.layout.new_cam);	
		super.onCreate(savedInstanceState); 
		
		CamName=(EditText)findViewById(R.id.new_cam_name);//+array
		CamIP=(EditText)findViewById(R.id.new_cam_ip);//+array
		add=(Button)findViewById(R.id.new_cam_add);
		test=(Button)findViewById(R.id.new_cam_test);
		
		intent=this.getIntent();
		
		/*
		//讀取舊設定
		String[][] caminfo = null;
        InputStream XMLinputStream1=null;
        try 
		{
			XMLinputStream1 = this.openFileInput("CamInfo.xml");
			caminfo=ReadXML.readXML(XMLinputStream1,"CamInfo");
			
			camname_ar=new String[caminfo.length+1];
			camip_ar=new String[caminfo.length+1];
			
			for(int i=0;i<caminfo.length;i++)
			{
				camname_ar[i+1]=caminfo[i][0]; //camname_ar[0]留給準備新增的資料
				camip_ar[i+1]=caminfo[i][1]; //camip_ar[0]留給準備新增的資料
			}
			Log.d(TAG, "camname_ar.length="+String.valueOf(camname_ar.length));
		}
        catch (FileNotFoundException e) 
		{
        	camname_ar=new String[1];
			camip_ar=new String[1];
			Log.d(TAG, e.toString());
			Log.d(TAG, "camname_ar.length="+String.valueOf(camname_ar.length));
		}
		
        
		add.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View V)
        	{
        		camname_ar[0]=CamName.getText().toString();
        		camip_ar[0]=CamIP.getText().toString();
                
        		Log.v(TAG,camname_ar[0]);
        		
        		//把IPcam的設定資料存成CamInfo.xml
        		boolean b=writeXML("CamInfo.xml",outXml(camname_ar,camip_ar));
        		
        		//回到Home
        		NewCam.this.setResult(RESULT_OK,intent);
        		NewCam.this.finish();
        		
        	}
        });
		
		test.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View V)
        	{
        		boolean re = Ping.ping(CamIP.getText().toString());
        		if(re)
        		{
        			Toast.makeText(NewCam.this, "OK", Toast.LENGTH_SHORT).show();
        		}
        		else
        		{
        			Toast.makeText(NewCam.this, "NO", Toast.LENGTH_SHORT).show();
        		}
        		
        	}
        });
		*/
	}
	
	private String outXml(String[] name,String[] IP)
    {
    	XmlSerializer serializer = Xml.newSerializer();
    	StringWriter writer = new StringWriter();
    	try
    	{
	    	serializer.setOutput(writer);
	
	    	// <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	    	serializer.startDocument("UTF-8",true);
	    	
	    	//<CamView>
	    	serializer.startTag("","CamView");
	    	
	    	for(int i=0;i<name.length;i++)
	    	{
		    	//<CamInfo>
		    	serializer.startTag("","CamInfo");
		    	//serializer.attribute("","date","2009-09-23");
		    	
		    	//<CamName>name</CamName>
		    	serializer.startTag("","CamName");
		    	serializer.text(name[i]);
		    	serializer.endTag("","CamName");
		    	
		    	//<CamIP>IP</CamIP>
		    	serializer.startTag("","CamIP");
		    	serializer.text(IP[i]);
		    	serializer.endTag("","CamIP");
		    	
		    	//<CamInfo>
		    	serializer.endTag("","CamInfo");
	    	}
	    	
	    	//<CamView>
	    	serializer.endTag("","CamView");
	    	
	    	serializer.endDocument();
	    	return writer.toString();
    	}
    	catch(Exception e)
    	{
    		Log.d(TAG,e.toString());
    		return null;
    	}
    }
	
	public boolean writeXML(String path,String txt)
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
