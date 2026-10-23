package com.example.auth.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Reads db.properties from the classpath; environment variables take precedence. */
public final class Config {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) PROPS.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Config() {}

    public static String get(String key, String envName, String fallback) {
        String v = envName == null ? null : System.getenv(envName);
        if (v != null && !v.isBlank()) return v;
        String p = PROPS.getProperty(key);
        return (p != null && !p.isBlank()) ? p.trim() : fallback;
    }
}
