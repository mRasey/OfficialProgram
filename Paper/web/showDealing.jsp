<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.IOException" %>
<%@ page import="servlets.Scan" %>
<%
        out.println("<html>");
        out.println("<head>");
        out.println("<title>正在处理</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<p>正在处理</p>");
        out.println("</body>");
        out.println("</html>");

    try {
//        new Thread(new Scan(request, response)).start();
        String dirPath = request.getParameter("dirPath");
        String fileName = request.getParameter("fileName");
        response.sendRedirect("result.jsp?dirPath=" + dirPath + "&fileName=" + fileName);
    }
    catch (Exception e) {
        e.printStackTrace();
        response.sendRedirect("showErrorInfo.jsp");
    }
%>
<%--<html>--%>
<%--<head>--%>
    <%--<title>正在处理</title>--%>
    <%--<link rel="stylesheet" type="text/css" href="css/css-title/normalize.css" />--%>
    <%--<link rel="stylesheet" type="text/css" href="fonts/font-awesome-4.2.0/css/font-awesome.min.css" />--%>
    <%--<link rel="stylesheet" type="text/css" href="css/css-title/demo.css" />--%>
    <%--<link rel="stylesheet" type="text/css" href="css/css-title/component.css" />--%>
    <%--<link rel="stylesheet" type="text/css" href="css/css-button/button.css" />--%>
<%--</head>--%>
<%--<body>--%>
<%--<div class="container">--%>
    <%--<header class="codrops-header">--%>
        <%--<h1>北航毕设论文格式在线校正系统<span>（测试版）</span></h1>--%>
        <%--<p>正在处理</p>--%>
    <%--</header>--%>
<%--</div>--%>
<%--</body>--%>
<%--</html>--%>
