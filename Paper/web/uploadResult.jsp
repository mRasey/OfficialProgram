<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>论文格式校正</title>
</head>
<body>
    <div align="center">
        上传成功<br>
        <a href="deal.jsp">开始处理</a>
        <%
            request.setAttribute("fileName", request.getAttribute("fileName"));
        %>
    </div>
</body>
</html>
