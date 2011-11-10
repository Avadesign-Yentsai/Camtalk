package com.ava.camtalk;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.json.simple.JSONArray;

public class CamList 
{
	public static String getList(String Email)
	{
		String url=DBInfo.getDBurl();
		String DBuser=DBInfo.getDBuser();
		String DBpassword=DBInfo.getDBpassword();
		
		String sql="select* from `caminfo_tb` where `userMail`='"+Email+"' AND `camDel`='n'";
		
		JSONArray camlist = new JSONArray();
		StringWriter out = new StringWriter();

		
		try
		{
			Connection conn = DriverManager.getConnection(url+"&user="+DBuser+"&password="+DBpassword);
			Statement stmt=conn.createStatement();
			
			ResultSet rs=stmt.executeQuery(sql);
			while(rs.next())
			{
				camlist.add(new CamInfo(rs.getString("camID"),
										rs.getString("camName"),
										rs.getString("camTalkAc"),
										rs.getString("camTalkPw"),
										rs.getString("camTalkPort"),
										rs.getString("camVideoPort"),
										rs.getString("camVideoCode"),
										rs.getString("camIP")));
			}
			
			camlist.writeJSONString(out);
			return out.toString();
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			return "DB_Error";
		}
		
	}
}
