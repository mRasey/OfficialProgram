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
<%@ page import="opXML.AnalyXML" %>
<%@ page import="java.net.URLEncoder" %>
<html>
<head>
    <title>论文格式修改</title>
</head>
<body>
    <div align="center">
        正在处理<br>
    </div>
</body>
<%
    try{
//        new Thread(new ExecPy(request.getParameter("fileName"))).start();
        String url = "result.jsp?fileName=" + request.getParameter("fileName");
        response.sendRedirect(url);
//        response.sendRedirect(URLEncoder.encode(url,"GBK"));
    }
    catch (Exception e) {
        response.sendRedirect("showErrorInfo.jsp");
    }
//    request.setAttribute("fileName", request.getParameter("fileName"));
%>
</html>
