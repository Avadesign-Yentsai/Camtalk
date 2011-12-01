package com.avadesign.camvideo;

import android.util.Log;

public class DoLogin 
{
	public static String doLogin(String Email, String Pwd) 
	{
		String TAG="camtalk/doLogin";
		
		String[] input = new String[7];
		
		input[0] = ServerInfo.getWebPath()+"login.jsp";
		input[1] = "Email";
		input[2] = Email;
		input[3] = "Pwd";
		input[4] = Pwd;
		input[5] = "Equip";
		input[6] = "phone";
		
		
		
		String re = HttpPostResponse.getHttpResponse(input);
		
		re=EregiReplace.eregi_replace("(\r\n|\r|\n|\n\r| |)", "", re);
		if(re.equals("true"))
		{
			Log.d(TAG, Email+" Login Success");
			return "true";
		}
		else if(re.equals("false"))
		{
			Log.d(TAG, Email+" Login Fail, invalid Account or Password");
			return "false";
		}
		else
		{
			Log.d(TAG, "Server Error:"+re);
			return "error";
		}
	}
}
