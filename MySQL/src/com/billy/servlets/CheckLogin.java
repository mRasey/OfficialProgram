package com.billy.servlets;

import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CheckLogin extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject jsonObject = new JSONObject();
        System.err.println("in checkLogin");
        String id = req.getParameter("id");
        String password = req.getParameter("password");
        System.out.println(id + " " + password);
        jsonObject.put("ifFind", "false");
        resp.getWriter().print(jsonObject);
    }

}
