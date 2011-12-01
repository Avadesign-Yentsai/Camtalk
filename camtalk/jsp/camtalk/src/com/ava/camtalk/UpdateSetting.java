package com.ava.camtalk;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class UpdateSetting 
{
	public static String getResult(String Email,String IP,String mds)
	{
		String url=DBInfo.getDBurl();
		String DBuser=DBInfo.getDBuser();
		String DBpassword=DBInfo.getDBpassword();
		
		String sql="UPDATE `user_tb` SET `user_md_set` = '"+mds+"' WHERE `userMail` = '"+Email+"' LIMIT 1 ;";
		
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
