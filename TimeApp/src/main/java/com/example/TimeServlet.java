package main.java.com.example;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class TimeServlet extends HttpServlet {

    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML");
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String timezoneParam = request.getParameter("timezone");
        if ((timezoneParam == null || timezoneParam.isEmpty()) && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("lastTimezone".equals(cookie.getName())) {
                    timezoneParam = cookie.getValue();
                    break;
                }
            }
        }

        if (timezoneParam == null || timezoneParam.isEmpty()) {
            timezoneParam = "UTC";
        }

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timezoneParam);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid timezone");
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        String timeString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Cookie tzCookie = new Cookie("lastTimezone", timezoneParam);
        tzCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(tzCookie);

        WebContext ctx = new WebContext(request, response, getServletContext());
        ctx.setVariable("currentTime", timeString);
        ctx.setVariable("timezone", timezoneParam);

        response.setContentType("text/html;charset=UTF-8");

        templateEngine.process("time", ctx, response.getWriter());
    }
}