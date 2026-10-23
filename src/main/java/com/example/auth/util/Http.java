package com.example.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Http {
    private Http() {}

    /** Parses a positive id; returns -1 when invalid. */
    public static long parseId(String s) {
        if (s == null) return -1;
        try {
            long v = Long.parseLong(s.trim());
            return v > 0 ? v : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Translates a message key with the language resolved by AppFilter. */
    public static String msg(HttpServletRequest req, String key) {
        String lang = (String) req.getAttribute("lang");
        if (lang == null) lang = "fr";
        try {
            return ResourceBundle.getBundle("i18n.messages", Locale.forLanguageTag(lang)).getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public static void json(HttpServletResponse resp, int status, boolean ok, String message)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("{\"ok\":" + ok + ",\"message\":\"" + escape(message) + "\"}");
    }

    static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
