package com.billy.servlets;

import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CheckRegister extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject jsonObject = new JSONObject();
        System.err.println("in checkLogin");
        String id = req.getParameter("name");
        String name = req.getParameter("name");
        String password = req.getParameter("password");
        String job = req.getParameter("job");
        System.out.println(id + " " + name + " " + password + " " + job);
        jsonObject.put("contain", "true");
        resp.getWriter().print(jsonObject);
    }
}
