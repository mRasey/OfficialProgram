<%@ page import="java.io.File" %><%--
  Created by IntelliJ IDEA.
  User: Billy
  Date: 2016/9/19
  Time: 18:42
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>正在处理</title>
    <link rel="stylesheet" type="text/css" href="css/css-title/normalize.css" />
    <link rel="stylesheet" type="text/css" href="fonts/font-awesome-4.2.0/css/font-awesome.min.css" />
    <link rel="stylesheet" type="text/css" href="css/css-title/demo.css" />
    <link rel="stylesheet" type="text/css" href="css/css-title/component.css" />
    <link rel="stylesheet" type="text/css" href="css/css-button/button.css" />
</head>
<body>
<div class="container">
    <header class="codrops-header">
        <h1>北航毕设论文格式在线校正系统<span>（测试版）</span></h1>
        <p>正在处理</p>
    </header>
</div>
</body>
<%
    try {
        String fileName = (String) request.getAttribute("fileName");
        loop:
        while (true) {
            File dir = new File("C:/Users/Billy/Documents/GitHub/OfficialProgram/Paper/data/" + fileName);
            if(dir != null)
                for (File file : dir.listFiles())
                    if (file.getName().equals("resultWithComments.docx"))
                        break loop;
            out.print("正在处理");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        request.setAttribute("fileName", request.getParameter("fileName"));
        response.sendRedirect("result.jsp?fileName=" + fileName);
    }
    catch (Exception e) {
        e.printStackTrace();
        response.sendRedirect("showErrorInfo.jsp");
    }
%>
<%--<jsp:forward page="result.jsp"/>--%>
</html>
