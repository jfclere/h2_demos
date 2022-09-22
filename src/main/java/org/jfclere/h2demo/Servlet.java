package org.jfclere.h2demo;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.PushBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.InputStream;

import java.util.Random;

/**
 * Servlet implementation class
 */
@WebServlet(description = "Demo for H2", urlPatterns = { "/*" })
public class Servlet extends HttpServlet {

    static int LINE = 40;
    static int COL  = 40;
    static Random rand = new Random();

    protected void service(HttpServletRequest req, HttpServletResponse resp)
           throws ServletException, IOException {
        // System.out.println("ContextPath requested: " + req.getContextPath());
        // System.out.println("RequestURL requested: " + req.getRequestURL());
        // System.out.println("RequestURL etag: " + req.getHeader("etag"));
        // System.out.println("RequestURL referer: " + req.getHeader("referer"));
        String url = req.getRequestURL().toString();
        if (url.endsWith("/page")) {
          resp.setContentType("text/html");
          PrintWriter out = resp.getWriter();
          try { build_html(out, req.getProtocol(), req.getContextPath(), false); } catch (IOException ex) { System.out.println("Ex: " + ex); }
          return;
        }
        else if (url.endsWith("/push")) {
          try { build_push(resp, req); } catch (IOException ex) { System.out.println("Ex: " + ex); }
          return;
        }
        else if (url.endsWith(".png")) {
          long now = System.currentTimeMillis();
          try { build_png(resp); } catch (IOException ex) { System.out.println("Ex: " + ex); }
          return;
        }
        else {
          try { build_index(resp, req.getServerName(), req.getRequestURI()); } catch (IOException ex) { System.out.println("Ex: " + ex); }
          return;
        }
        
    }
    void build_html(PrintWriter out, String scheme, String context, boolean push) throws IOException  {
          out.println("<!DOCTYPE html>");
          out.println("<html>");
          out.println("<head>");
          out.println("    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
          out.println("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
          out.println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
          /* we need the preloads like <link rel="preload" href="/styles/other.css" as="style"> */
          if (push) {
            build_imagesPreload(out, context);
            out.println("    <title>HTTP/2 SERVER PUSH DEMO</title>");
          } else {
            out.println("    <title>HTTP/2 DEMO</title>");
          }
          out.println("</head>");
          out.println("<script>");
          out.println("        function imageLoadTime() {");
          out.println("                var lapsed = Date.now() - pageStart;");
          out.println("                document.getElementById(\"loadTime\").innerHTML = ((lapsed) / 1000).toFixed(2)");
          out.println("        }");
          out.println("        var pageStart = Date.now();");
          out.println("</script>");
          out.println("<body>");
          if (push)
            out.println("Server PUSHED");
          else
            out.println("Normal " + scheme);
          out.println("<div id=\"main\" >");
          out.println("<div>Load time: <span id=\"loadTime\">0</span>s.</div>");
          out.println("</div>");
          int n = rand.nextInt(5000);
          for (int i=0; i<LINE; i++) {
            out.println("<div id=\"row" + i + "\" >");
            for (int j=0; j<COL; j++) {
              out.println("<img height=\"20\" width=\"20\" onload='imageLoadTime()' src=\"" + context + "/images/" + n + i + j + ".png\" />");
            }
            out.println("</div>");
          }
         out.println("</body>");
         out.println("</html>");
    }
    void build_png(HttpServletResponse resp) throws IOException  {
          resp.setContentType("image/png");
          resp.setHeader("Cache-Control", "max-age=120");
          ServletOutputStream sos = resp.getOutputStream();
          InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("../../tomcat.png");
          BufferedInputStream bis = new BufferedInputStream(is);
          int data;
          while((data = bis.read()) != -1) { 
            sos.write(data);
          } 
          bis.close();
    }
    void build_index(HttpServletResponse resp, String hostname, String uri) throws IOException  {
          resp.setContentType("text/html");
          PrintWriter out = resp.getWriter();
          out.println("<!DOCTYPE html>");
          out.println("<html>");
          out.println("<head>");
          out.println("    <title>HTTP/2 DEMO</title>");
          
          out.println("<a href=\"https://" + hostname + ":8002" + uri + "/page\">HTTP/2 page</a><br/>");
          out.println("<a href=\"https://" + hostname + ":8443" + uri + "/page\">HTTP/1 page</a><br/>");
          // out.println("<a href=\"push\">push servlet page</a>");
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

            /* push the images */
            long etag = System.currentTimeMillis();
            sendPush(pushBuilder, etag);

            /* send the html page */
            build_imagesLinks(resp, context);
            build_html(out, req.getProtocol(), context, true);

          } else {
            System.out.println("Problem pushBuilder == NULL");
            build_html(out, req.getProtocol(), context, false);
          }
          out.flush();
    }
    void sendPush(PushBuilder builder, long etag) {
          for (int i = 0; i < LINE; i++) {
              for (int j = 0; j < COL; j++) {
                  String s = imagePath(i, j);
                  builder.path(s);
                  builder.setHeader("ETag", "" + etag);
                  builder.setHeader("Cache-Control", "max-age=120");
                  builder.push();
              }
          }
          // System.out.println("sendPush Done!");
    }
    String imagePath(int i, int j) {
          return "images/" + i + j + ".png";
    }
    void build_imagesLinks(HttpServletResponse resp, String context) {
          for (int i = 0; i < LINE; i++) {
              for (int j = 0; j < COL; j++) {
                  String s = imagePath(i, j);
                  resp.addHeader("Link", "<" + context + "/" + s + ">; rel=preload; as=image");
              }
          }
    }
    void build_imagesPreload(PrintWriter out, String context) {
          for (int i = 0; i < LINE; i++) {
              for (int j = 0; j < COL; j++) {
                  String s = imagePath(i, j);
                  /* we need the preloads like <link rel="preload" href="/styles/other.css" as="style"> */
                  out.println("    <link rel=\"preload\" href=\"" + context + "/" + s + "\" rel=preload; as=\"image\">");
              }
          }
    }
}
