package com.billy.test;

import java.sql.*;

public class TestSQL {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch(ClassNotFoundException e) {
            System.out.println("找不到驱动程序");
        }
        try {
            String url = "jdbc:mysql://localhost:3306/游戏公司管理系统?user=root&password=199507wz";
            Connection connection = DriverManager.getConnection(url);
            if(!connection.isClosed())
                System.out.println("数据库连接成功!");

//            Statement statement = connection.createStatement();
//            String sql = "INSERT " +
//                    "INTO users " +
//                    "VALUES (" + $(name) + "," + $(password) + "," + $(email) + "," + $(job) + ");";
//                    "VALUES (" + "'wz'," + "'123'," + "'123@123'," + "'email');";
//            System.err.println(sql);
//            statement.executeQuery(sql);
//            if(resultSet.next()) {
//                System.out.println(resultSet.getString("name"));
//                System.out.println(resultSet.getString("password"));
//                System.out.println(resultSet.getString("email"));
//                System.out.println(resultSet.getString("job"));
//            }
//            else
//                System.out.println("not find");

            connection.close();
        }
        catch(SQLException e) {
            e.printStackTrace();
            System.out.println("error");
        }

    }
}
