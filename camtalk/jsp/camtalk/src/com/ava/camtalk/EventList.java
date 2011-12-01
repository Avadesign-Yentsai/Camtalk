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

public class EventList 
{
	public static String getList(String Email, String Date)
	{
		String url=DBInfo.getDBurl();
		String DBuser=DBInfo.getDBuser();
		String DBpassword=DBInfo.getDBpassword();
		String sql;
		
		if(Date.equals("group"))
		{
			sql="SELECT `userMail`,`eventDate`"+
			"FROM `event_log` "+
			"where `userMail`='"+Email+"' "+
			"AND `eventAvailable`='true' "+
			"group by `eventDate`"+
			"order by `eventDate` desc";
		}
		else
		{
			sql="SELECT `caminfo_tb`.`camName`,`userMail`,`eventDate`,`eventTime`"+
			"FROM `event_log`,`caminfo_tb`"+
			"where `userMail`='"+Email+"' "+
			"AND `eventDate`='"+Date+"'"+
			"AND `eventAvailable`='true'"+
			"AND `caminfo_tb`.`camID`=`event_log`.`camID`"+
			"order by `eventTime` desc";
		}
		
		JSONArray eventlist = new JSONArray();
		StringWriter out = new StringWriter();

		try
		{
			Connection conn = DriverManager.getConnection(url+"&user="+DBuser+"&password="+DBpassword);
			Statement stmt=conn.createStatement();
			
			ResultSet rs=stmt.executeQuery(sql);
			
			List l = new LinkedList();
			
			if(Date.equals("group"))
			{
				while(rs.next())
				{
					Map m = new HashMap();
					m.put("eventDate",rs.getString("eventDate"));
					m.put("eventTime","");
					m.put("camName","");
					l.add(m);
				}
			}
			else
			{
				while(rs.next())
				{
					Map m = new HashMap();
					m.put("camName",rs.getString("camName"));
					m.put("eventDate",rs.getString("eventDate"));
					m.put("eventTime",rs.getString("eventTime"));
					l.add(m);
				}
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
