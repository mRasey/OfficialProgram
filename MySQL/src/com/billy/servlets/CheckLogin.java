package com.billy.servlets;

import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

import static com.billy.util.AddsingleQuotes.$;

public class CheckLogin extends HttpServlet {

    private Connection connection;
    private Statement statement;
    private String sql;

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/游戏公司管理系统?user=root&password=199507wz";
            connection = DriverManager.getConnection(url);
            if (!connection.isClosed())
                System.out.println("数据库连接成功!");
            statement = connection.createStatement();
        }
        catch(SQLException e) {
            System.out.println("error");
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject jsonObject = new JSONObject();
        System.err.println("in checkLogin");
        String name = req.getParameter("name");
        String password = req.getParameter("password");
        System.out.println(name + " " + password);
        try {
            sql = "SELECT 账号, 密码, 工作类别 FROM 员工 WHERE 账号 = " + $(name);
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next()) {
                if(resultSet.getString("密码").equals(password)) {
//                    System.out.println(resultSet.getString("工作类别"));
                    String s = resultSet.getString("工作类别");
                    jsonObject.put("ifFind", s);
                    resp.getWriter().print(jsonObject);
//                    resp.getWriter().print("true");
                    return;
                }
            }
            jsonObject.put("ifFind", "false");
            resp.getWriter().print(jsonObject);
//            resp.getWriter().print("false");
            System.out.println("false");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("操作数据库出错");
            jsonObject.put("ifFind", "error");
            resp.getWriter().print(jsonObject);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        JSONObject jsonObject = new JSONObject();
        System.err.println("in checkRegister");
        String name;
        String password;
        String job;
        String email;
        String realName;
        String phone;
        String address;

        if(req.getParameter("op").equals("checkIfExist")) {
            System.out.println("in checkIfExist");
            name = req.getParameter("name");
            password = req.getParameter("password");
            System.out.println(name + " " + password);

            try {
                sql = "SELECT 账号 FROM 员工 WHERE 账号 = " + $(name);
                ResultSet resultSet = statement.executeQuery(sql);
                if (resultSet.next()) {
                    resp.getWriter().print("true");
                    return;
                }
                resp.getWriter().print("false");
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("操作数据库出错");
                resp.getWriter().print("error");
            }
        }
        else if(req.getParameter("op").equals("submitAll")) {
            try {
                System.out.println("in submitAll");
                name = req.getParameter("name");
                password = req.getParameter("password");
                job = req.getParameter("job");
                email = req.getParameter("email");
                realName = req.getParameter("realName");
                phone = req.getParameter("phone");
                address = req.getParameter("address");
                sql = "INSERT " +
                        "INTO 员工 " +
                        "VALUES (" + $(name) + ","
                                    + $(password) + ","
                                    + $(job) + ","
                                    + $(email) + ","
                                    + $(realName) + ","
                                    + $(phone) + ","
                                    + $(address) + ");";
                statement.executeUpdate(sql);
                resp.getWriter().print("true");
            } catch (SQLException e) {
                e.printStackTrace();
                resp.getWriter().print("error");
            }
        }
    }

    @Override
    public void destroy() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
