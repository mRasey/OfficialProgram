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

        String PyDirPath = "C:\\Users\\Billy\\Documents\\GitHub\\Java\\Paper\\web\\";
        String checkPy = PyDirPath + "check.py";
        String modifyPy = PyDirPath + "modify.py";
        String DataDirPath = "C:\\Users\\Billy\\Documents\\GitHub\\Java\\Paper\\web\\data\\" + request.getAttribute("fileName") + "\\";

        System.out.println("check start");
        Process checkProcess = Runtime.getRuntime().exec("python" + " " + checkPy + " " + PyDirPath + " " + DataDirPath);
        String line;

        BufferedReader inputStream = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()));
        while ((line = inputStream.readLine()) != null) {
            System.out.println(line);
        }
        inputStream.close();

        BufferedReader errorStream = new BufferedReader(new InputStreamReader(checkProcess.getErrorStream()));
        while ((line = errorStream.readLine()) != null) {
            System.out.println(line);
        }
        errorStream.close();

        checkProcess.waitFor();//�ȴ�check����ִ�����
        System.out.println("check end");

        System.out.println("modify start");
        Process modifyProcess = Runtime.getRuntime().exec("python" + " " + modifyPy + " " + DataDirPath);

        inputStream = new BufferedReader(new InputStreamReader(modifyProcess.getInputStream()));
        while ((line = inputStream.readLine()) != null) {
            System.out.println(line);
        }
        inputStream.close();

        errorStream = new BufferedReader(new InputStreamReader(modifyProcess.getErrorStream()));
        while ((line = errorStream.readLine()) != null) {
            System.out.println(line);
        }
        errorStream.close();

        modifyProcess.waitFor();//�ȴ�modify����ִ�����
        System.out.println("modify end");

        request.setAttribute("fileName", request.getAttribute("fileName"));
//        response.sendRedirect("result.jsp");//��ת���������

    } catch (Exception e){
        e.printStackTrace();
    }
%>
<jsp:forward page="result.jsp"/>
</html>
