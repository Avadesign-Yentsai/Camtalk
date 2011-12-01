package com.ava.camtalk;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class DelEvent 
{
	public static String getResult(String Email,String Type,String Data)
	{
		String url=DBInfo.getDBurl();
		String DBuser=DBInfo.getDBuser();
		String DBpassword=DBInfo.getDBpassword();
		
		JSONObject obj=(JSONObject) JSONValue.parse(Data.toString());
		
		String sql;
		
		if(Type.equals("group"))
		{
			sql="UPDATE `event_log`"+
				"SET `eventAvailable` = 'false' "+
				"WHERE `userMail` = '"+Email+"' "+
				"AND `eventDate` = '"+obj.get("date").toString()+"' ";
		}
		else
		{
			sql="UPDATE `event_log`,`caminfo_tb` "+
				"SET `eventAvailable` = 'false' "+
				"WHERE `userMail` = '"+Email+"' "+
				"AND `caminfo_tb`.`camName` = '"+obj.get("name").toString()+"' "+
				"AND `eventDate` = '"+obj.get("date").toString()+"' "+
				"AND `eventTime` = '"+obj.get("time").toString()+"' "+
				"AND `event_log`.`camID` = `caminfo_tb`.`camID`";
		}
		
		
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
