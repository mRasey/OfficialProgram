<%--
  Created by IntelliJ IDEA.
  User: Billy
  Date: 2016/8/6
  Time: 13:37
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=gbk" language="java" pageEncoding="gbk" %>
<%@ page import="java.lang.*" %>
<%@ page import="java.io.*" %>
<%@ page import="execPy.ExecPy" %>
<html>
<head>
    <title>���ĸ�ʽ�޸�</title>
</head>
<body>
    <div align="center">
        ���ڴ���<br>
    </div>
</body>
<%
    try{
        ExecPy.run((String) request.getAttribute("fileName"));
    }
    catch (Exception e) {
        response.sendRedirect("showErrorInfo.jsp");
    }
    request.setAttribute("fileName", request.getAttribute("fileName"));
%>
<jsp:forward page="result.jsp"/>
</html>
