package com.example.auth.util;

import com.example.auth.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Session + CSRF helpers. The session NEVER holds the password or its hash. */
public final class Auth {
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String CSRF = "csrfToken";

    private static final SecureRandom RANDOM = new SecureRandom();

    private Auth() {}

    public static boolean isLoggedIn(HttpServletRequest req) {
        return currentUserId(req) != null;
    }

    /** The logged-in user's id, or null. */
    public static Long currentUserId(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (Long) s.getAttribute(USER_ID);
    }

    /** Creates a brand-new session (prevents session fixation). */
    public static void login(HttpServletRequest req, User user) {
        HttpSession old = req.getSession(false);
        if (old != null) old.invalidate();
        HttpSession s = req.getSession(true);
        s.setAttribute(USER_ID, user.getId());
        s.setAttribute(USERNAME, user.getUsername());
        s.setAttribute(CSRF, newToken());
    }

    public static void logout(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
    }

    /** Returns the CSRF token of the current session, creating session/token if needed. */
    public static String csrfToken(HttpServletRequest req) {
        HttpSession s = req.getSession(true);
        String t = (String) s.getAttribute(CSRF);
        if (t == null) {
            t = newToken();
            s.setAttribute(CSRF, t);
        }
        return t;
    }

    /**
     * Accepts the token from the X-CSRF-Token header (used by fetch/XHR, and checked first so
     * multipart bodies are not parsed before validation) or from the "csrf" form field.
     */
    public static boolean validCsrf(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return false;
        String expected = (String) s.getAttribute(CSRF);
        String given = req.getHeader("X-CSRF-Token");
        if (given == null) given = req.getParameter("csrf");
        return expected != null && given != null
                && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                                         given.getBytes(StandardCharsets.UTF_8));
    }

    private static String newToken() {
        byte[] b = new byte[32];
        RANDOM.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}
