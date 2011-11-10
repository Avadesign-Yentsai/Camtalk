package com.ava.camtalk;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;

import org.json.simple.JSONStreamAware;
import org.json.simple.JSONValue;

public class CamInfo implements JSONStreamAware
{
	 private String id;
     private String name;
     private String talkac;
     private String talkpw;
     private String talkport;
     private String videoport;
     private String videocode;
     private String ip;
     
     
     public CamInfo(String id, String name, String talkac, String talkpw,String talkport,String videoport,String videocode,String ip)
     {
             this.id = id;
             this.name = name;
             this.talkac = talkac;
             this.talkpw = talkpw;
             this.talkport = talkport;
             this.videoport = videoport;
             this.videocode=videocode;
             this.ip = ip;
     }
     
    public void writeJSONString (Writer out) throws IOException
    {
             LinkedHashMap obj = new LinkedHashMap();
             obj.put("name", name);
             obj.put("talkac", talkac);
             obj.put("talkpw", talkpw);
             obj.put("talkport", talkport);
             obj.put("videoport", videoport);
             obj.put("videocode", videocode);
             obj.put("ip", ip);
             JSONValue.writeJSONString(obj, out);
    }
}
