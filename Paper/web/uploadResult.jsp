<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>上传结果</title>
    <link rel="stylesheet" type="text/css" href="css/css-title/normalize.css" />
    <link rel="stylesheet" type="text/css" href="fonts/font-awesome-4.2.0/css/font-awesome.min.css" />
    <link rel="stylesheet" type="text/css" href="css/css-title/demo.css" />
    <link rel="stylesheet" type="text/css" href="css/css-title/component.css" />
    <link rel="stylesheet" type="text/css" href="css/css-button/button.css" />

</head>
<body>
    <div class="container">
        <header class="codrops-header">
            <h1>北航论文格式在线校正系统<span>Please upload your file here.</span></h1>
            <%--<p>测试版</p>--%>
        </header>
        <div align="center">
            上传成功<br><br>
            <%
                String fileName = (String) request.getAttribute("fileName");
//                request.setAttribute("fileName", request.getAttribute("fileName"));
            %>
            <a class="button" href="deal.jsp?fileName=<%=fileName%>">开始处理</a>
        </div>
    </div>
</body>
</html>
