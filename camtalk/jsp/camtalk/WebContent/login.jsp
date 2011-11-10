<%@ page language="java" contentType="text/html; charset=BIG5"
    pageEncoding="BIG5"%>
<%@ page import="java.sql.*"%>
<%@ page import="com.ava.camtalk.*" %>

<%
	String url=DBInfo.getDBurl();
	String DBuser=DBInfo.getDBuser();
	String DBpassword=DBInfo.getDBpassword();
	
	String Email = new String(request.getParameter("Email").getBytes("ISO-8859-1"),"UTF-8");
	String Pwd = new String(request.getParameter("Pwd").getBytes("ISO-8859-1"),"UTF-8");
	String IP= request.getRemoteAddr();
	String Equip = new String(request.getParameter("Equip").getBytes("ISO-8859-1"),"UTF-8");

	
	String re = UserVerify.getResult(Email, Pwd);
	
	if (re.equals("true"))
	{
		UpdateUserIP.getResult(Email, IP);
		re = CreatLog.getResult(Email, IP, Equip);
	}
	out.print(re);
%>