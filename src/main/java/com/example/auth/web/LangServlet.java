package com.example.auth.web;

import com.example.auth.filter.AppFilter;
import com.example.auth.util.Http;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Map;

/** GET /lang?lang=fr|en|mg&next=login|register|home|read[&id=] */
@WebServlet("/lang")
public class LangServlet extends HttpServlet {
    // whitelist => no open redirect
    private static final Map<String, String> NEXT =
            Map.of("login", "/login", "register", "/register", "home", "/home");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String lang = req.getParameter("lang");
        if (lang != null && AppFilter.SUPPORTED.contains(lang)) {
            String ctx = req.getContextPath();
            Cookie c = new Cookie(AppFilter.LANG_COOKIE, lang);
            c.setPath(ctx.isEmpty() ? "/" : ctx);
            c.setMaxAge(365 * 24 * 3600);
            c.setHttpOnly(true);
            c.setSecure(req.isSecure());
            c.setAttribute("SameSite", "Lax");
            resp.addCookie(c);
        }
        String next = req.getParameter("next");
        String target = NEXT.getOrDefault(next, "/home");
        if ("read".equals(next)) {
            long id = Http.parseId(req.getParameter("id"));     // digits only => safe
            target = id > 0 ? "/read?id=" + id : "/home";
        }
        resp.sendRedirect(req.getContextPath() + target);
    }
}
