package org.jfclere.h2demo;

import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.PushBuilder;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * Servlet implementation class
 */
@WebServlet(description = "Demo for H2", urlPatterns = { "/*" })
public class Servlet extends HttpServlet {

    protected void service(HttpServletRequest req, HttpServletResponse resp)
           throws ServletException, IOException {
        System.out.println("ContextPath requested: " + req.getContextPath());
        System.out.println("RequestURL requested: " + req.getRequestURL());
        System.out.println("RequestURL etag: " + req.getHeader("etag"));
        System.out.println("RequestURL referer: " + req.getHeader("referer"));
        String url = req.getRequestURL().toString();
        if (url.endsWith("/")) {
          try { build_index(resp); } catch (IOException ex) { System.out.println("Ex: " + ex); }
          return;
        }
        if (url.endsWith("/page")) {
          resp.setContentType("text/html");
          PrintWriter out = resp.getWriter();
          try { build_html(out, req.getContextPath(), "normal page"); } catch (IOException ex) { System.out.println("Ex: " + ex); }
          return;
        }
        if (url.endsWith("/push")) {
          try { build_push(resp, req); } catch (IOException ex) { System.out.println("Ex: " + ex); }
          return;
        }
        if (url.endsWith(".png")) {
          long now = System.currentTimeMillis();
          try { build_png(resp); } catch (IOException ex) { System.out.println("Ex: " + ex); }
          return;
        }
        System.out.println("RequestURL requested: " + url);
        
    }
    void build_html(PrintWriter out, String context, String where) throws IOException  {
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
          out.println(where);
          out.println("<div id=\"main\" >");
          out.println("<div>Load time: <span id=\"loadTime\">0</span>s.</div>");
          out.println("</div>");
          for (int i=0; i<25; i++) {
            out.println("<div id=\"row" + i + "\" >");
            for (int j=0; j<45; j++) {
              out.println("<img height=\"20\" width=\"20\" onload='imageLoadTime()' src=\"" + context + "/images/" + i + j + ".png\" />");
            }
            out.println("</div>");
          }
         out.println("</body>");
         out.println("</html>");
         out.flush();
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
    }
    void build_index(HttpServletResponse resp) throws IOException  {
          resp.setContentType("text/html");
          PrintWriter out = resp.getWriter();
          out.println("<!DOCTYPE html>");
          out.println("<html>");
          out.println("<head>");
          out.println("    <title>HTTP/2 DEMO</title>");
          
          out.println("<a href=\"page\">normal HTTP/2 page</a>");
          out.println("<a href=\"push\">push servlet page</a>");
          out.println("</head>");
          out.println("</body>");
          out.println("<body>");
          out.println("</html>");
    }
    void build_push(HttpServletResponse resp, HttpServletRequest req) throws IOException  {
          resp.setContentType("text/html");
          PrintWriter out = resp.getWriter();
          String context = req.getContextPath();
          
          PushBuilder pushBuilder = req.newPushBuilder();
          if (pushBuilder != null) {
            long etag = System.currentTimeMillis();
            sendPush(pushBuilder, etag);
            build_html(out, context, "Server push");
          } else {
            build_html(out, context, "No server push");
          }
          out.flush();
    }
    void sendPush(PushBuilder builder, long etag) {
          for (int i = 0; i < 25; i++) {
              for (int j = 0; j < 45; j++) {
                  String s = imagePath(i, j);
                  builder.path(s);
                  builder.setHeader("ETag", "" + etag);
                  builder.setHeader("Cache-Control", "max-age=120");
                  builder.push();
              }
          }
          System.out.println("sendPush Done!");
    }
    String imagePath(int i, int j) {
          return "images/" + i + j + ".png";
    }
}
