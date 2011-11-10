<%@ page language="java" contentType="text/html; charset=BIG5"
    pageEncoding="BIG5"%>
<%@ page import="java.sql.*"%>
<%@ page import="com.ava.camtalk.*" %>

<%
	DBInfo gdi =new DBInfo();
	
	String url=gdi.getDBurl();
	String DBuser=gdi.getDBuser();
	String DBpassword=gdi.getDBpassword();
	
	String Email=new String(request.getParameter("Email").getBytes("ISO-8859-1"),"UTF-8");
	String Pwd=new String(request.getParameter("Pwd").getBytes("ISO-8859-1"),"UTF-8");
	
	String re = UserVerify.getResult(Email,Pwd);
	
	if(re.equals("true")) //return camlist
	{
		out.println(CamList.getList(Email));
	}
	else if(re.equals("false")) 
	{
		out.println("Verify_Fail");
	}
	else
	{
		out.println("DB_Error");
	}
%>