<%@ page contentType="text/html;charset=gbk" language="java" pageEncoding="gbk" autoFlush="true" %>
<html>
<head>
    <title>���ڴ���</title>
</head>
<body>

</body>
<%@ page import="java.io.*,java.util.*, javax.servlet.*" %>
<%@ page import="javax.servlet.http.*" %>
<%@ page import="org.apache.commons.fileupload.*" %>
<%@ page import="org.apache.commons.fileupload.disk.*" %>
<%@ page import="org.apache.commons.fileupload.servlet.*" %>
<%@ page import="org.apache.commons.io.output.*" %>
<%@ page import="opXML.ExtractXML" %>

<%
    try {
        System.err.println("in uploadFile");
        String fileCode = (String) System.getProperties().get("file.encoding");
        File file;
        int maxFileSize = 5000 * 1024;
        int maxMemSize = 5000 * 1024;
        ServletContext context = pageContext.getServletContext();
        String filePath = request.getSession().getServletContext().getRealPath("/") + "data/";
//        System.err.println(dirPath);
//        String filePath = "C:\\Users\\Billy\\Documents\\GitHub\\OfficialProgram\\Paper\\data\\";
        // ��֤�ϴ�����������
        String contentType = request.getContentType();
        if ((contentType.indexOf("multipart/form-data") >= 0)) {

            DiskFileItemFactory factory = new DiskFileItemFactory();
            // �����ڴ��д洢�ļ������ֵ
            factory.setSizeThreshold(maxMemSize);
            // ���ش洢�����ݴ��� maxMemSize.
            factory.setRepository(new File("c:\\temp"));

            // ����һ���µ��ļ��ϴ��������
            ServletFileUpload upload = new ServletFileUpload(factory);
            // ��������ϴ����ļ���С
            upload.setSizeMax(maxFileSize);
            try {
                // ������ȡ���ļ�
                List fileItems = upload.parseRequest(request);

                // �����ϴ����ļ�
                Iterator i = fileItems.iterator();

                out.println("<html>");
                out.println("<head>");
                out.println("<title>JSP File upload</title>");
                out.println("</head>");
                out.println("<body>");
                while (i.hasNext()) {
                    FileItem fi = (FileItem) i.next();
                    if (!fi.isFormField()) {
                        // ��ȡ�ϴ��ļ��Ĳ���
                        String fieldName = fi.getFieldName();
                        String fileName = fi.getName();//�ϴ��ļ���
                        fileName = new String(fileName.getBytes(fileCode), fileCode);//����ϵͳĬ�ϱ������¶�ȡ�ļ���
                        boolean isInMemory = fi.isInMemory();
                        long sizeInBytes = fi.getSize();
                        // д���ļ�
                        String name;//�ϴ��ļ��Ķ�����
                        if (fileName.lastIndexOf("\\") >= 0) {
                            name = fileName.substring(fileName.lastIndexOf("\\") + 1);
                            File dir = new File(filePath + name);
                            if(dir.exists())
                                ExtractXML.deleteAllDir(filePath, name);
                            new File(filePath + name).mkdirs();
                            file = new File(filePath + name + "/", "origin.docx");//�ϴ��ļ�����Ϊorigin
                        } else {
                            name = fileName.substring(fileName.lastIndexOf("\\") + 1);
                            File dir = new File(filePath + name);
                            if(dir.exists())
                                ExtractXML.deleteAllDir(filePath, name);
                            new File(filePath + name).mkdirs();
                            file = new File(filePath + name + "/", "origin.docx");//�ϴ��ļ�����Ϊorigin
                        }
                        fi.write(file);
//                    new File(filePath + name + "\\", "check_out.txt").createNewFile();
//                    new File(filePath + name + "\\", "check_out1.txt").createNewFile();
                        out.println("Uploaded Filename: " + filePath + fileName + "<br>");
                        response.sendRedirect("uploadResult.jsp?dirPath=" + filePath + "&fileName=" + name);//�ϴ��ɹ�����ת���ϴ��������
                    }
                }
                out.println("</body>");
                out.println("</html>");
            } catch (Exception ex) {
                response.sendRedirect("showErrorInfo.jsp");
                System.out.println(ex);
            }
        } else {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet upload</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<p>No file uploaded</p>");
            out.println("</body>");
            out.println("</html>");
        }
    }
    catch (Exception e) {
        response.sendRedirect("showErrorInfo.jsp");
    }
%>
</html>
