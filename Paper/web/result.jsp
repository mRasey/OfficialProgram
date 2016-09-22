<%@ page import="java.io.File" %>
<%@ page import="java.io.FileReader" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>处理成功</title>
    <link rel="stylesheet" type="text/css" href="css/css-title/normalize.css" />
    <link rel="stylesheet" type="text/css" href="fonts/font-awesome-4.2.0/css/font-awesome.min.css" />
    <link rel="stylesheet" type="text/css" href="css/css-title/demo.css" />
    <link rel="stylesheet" type="text/css" href="css/css-title/component.css" />
    <link rel="stylesheet" type="text/css" href="css/css-button/button.css" />
</head>
<body>
    <%
        String fileName = request.getParameter("fileName");
        String dirPath = request.getParameter("dirPath");
//        String txtPath = dirPath + fileName + "/" + "check_out.txt";
//        String wordPath = dirPath + fileName + "/" + "result.docx";
    %>
    <%--<jsp:include page="<%=txtPath%>" flush="true"/>--%>
    <div class="container">
        <header class="codrops-header">
            <h1>北航毕设论文格式在线校正系统<span>（测试版）</span></h1>
            <%--<p>测试版</p>--%>
        </header>
        <div align="center">
            <%--查看结果<br><br>--%>
            <%--<a href="checkTXTInfo.jsp?dirName=<%=dirName%>">查看错误信息</a><br>--%>
            <a href="checkWordResult.jsp?dirPath=<%=dirPath%>&fileName=<%=fileName%>">修改过的Word文档</a><br><br>
            <a href="checkWordWithComments.jsp?dirPath=<%=dirPath%>&fileName=<%=fileName%>">带有批注的原Word文档</a><br><br><br>
            <a class="button" href="index.jsp">回到首页</a>
            <br><br>
            <p>
                Microsoft Office 2007及以上的版本可在菜单栏的审阅栏目下找到“比较”来比对修改前后的不同
            </p>
        </div>
    </div>
</body>
<head>
    <title>论文格式校正</title>
</head>
</html>
