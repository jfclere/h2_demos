package org.jfclere.h2demo;

import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

/**
 * Servlet implementation class
 */
@WebServlet(description = "Demo for H2", urlPatterns = { "/demo" })
public class Servlet extends HttpServlet {

    protected void service(HttpServletRequest req, HttpServletResponse resp)
           throws ServletException {
        System.out.println("sessionid: " + req.getRequestedSessionId());
    }
}
