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
        JSONObject jsonObject = new JSONObject();
        System.err.println("in checkLogin");
        String name = req.getParameter("name");
        String password = req.getParameter("password");
        System.out.println(name + " " + password);
        try {
            sql = "SELECT name, password FROM users WHERE name = " + "'" + name + "'";
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next()) {
                if(resultSet.getString("password").equals(password)) {
                    jsonObject.put("ifFind", "true");
                    resp.getWriter().print(jsonObject);
                    return;
                }
            }
            jsonObject.put("ifFind", "false");
            resp.getWriter().print(jsonObject);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("操作数据库出错");
            jsonObject.put("ifFind", "error");
            resp.getWriter().print(jsonObject);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject jsonObject = new JSONObject();
        System.err.println("in checkLogin");
        String name = req.getParameter("name");
        String password = req.getParameter("password");
        String email = req.getParameter("email");
        String job = req.getParameter("job");
        System.out.println(name + " " + password + " " + email + " " + job);

        try {
            sql = "SELECT name FROM users WHERE name = " + $(name);
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next()) {
                jsonObject.put("contain", "true");
                resp.getWriter().print(jsonObject);
                return;
            }
            sql = "INSERT " +
                    "INTO users " +
                    "VALUES (" + $(name) + "," + $(password) + "," + $(email) + "," + $(job) + ");";
//                    "VALUES (" + "'wz'," + "'123'," + "'123@123'," + "'email');";
            System.err.println(sql);
            statement.executeUpdate(sql);
            jsonObject.put("contain", "false");
            resp.getWriter().print(jsonObject);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("操作数据库出错");
            jsonObject.put("contain", "error");
            resp.getWriter().print(jsonObject);
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

    private boolean ifExist(String name) {
        try {
            sql = "SELECT * FROM users WHERE name = " + $(name);
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next()) {
                    return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("操作数据库出错");
        }
        return false;
    }


}