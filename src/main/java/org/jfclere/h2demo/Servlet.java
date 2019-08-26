package org.jfclere.h2demo;

import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * Servlet implementation class
 */
@WebServlet(description = "Demo for H2", urlPatterns = { "/demo", "/*" })
public class Servlet extends HttpServlet {

    protected void service(HttpServletRequest req, HttpServletResponse resp)
           throws ServletException {
        System.out.println("URL requested: " + req.getContextPath());
        System.out.println("ServletPath requested: " + req.getServletPath());
        System.out.println("RequestURL requested: " + req.getRequestURL());
        String url = req.getRequestURL().toString();
        if (url.endsWith("/demo")) {
          try { build_html(resp); } catch (IOException ex) { System.out.println("Ex: " + ex); }
          return;
        }
        if (url.endsWith(".png")) {
          try { build_png(resp); } catch (IOException ex) { System.out.println("Ex: " + ex); }
          return;
        }
        System.out.println("RequestURL requested: " + url);
        
    }
    void build_html(HttpServletResponse resp) throws IOException  {
          resp.setContentType("text/html");
          PrintWriter out = resp.getWriter();
          out.println("<!DOCTYPE html>");
          out.println("<html>");
          out.println("<head>");
          out.println("    <title>HTTP/2 DEMO</title>");
          out.println("</head>");
          out.println("<script>");
          out.println("        function imageLoadTime() {");
          out.println("                var lapsed = Date.now() - pageStart;");
          out.println("                document.getElementById(\"loadTime\").innerHTML = ((lapsed) / 1000).toFixed(2)");
          out.println("        }");
          out.println("        var pageStart = Date.now();");
          out.println("</script>");
          out.println("<body>");
          out.println("<div id=\"main\" >");
          out.println("<div>Load time: <span id=\"loadTime\">0</span>s.</div>");
          out.println("</div>");
          for (int i=0; i<25; i++) {
            out.println("<div id=\"row" + i + "\" >");
            for (int j=0; j<45; j++) {
              out.println("<img height=\"20\" width=\"20\" onload='imageLoadTime()' src=\"images/" + i + j + ".png\" />");
            }
            out.println("</div>");
          }
         out.println("</body>");
         out.println("</html>");
    }
    void build_png(HttpServletResponse resp) throws IOException  {
          resp.setContentType("image/png");
          ServletOutputStream sos = resp.getOutputStream();
          InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("../../tomcat.png");
          BufferedInputStream bis = new BufferedInputStream(is);
          int data;
          while((data = bis.read()) != -1) { 
            sos.write(data);
          } 
          bis.close();
          sos.close();
    }
}
