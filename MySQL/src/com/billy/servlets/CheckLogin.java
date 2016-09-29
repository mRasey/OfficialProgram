package com.billy.servlets;

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
            String url = "jdbc:mysql://localhost:3306/system?user=root&password=199507wz";
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
//        JSONObject jsonObject = new JSONObject();
        System.err.println("in checkLogin");
        String name = req.getParameter("name");
        String password = req.getParameter("password");
        System.out.println(name + " " + password);
        try {
            sql = "SELECT name, password FROM users WHERE name = " + "'" + name + "'";
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next()) {
                if(resultSet.getString("password").equals(password)) {
//                    jsonObject.put("ifFind", "true");
                    resp.getWriter().print("true");
                    return;
                }
            }
//            jsonObject.put("ifFind", "false");
            resp.getWriter().print("false");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("操作数据库出错");
//            jsonObject.put("ifFind", "error");
            resp.getWriter().print("error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        JSONObject jsonObject = new JSONObject();
        System.err.println("in checkRegister");
        String name;
        String password;
        String job;

        if(req.getParameter("op").equals("checkIfExist")) {
            System.out.println("in checkIfExist");
            name = req.getParameter("name");
            password = req.getParameter("password");
            System.out.println(name + " " + password);

            try {
                sql = "SELECT name FROM 用户 WHERE name = " + $(name);
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
                name = req.getParameter("name");
                password = req.getParameter("password");
                sql = "INSERT " +
                        "INTO 用户 " +
                        "VALUES (" + $(name) + "," + $(password) + ");";
                statement.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
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
