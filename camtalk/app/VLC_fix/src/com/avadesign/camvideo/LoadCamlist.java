package com.avadesign.camvideo;


public class LoadCamlist 
{
	public static String load(String Email, String Pwd) 
	{
		String TAG="LoadCamlist";
		
		String[] input = new String[5];
		
		input[0] = ServerInfo.getWebPath()+"loadcamlist.jsp";
		input[1] = "Email";
		input[2] = Email;
		input[3] = "Pwd";
		input[4] = Pwd;
		
		String re = HttpPostResponse.getHttpResponse(input);
		re=EregiReplace.eregi_replace("(\r\n|\r|\n|\n\r| |)", "", re);
		
		return re;
	}
}
