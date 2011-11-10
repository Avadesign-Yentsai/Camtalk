package com.ava.camtalk;

public class DBInfo 
{
	public static String getDBurl()
    {
		return "jdbc:mysql://localhost:3306/yen_camtalk?autoReconnect=true&useUnicode=true&characterEncoding=big5";
    }
	public static String getDBuser()
    {
		return "admin";
    }
	public static String getDBpassword()
    {
		return "avadesign";
    }
}

