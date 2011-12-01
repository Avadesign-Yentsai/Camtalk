<%@ page language="java" contentType="text/html; charset=BIG5"
    pageEncoding="BIG5"%>
<%@ page import="java.sql.*"%>
<%@ page import="com.ava.camtalk.*" %>
<%@ page session="true" %>

<%
	
	String Email = session.getAttribute("usermail").toString();
	String Pwd = session.getAttribute("userpwd").toString();
	String mds = new String(request.getParameter("mds").getBytes("ISO-8859-1"),"UTF-8");
	
	String re = UserVerify.getResult(Email, Pwd);
	
	if (re.equals("true"))
	{
		
		UpdateSetting.getResult(Email, Pwd, mds);//更改使用者設定
		
	}
	out.print(re);
	
%>