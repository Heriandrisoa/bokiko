package com.example.auth.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

/** Resolves the language from the cookie, sets UTF-8 and basic security headers. */
@WebFilter("/*")
public class AppFilter implements Filter {
    public static final Set<String> SUPPORTED = Set.of("fr", "en", "mg");
    public static final String DEFAULT_LANG = "fr";
    public static final String LANG_COOKIE = "lang";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        request.setCharacterEncoding("UTF-8");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "same-origin");
        // Pages: never cached. Static assets: revalidate (cheap 304s). Servlets may override.
        if (request.getServletPath().startsWith("/static/")) {
            response.setHeader("Cache-Control", "no-cache");
        } else {
            response.setHeader("Cache-Control", "no-store");
        }

        String lang = DEFAULT_LANG;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (LANG_COOKIE.equals(c.getName()) && SUPPORTED.contains(c.getValue())) {
                    lang = c.getValue();
                    break;
                }
            }
        }
        request.setAttribute("lang", lang);
        chain.doFilter(req, res);
    }
}
