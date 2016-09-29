<%--
  Created by IntelliJ IDEA.
  User: Billy
  Date: 2016/9/28
  Time: 23:36
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>注册</title>

    <%--<link rel="stylesheet" type="text/css" href="css/css-login/login.css" >--%>
    <link rel="stylesheet" type="text/css" href="css/css-login/default.css">
    <link rel="stylesheet" type="text/css" href="css/css-login/reset.css">
    <link rel="stylesheet" type="text/css" href="css/css-login/styles.css">
    <link rel="stylesheet" type="text/css" href="css/css-login/login.css" >
    <link rel="stylesheet" type="text/css" href="css/css-login/htmleaf-demo.css" >
    <link rel="stylesheet" type="text/css" href="css/css-login/normalize.css" >

    <script type="text/javascript" src="js/jquery-3.1.0.js"></script>

</head>
<body>
<article class="htmleaf-content">
    <!-- multistep form -->
    <form id="msform">
        <!-- progressbar -->
        <ul id="progressbar">
            <li class="active">账号设置</li>
            <li>个人详细信息</li>
            <li>职位信息</li>
        </ul>
        <!-- fieldsets -->
        <fieldset>
            <h2 class="fs-title">创建你的账号</h2>
            <h3 class="fs-subtitle">这是第一步</h3>
            <input type="text" name="userName" id="userName" placeholder="账号" />
            <input type="password" name="password" id="password" placeholder="密码" />
            <input type="password" name="repeatPassword" id="repeatPassword" placeholder="重复密码" />
            <section>
                <select id="job" class="cs-select cs-skin-border">
                    <option value="" disabled selected>请选择职业类别</option>
                    <option value="management">管理层</option>
                    <option value="studioMajordomo">工作室总监</option>
                    <option value="workingGroupPrincipal">项目组负责人</option>
                    <option value="workingGroupMember">项目组组员</option>
                    <option value="operationalDepartmentMember">运营部成员</option>
                </select>
            </section>
            <input type="button" name="next" class="next action-button" value="下一步" />
        </fieldset>
        <fieldset>
            <h2 class="fs-title">填写个人信息</h2>
            <h3 class="fs-subtitle">个人详细信息是保密的，不会被泄露</h3>
            <input type="text" name="email" id="email" placeholder="邮箱" />
            <input type="text" name="realName" id="realName" placeholder="真实姓名" />
            <input type="text" name="phone" id="phone" placeholder="电话号码" />
            <textarea name="address" id="address" placeholder="家庭住址"></textarea>
            <input type="button" name="previous" class="previous action-button" value="上一步" />
            <input type="button" name="next" class="next action-button" value="下一步" />
        </fieldset>
        <fieldset>
            <h2 class="fs-title">职位信息</h2>
            <h3 class="fs-subtitle"></h3>
            <input style="display: none" type="text" name="managementJob" id="managementJob" placeholder="管理层职位" />
            <input style="display: none" type="text" name="studioMajordomo" id="studioMajordomo" placeholder="所属工作室" />
            <input style="display: none" type="text" name="workingGroupPrincipal" id="workingGroupPrincipal" placeholder="所属项目组" />
            <input style="display: none" type="text" name="groupMemberBelongGroup" id="workingGroupMember1" placeholder="所属项目组" />
            <input style="display: none" type="text" name="departmentMemberJobInfo" id="workingGroupMember2" placeholder="所属职位" />
            <input style="display: none" type="text" name="departmentMemberGame" id="operationalDepartmentMember" placeholder="运营游戏" />
            <input type="button" name="previous" class="previous action-button" value="上一步" />
            <input type="submit" name="submit" class="submit action-button" value="提交" />
        </fieldset>
    </form>
</article>

