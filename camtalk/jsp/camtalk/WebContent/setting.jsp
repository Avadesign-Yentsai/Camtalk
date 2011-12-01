<%@ page language="java" contentType="text/html; charset=BIG5"
    pageEncoding="BIG5"%>
<%@ page import="java.sql.*"%>
<%@ page import="com.ava.camtalk.*" %>
<%@ page session="true" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=BIG5">
<title>Setting</title>
</head>
<body>
<table width="100%" border="0">
  <tr>
    <td><div align="center" class="pic2"><img src="image/995-1.png" width="995" height="132" /></div></td>
  </tr>
  <tr>
    <td><div align="center" class="pic"><img src="image/blue.png" width="995" height="131" /></div></td>
  </tr>
   <tr>
    <td>
		<div align="center">
		<% 
		//out.print(session.getAttribute("usermail")); 
		//out.print(session.getAttribute("Pwd")); 
		%>
		<form action="doSetting.jsp" method=post>動態偵測設定
			<input type="radio" name="mds" value ="on">on<br>
			<input type="radio" name="mds" value ="off">off<br>
			<input type="submit" value ="Submit"><br>
		</form>
	
		</div>
	</td>
  </tr>
</table>
</body>
</html>