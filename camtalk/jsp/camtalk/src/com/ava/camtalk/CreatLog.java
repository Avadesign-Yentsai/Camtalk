package com.ava.camtalk;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CreatLog 
{
	public static String getResult(String Email,String IP,String Equip)
	{
		String url=DBInfo.getDBurl();
		String DBuser=DBInfo.getDBuser();
		String DBpassword=DBInfo.getDBpassword();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(Calendar.getInstance().getTime());
		
		String sql="INSERT INTO `user_log` "+
					"(`logNo` ,`userMail` ,`logTime` ,`logType` ,`logEquip` ,`logIP`)"+
					"VALUES "+
					"(NULL, '"+Email+"', '"+date+"', 'login', '"+Equip+"', '"+IP+"')";
		
		try
		{
			Connection conn = DriverManager.getConnection(url+"&user="+DBuser+"&password="+DBpassword);
			Statement stmt=conn.createStatement();
			
			stmt.executeUpdate(sql);
			
			return "true";
			
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			return "DB_error";
		}
	}
}
