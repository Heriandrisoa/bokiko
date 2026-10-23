package com.example.auth.service;

import com.example.auth.util.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Files live under books.dir as {userId}/{uuid}.pdf and {userId}/{uuid}.jpg. */
public final class BookStorage {
    private static final Path ROOT;

    static {
        String dir = Config.get("books.dir", "BOOKS_DIR",
                System.getProperty("user.home") + "/jee-auth-data/books");
        ROOT = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(ROOT);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private BookStorage() {}

    public static Path userDir(long userId) throws IOException {
        Path d = ROOT.resolve(Long.toString(userId));
        Files.createDirectories(d);
        return d;
    }

    /** Turns a stored relative path into an absolute one, refusing anything outside the root. */
    public static Path resolve(String relative) {
        Path p = ROOT.resolve(relative).normalize();
        if (!p.startsWith(ROOT)) throw new SecurityException("Path escapes books.dir");
        return p;
    }

    public static String relative(Path absolute) {
        return ROOT.relativize(absolute).toString().replace('\\', '/');
    }

    public static void deleteQuietly(String relative) {
        if (relative == null || relative.isBlank()) return;
        try {
            Files.deleteIfExists(resolve(relative));
        } catch (IOException | SecurityException ignored) {
            // an orphan file is better than a failed request
        }
    }
}
