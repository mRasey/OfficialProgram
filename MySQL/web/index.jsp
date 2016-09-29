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
      <title>学生信息管理系统</title>
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
                      var ifFind = data.toString();
                      if (ifFind == "true") {
                          alert("欢迎登陆");
                      }
                      else if (ifFind == "false") {
                          alert("未注册，请注册后登陆");
                          $("#login_form").removeClass('shake_effect');
                          setTimeout(function () {
                              $("#login_form").addClass('shake_effect')
                          }, 1);
                      }
                      else {
                          alert("发生错误");
                          self.location = "index.jsp";
                      }
                  }).fail(function () {
                      alert("fail");
                      self.location = "index.jsp";
                  });
              }
          }

          $(function(){
//              $("#create").click(function(){
//                  check_register();
//                  return false;
//              });
              $("#login").click(function(){
                  check_login();
                  return false;
              });
              $('.message a').click(function () {
                  self.location = "register.jsp";
//                  $('form').animate({
//                      height: 'toggle',
//                      opacity: 'toggle'
//                  }, 'slow');
              });
          })
      </script>
  </head>
  <body>
      <div class="htmleaf-container">
          <header class="htmleaf-header">
              <h1>信息管理系统</h1>
          </header>
          <div id="wrapper" class="login-page">
              <div id="login_form" class="form">
                  <%--<form name="form" class="register-form">--%>
                      <%--<input type="text" placeholder="用户名" id="r_user_name"/>--%>
                      <%--<input type="password" placeholder="密码" id="r_password" />--%>
                      <%--<input type="password" placeholder="确认密码" id="r_confirm_password" />--%>
                      <%--<section>--%>
                          <%--<select id="job" class="cs-select cs-skin-border">--%>
                              <%--<option value="" disabled selected>请选择职业类别</option>--%>
                              <%--<option value="management">管理层</option>--%>
                              <%--<option value="studioMajordomo">工作室总监</option>--%>
                              <%--<option value="workingGroupPrincipal">项目组负责人</option>--%>
                              <%--<option value="workingGroupMember">项目组组员</option>--%>
                              <%--<option value="operationalDepartmentMember">运营部成员</option>--%>
                          <%--</select>--%>
                      <%--</section>--%>
                      <%--<br>--%>
                      <%--<button id="create">创建账户</button>--%>
                      <%--<p class="message">已经有了一个账户? <a href="#">立刻登录</a></p>--%>
                  <%--</form>--%>
                  <form class="login-form">
                      <input type="text" placeholder="用户名" id="user_name"/>
                      <input type="password" placeholder="密码" id="password"/>
                      <button id="login">登　录</button>
                      <p class="message">还没有账户? <a href="#">立刻创建</a></p>
                      <p id="hint"></p>
                  </form>
              </div>
          </div>
      </div>
  </body>
</html>
