package com.ava.camtalk;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

public class CamList 
{
	public static String getList(String Email)
	{
		String url=DBInfo.getDBurl();
		String DBuser=DBInfo.getDBuser();
		String DBpassword=DBInfo.getDBpassword();
		
		//String sql="select* from `caminfo_tb` where `userMail`='"+Email+"' AND `camDel`='n'";
		String sql="SELECT * "+
					"FROM `caminfo_tb` , `camlist_tb` , `user_tb` "+
					"WHERE `caminfo_tb`.`camID` = `camlist_tb`.`camID` "+
					"AND `camlist_tb`.`userMail` = `user_tb`.`userMail` "+
					"AND `camlist_tb`.`userMail` ='"+Email+"' "+
					"AND `camlist_tb`.`camAvailable` = 'true'";
		
		JSONArray camlist = new JSONArray();
		StringWriter out = new StringWriter();

		
		try
		{
			Connection conn = DriverManager.getConnection(url+"&user="+DBuser+"&password="+DBpassword);
			Statement stmt=conn.createStatement();
			
			List l = new LinkedList();
			
			ResultSet rs=stmt.executeQuery(sql);
			while(rs.next())
			{
				Map m = new HashMap();
				m.put("id",rs.getString("camID"));
				m.put("name",rs.getString("camName"));
				m.put("talkac",rs.getString("camTalkAc"));
				m.put("talkpw",rs.getString("camTalkPw"));
				m.put("talkport",rs.getString("camTalkPort"));
				m.put("videoport",rs.getString("camVideoPort"));
				m.put("videocode",rs.getString("camVideoCode"));
				m.put("ip",rs.getString("camIP"));
				l.add(m);
			}
			
			String jsonString = JSONValue.toJSONString(l);
			return jsonString.toString();
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			return "DB_Error";
		}
		
	}
}
