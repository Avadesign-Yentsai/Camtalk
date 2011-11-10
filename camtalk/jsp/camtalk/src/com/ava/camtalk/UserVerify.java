package com.ava.camtalk;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class UserVerify 
{
	public static String getResult(String Email,String Pwd)
	{
		String url=DBInfo.getDBurl();
		String DBuser=DBInfo.getDBuser();
		String DBpassword=DBInfo.getDBpassword();
		
		String sql="select* from `user_tb` where `userMail`='"+Email+"' AND `userPwd`='"+Pwd+"'";
		
		try
		{
			Connection conn = DriverManager.getConnection(url+"&user="+DBuser+"&password="+DBpassword);
			Statement stmt=conn.createStatement();
			
			ResultSet rs=stmt.executeQuery(sql);
			if(rs.next())
			{
				rs.close();
				stmt.close();
			    conn.close();
			    
				return("true");
			}
			else
			{
				rs.close();
				stmt.close();
			    conn.close();
			    
				return("false");
			}
			
			
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			return "DB_error";
		}
		
	}
}
