<%@ page contentType="text/html;charset=gbk" language="java" %>
<html lang="zh" class="no-js">
    <style>

    </style>
    <head>
      <title>论文格式校正</title>
      <link rel="stylesheet" type="text/css" href="css/css-title/normalize.css" />
      <link rel="stylesheet" type="text/css" href="fonts/font-awesome-4.2.0/css/font-awesome.min.css" />
      <link rel="stylesheet" type="text/css" href="css/css-title/demo.css" />
      <link rel="stylesheet" type="text/css" href="css/css-title/component.css" />

      <%--<meta charset="UTF-8">--%>
      <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <link rel="stylesheet" type="text/css" href="css/css-upload/normalize.css" />
      <%--<link rel="stylesheet" type="text/css" href="css/css-upload/default.css" />--%>
      <link rel="stylesheet" type="text/css" href="css/css-upload/component.css" />
      <script>(function(e,t,n){
          var r=e.querySelectorAll("html")[0];
          r.className=r.className.replace(/(^|\s)no-js(\s|$)/,"$1js$2")})(document,window,0);
      </script>
    </head>
    <script language="JavaScript">
        function checkFileType() {
            var fileName = document.getElementById("file").value;
            var postfix = fileName.substring(fileName.length - 5, fileName.length);
            if(postfix == ".docx") {
                return true;
            }
            else {
                alert("请输入有效的.docx文件" + fileName);
                return false;
            }
        }
    </script>
    <body>
        <div class="container">
            <header class="codrops-header">
                <h1>北航论文格式在线校正系统<span>Please upload your file here.</span></h1>
                <p>测试版</p>
            </header>
            <%--<div align="center">--%>
                <%--<form onsubmit="return checkFileType()" method="post" action="UploadFile.jsp" enctype="multipart/form-data">--%>
                    <%--<input type="file" name="file" id="file"><br />--%>
                    <%--<input type="submit" value="提交">--%>
                <%--</form>--%>
            <%--</div>--%>
            <div class="content">
                <div class="box">
                    <form onsubmit="return checkFileType()" method="post" action="UploadFile.jsp" enctype="multipart/form-data">
                    <input type="file" name="file" id="file" class="inputfile inputfile-3" data-multiple-caption="{count} files selected" multiple />
                    <label for="file">
                    <span>Choose a file&hellip;</span></label>
                    <br>
                    <%--<input type="file" name="file" id="file"><br />--%>
                    <input type="submit" value="提交">
                    </form>
                </div>
            </div>
        </div>
    </body>
    <script src="js/custom-file-input.js"></script>
</html>
