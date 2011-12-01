<%@ page language="java" contentType="text/html; charset=BIG5"
    pageEncoding="BIG5"%>
<%@ page import="java.sql.*"%>
<%@ page import="com.ava.camtalk.*" %>

<%

	String url=DBInfo.getDBurl();
	String DBuser=DBInfo.getDBuser();
	String DBpassword=DBInfo.getDBpassword();
	
	String Email=new String(request.getParameter("Email").getBytes("ISO-8859-1"),"UTF-8").toLowerCase();
	String Pwd=new String(request.getParameter("Pwd").getBytes("ISO-8859-1"),"UTF-8");
	
	//­n­×§ï
	try
	{
		Connection conn = DriverManager.getConnection(url+"&user="+DBuser+"&password="+DBpassword);
		Statement stmt=conn.createStatement();
		
		String sql="select* from `user_tb` where `userMail`='"+Email+"'";
		
		ResultSet rs=stmt.executeQuery(sql);
		if(rs.next())
		{
			out.println("invaidEmail");
		}
		else
		{
			sql="INSERT INTO `user_tb` (`userMail` ,`userPwd`) VALUES ('"+Email+"', '"+Pwd+"');";
			stmt.executeUpdate(sql);
			out.println("true");
		}
		
		rs.close();
		stmt.close();
	    conn.close();
	}
    catch(Exception e)
	{
		System.out.println(e.toString());
		out.println("DB_Error");
	}
%>