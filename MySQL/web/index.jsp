<%--
  Created by IntelliJ IDEA.
  User: Billy
  Date: 2016/9/25
  Time: 13:06
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
      <title>游戏公司管理系统</title>
      <link rel="stylesheet" type="text/css" href="css/css-login/login.css" >
      <link rel="stylesheet" type="text/css" href="css/css-login/htmleaf-demo.css" >
      <link rel="stylesheet" type="text/css" href="css/css-login/normalize.css" >
      <link rel="stylesheet" type="text/css" href="css/css-select/cs-select.css" >
      <link rel="stylesheet" type="text/css" href="css/css-select/cs-skin-border.css" >

      <script type="text/javascript" src="js/jquery-3.1.0.js"></script>
      <script type="text/javascript" src="js/select/classie.js"></script>
      <script type="text/javascript" src="js/select/selectFx.js"></script>
      <script>
          (function() {
              [].slice.call( document.querySelectorAll( 'select.cs-select' ) ).forEach( function(el) {
                  new SelectFx(el);
              } );
          })();
      </script>
      <script type="text/javascript">
          function check_login() {
              var name=$("#user_name").val();
              var pass=$("#password").val();

              if(name == "" || pass == "") {
                  $("#login_form").removeClass('shake_effect');
                  setTimeout(function() {
                      $("#login_form").addClass('shake_effect')
                  },1);
              }
              else {
                  $.ajax({
                      type: "GET",
                      dataType: "json",
                      data: {
                          "name": name,
                          "password": pass
                      },
                      url: "CheckLogin"
                  }).done(function (data) {
//                  alert("done");
//                      var ifFind = data.toString();
                      var ifFind = eval(data).ifFind;
                      if (ifFind == "false") {
//                          alert("未注册，请注册后登陆");
                          document.getElementById("hint").style.display = "";
                          $("#login_form").removeClass('shake_effect');
                          setTimeout(function () {
                              $("#login_form").addClass('shake_effect')
                          }, 1);
                      }
                      else if(ifFind == "error"){
                          alert("发生错误");
                          self.location = "index.jsp";
                      }
                      else {
                          alert("欢迎登陆");
                          self.location = "/users/" + ifFind +  "/index.jsp";
                      }
                  }).fail(function () {
                      alert("fail");
                      self.location = "index.jsp";
                  });
              }
          }

          $(function(){
              $("#login").click(function(){
                  check_login();
                  return false;
              });
              $('.message a').click(function () {
                  self.location = "register.jsp";
              });
          })
      </script>
  </head>
  <body>
      <div class="htmleaf-container">
          <header class="htmleaf-header">
              <h1>游戏公司管理系统</h1>
          </header>
          <div id="wrapper" class="login-page">
              <div id="login_form" class="form">
                  <form class="login-form">
                      <input type="text" placeholder="用户名" id="user_name"/>
                      <input type="password" placeholder="密码" id="password"/>
                      <button id="login">登　录</button>
                      <p class="message">还没有账户? <a href="#">立刻创建</a></p>
                      <p class="message" style="display: none" id="hint">
                          <span style="color: red; ">账号不存在或密码错误</span></p>
                  </form>
              </div>
          </div>
      </div>
  </body>
</html>
