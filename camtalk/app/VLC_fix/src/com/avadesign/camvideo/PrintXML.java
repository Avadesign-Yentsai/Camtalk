package com.avadesign.camvideo;

import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

public class PrintXML 
{
	private static String TAG = "PrintXML";
	
	public static String outXml(String Acc,String Pwd)
    {
    	XmlSerializer serializer = Xml.newSerializer();
    	StringWriter writer = new StringWriter();
    	try
    	{
	    	serializer.setOutput(writer);
	
	    	// <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	    	serializer.startDocument("UTF-8",true);
	    	
	    	//<CamTalk>
	    	serializer.startTag("","CamTalk");
	    	
	    	
	    	//<UserInfo>
	    	serializer.startTag("","UserInfo");
	    	//serializer.attribute("","date","2009-09-23");
	    	
	    	//<UserAcc>Acc</UserAcc>
	    	serializer.startTag("","UserAcc");
	    	serializer.text(Acc);
	    	serializer.endTag("","UserAcc");
	    	
	    	//<UserPwd>Pwd</UserPwd>
	    	serializer.startTag("","UserPwd");
	    	serializer.text(Pwd);
	    	serializer.endTag("","UserPwd");
	    	
	    	//<UserInfo>
	    	serializer.endTag("","UserInfo");
	    	
	    	
	    	//<CamTalk>
	    	serializer.endTag("","CamTalk");
	    	
	    	serializer.endDocument();
	    	return writer.toString();
    	}
    	catch(Exception e)
    	{
    		Log.d(TAG,e.toString());
    		return null;
    	}
    }
	

}
