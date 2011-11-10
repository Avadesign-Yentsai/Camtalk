package com.avadesign.camvideo;

import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;


public class Ping 
{
	public static boolean ping (String URL)
	{
		
		try 
		{
			URL url = new URL(URL);
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			urlc.setRequestProperty("User-Agent", "Android Application");
		    urlc.setRequestProperty("Connection", "close");
		    urlc.setConnectTimeout(1000 * 10); 
		    urlc.connect();
		    if (urlc.getResponseCode() == 200) 
		    {
		        return true;
		    }
		} 
		catch (Exception e)
		{
			Log.d("PING", e.toString());
		}
		return false;

	}
}
