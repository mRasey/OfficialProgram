<%@ page import="execPy.ExecPy" %>
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
            <h1>北航毕设论文格式在线校正系统<span>（测试版）</span></h1>
        </header>
        <div align="center" id="upSuc">
            上传成功<br><br>
            <%
                String fileName = request.getParameter("fileName");
                new Thread(new ExecPy(fileName)).start();
            %>
            <a class="button" href="showDealing.jsp?fileName=<%=fileName%>">开始处理</a>
        </div>
    </div>
</body>
</html>
