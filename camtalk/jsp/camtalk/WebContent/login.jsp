<%@ page language="java" contentType="text/html; charset=BIG5"
    pageEncoding="BIG5"%>
<%@ page import="java.sql.*"%>
<%@ page import="com.ava.camtalk.*" %>
<%@ page session="true" %>

<%
	String Email = new String(request.getParameter("Email").getBytes("ISO-8859-1"),"UTF-8").toLowerCase();
	String Pwd = new String(request.getParameter("Pwd").getBytes("ISO-8859-1"),"UTF-8");
	String IP= request.getRemoteAddr();
	String Equip = new String(request.getParameter("Equip").getBytes("ISO-8859-1"),"UTF-8");

	
	String re = UserVerify.getResult(Email, Pwd);
	
	if (re.equals("true"))
	{
		if(Equip.equals("pc")) //PC�� //1114���ỡ�i�ण�|���ϥ�browser�����|
		{
			session.setAttribute("usermail",Email);
			session.setAttribute("userpwd",Pwd);
			
			response.sendRedirect("setting.jsp");
		}
		
		re = CreatLog.getResult(Email, IP, Equip);//�n�J�O��
	
	}
	out.print(re);
%>