<%--<script src="http://libs.useso.com/js/jquery/2.1.1/jquery.min.js" type="text/javascript"></script>--%>
<%--<script>window.jQuery || document.write('<script src="js/jquery-2.1.1.min.js"><\/script>')</script>--%>
<script src="js/login/jquery.easing.min.js" type="text/javascript"></script>
<script>
    var current_fs, next_fs, previous_fs;
    var left, opacity, scale;
    var animating;
    var index = 0;
    var name;
    var password;
    var repeatPassword;
    var email;
    var realName;
    var phone;
    var address;
    var job;

    $('.next').click(function () {
        index = index + 1;


        if(index == 2) {
            job = document.getElementById("job").value;
            if(job == "management") {
                document.getElementById("managementJob").style.display = "";
                document.getElementById("studioMajordomo").style.display = "none";
                document.getElementById("workingGroupPrincipal").style.display = "none";
                document.getElementById("workingGroupMember1").style.display = "none";
                document.getElementById("workingGroupMember2").style.display = "none";
                document.getElementById("operationalDepartmentMember").style.display = "none";
            }
            else if(job == "studioMajordomo") {
                document.getElementById("managementJob").style.display = "none";
                document.getElementById("studioMajordomo").style.display = "";
                document.getElementById("workingGroupPrincipal").style.display = "none";
                document.getElementById("workingGroupMember1").style.display = "none";
                document.getElementById("workingGroupMember2").style.display = "none";
                document.getElementById("operationalDepartmentMember").style.display = "none";
            }
            else if(job == "workingGroupPrincipal") {
                document.getElementById("managementJob").style.display = "none";
                document.getElementById("studioMajordomo").style.display = "none";
                document.getElementById("workingGroupPrincipal").style.display = "";
                document.getElementById("workingGroupMember1").style.display = "none";
                document.getElementById("workingGroupMember2").style.display = "none";
                document.getElementById("operationalDepartmentMember").style.display = "none";
            }
            else if(job == "workingGroupMember") {
                document.getElementById("managementJob").style.display = "none";
                document.getElementById("studioMajordomo").style.display = "none";
                document.getElementById("workingGroupPrincipal").style.display = "none";
                document.getElementById("workingGroupMember1").style.display = "";
                document.getElementById("workingGroupMember2").style.display = "";
                document.getElementById("operationalDepartmentMember").style.display = "none";
            }
            else if(job == "operationalDepartmentMember") {
                document.getElementById("managementJob").style.display = "none";
                document.getElementById("studioMajordomo").style.display = "none";
                document.getElementById("workingGroupPrincipal").style.display = "none";
                document.getElementById("workingGroupMember1").style.display = "none";
                document.getElementById("workingGroupMember2").style.display = "none";
                document.getElementById("operationalDepartmentMember").style.display = "";
            }
        }

        if(index == 1) {
            name = document.getElementById("userName").value;
            password = document.getElementById("password").value;
            repeatPassword = document.getElementById("repeatPassword").value;
            job = document.getElementById("job").value;

            if(name == "" || password == "" || repeatPassword == ""
                    || job == "null" || password != repeatPassword) {
                $("#msform").removeClass('shake_effect');
                setTimeout(function() {
                    $("#msform").addClass('shake_effect')
                },1);
                index = index - 1;
                return false;
            }
            else {
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    data: {
                        "op": "checkIfExist",
                        "name": name,
                        "password": password
                    },
                    url: "CheckLogin"
                }).done(function (data) {
                    var contain = data.toString();
                    if (contain == "true") {
                        alert("用户已存在，请登录");
                        self.location = "index.jsp";
                    }
                    else if(contain == "error") {
                        alert("发生错误");
                        self.location = "index.jsp"
                    }
                }).fail(function () {
                    alert("fail");
                    self.location = "index.jsp";
                });
            }
        }
        if(index == 2) {
            email = document.getElementById("email").value;
            realName = document.getElementById("realName").value;
            phone = document.getElementById("phone").value;
            address = document.getElementById("address").value;

            if(email == "" || realName == "" || phone == "" || address == "") {
                $("#msform").removeClass('shake_effect');
                setTimeout(function () {
                    $("#msform").addClass('shake_effect')
                }, 1);
                index = index - 1;
                return false;
            }
        }

        if (animating)
            return false;
        animating = true;
        current_fs = $(this).parent();
        next_fs = $(this).parent().next();
        $('#progressbar li').eq($('fieldset').index(next_fs)).addClass('active');
        next_fs.show();
        current_fs.animate({ opacity: 0 }, {
            step: function (now, mx) {
                scale = 1 - (1 - now) * 0.2;
                left = now * 50 + '%';
                opacity = 1 - now;
                current_fs.css({ 'transform': 'scale(' + scale + ')' });
                next_fs.css({
                    'left': left,
                    'opacity': opacity
                });
            },
            duration: 800,
            complete: function () {
                current_fs.hide();
                animating = false;
            },
            easing: 'easeInOutBack'
        });
    });
    $('.previous').click(function () {
        index = index - 1;
        if (animating)
            return false;
        animating = true;
        current_fs = $(this).parent();
        previous_fs = $(this).parent().prev();
        $('#progressbar li').eq($('fieldset').index(current_fs)).removeClass('active');
        previous_fs.show();
        current_fs.animate({ opacity: 0 }, {
            step: function (now, mx) {
                scale = 0.8 + (1 - now) * 0.2;
                left = (1 - now) * 50 + '%';
                opacity = 1 - now;
                current_fs.css({ 'left': left });
                previous_fs.css({
                    'transform': 'scale(' + scale + ')',
                    'opacity': opacity
                });
            },
            duration: 800,
            complete: function () {
                current_fs.hide();
                animating = false;
            },
            easing: 'easeInOutBack'
        });
    });
    $('.submit').click(function () {
        if((job == "management" && document.getElementById("managementJob").value == "")
        || (job == "studioMajordomo") && document.getElementById("studioMajordomo").value == ""
        || (job == "workingGroupPrincipal") && document.getElementById("workingGroupPrincipal").value == ""
        || (job == "workingGroupMember") && (document.getElementById("workingGroupMember1").value == "" || document.getElementById("workingGroupMember2").value == "")
        || (job == "operationalDepartmentMember") && document.getElementById("operationalDepartmentMember").value == "") {
            $("#msform").removeClass('shake_effect');
            setTimeout(function() {
                $("#msform").addClass('shake_effect')
            },1);
            return false;
        }
        $.ajax({
            type: "POST",
            dataType: "json",
            data: {
                "op": "submitAll",
                "name": name,
                "password": password,
                "job": job,
                "email": email,
                "realName": realName,
                "phone": phone,
                "address": address,
                "management": document.getElementById("managementJob").value,
                "studioMajordomo": document.getElementById("studioMajordomo").value,
                "workingGroupPrincipal": document.getElementById("workingGroupPrincipal").value,
                "workingGroupMember1": document.getElementById("workingGroupMember1").value,
                "workingGroupMember2": document.getElementById("workingGroupMember2").value,
                "operationalDepartmentMember": document.getElementById("operationalDepartmentMember").value
            },
            url: "CheckLogin"
        }).done(function (data) {
            if(data.toString() == "true") {
                alert("注册成功");
                self.location = "index.jsp";
            }
            else if(data.toString() == "error") {
                alert("error");
            }
        });
        return false;
    });
</script>
</body>
</html>
