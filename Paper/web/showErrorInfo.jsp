<%@ page import="java.io.File" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>出错了＞﹏＜</title>
    <link rel="stylesheet" type="text/css" href="css/css-title/normalize.css" />
    <link rel="stylesheet" type="text/css" href="fonts/font-awesome-4.2.0/css/font-awesome.min.css" />
    <link rel="stylesheet" type="text/css" href="css/css-title/demo.css" />
    <link rel="stylesheet" type="text/css" href="css/css-title/component.css" />
</head>
<body>
    <div class="container">
        <header class="codrops-header">
            <h1>北航论文格式在线校正系统<span>Please upload your file here.</span></h1>
            <p>测试版</p>
        </header>
    <%
        out.println("<html>");
        out.println("<head>");
        out.println("</head>");
        out.println("<body>");
        out.println("<p>访问出错</p><br>");
        out.print("<a href='index.jsp'>");
        out.print("返回首页");
        out.println("</a>");
        out.println("</body>");
        out.println("</html>");
    %>
    </div>
</body>
</html>
