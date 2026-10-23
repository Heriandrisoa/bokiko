package com.example.auth.service;

import com.example.auth.dao.BookDao;
import jakarta.servlet.http.Part;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

/** Validates an uploaded PDF, stores it on disk, renders its cover and records it in PostgreSQL. */
public class BookImporter {
    static {
        System.setProperty("java.awt.headless", "true");
    }

    /** Carries a message key from messages_*.properties. */
    public static class ImportException extends Exception {
        private final String key;
        public ImportException(String key) { super(key); this.key = key; }
        public String getKey() { return key; }
    }

    private final BookDao dao = new BookDao();

    public long importPdf(long userId, Part part) throws ImportException, IOException, SQLException {
        String original = cleanName(part.getSubmittedFileName());
        if (original == null || !original.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new ImportException("books.error.notPdf");
        }
        if (original.length() > 255) original = original.substring(0, 251) + ".pdf";
        String title = original.substring(0, original.length() - 4).trim();
        if (title.isEmpty()) title = original;

        Path dir = BookStorage.userDir(userId);
        String base = UUID.randomUUID().toString();     // server-chosen name: no path tricks
        Path pdf = dir.resolve(base + ".pdf");
        Path cover = dir.resolve(base + ".jpg");
        boolean saved = false;

        try {
            // 1. stream to disk while hashing
            MessageDigest md = sha256();
            long size;
            try (InputStream in = new DigestInputStream(part.getInputStream(), md)) {
                size = Files.copy(in, pdf);
            }
            if (size == 0 || !hasPdfHeader(pdf)) throw new ImportException("books.error.notPdf");

            // 2. duplicate check (same user, same content)
            String sha = HexFormat.of().formatHex(md.digest());
            if (dao.existsByHash(userId, sha)) throw new ImportException("books.error.duplicate");

            // 3. parse for real, count pages, render page 1 as the cover
            int pages;
            boolean hasCover = false;
            try (PDDocument doc = Loader.loadPDF(pdf.toFile())) {
                pages = doc.getNumberOfPages();
                if (pages < 1) throw new ImportException("books.error.invalid");
                try {
                    BufferedImage img = new PDFRenderer(doc).renderImage(0, 1.0f, ImageType.RGB);
                    hasCover = ImageIO.write(img, "jpg", cover.toFile());
                } catch (Exception | LinkageError e) {
                    hasCover = false;           // a missing cover must not block the import
                }
            } catch (InvalidPasswordException e) {
                throw new ImportException("books.error.protected");
            } catch (IOException e) {
                throw new ImportException("books.error.invalid");
            }

            // 4. record it
            long id;
            try {
                id = dao.insert(userId, title, original, BookStorage.relative(pdf),
                        hasCover ? BookStorage.relative(cover) : null, pages, size, sha);
            } catch (SQLException e) {
                if ("23505".equals(e.getSQLState())) throw new ImportException("books.error.duplicate");
                throw e;
            }
            saved = true;
            return id;
        } finally {
            if (!saved) {
                try { Files.deleteIfExists(pdf); } catch (IOException ignored) { }
                try { Files.deleteIfExists(cover); } catch (IOException ignored) { }
            }
        }
    }

    private static String cleanName(String name) {
        if (name == null) return null;
        int i = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        return name.substring(i + 1).replaceAll("\\p{Cntrl}", "").trim();
    }

    /** The PDF spec allows the header anywhere in the first 1024 bytes. */
    private static boolean hasPdfHeader(Path p) throws IOException {
        try (InputStream in = Files.newInputStream(p)) {
            byte[] head = in.readNBytes(1024);
            return new String(head, StandardCharsets.ISO_8859_1).contains("%PDF-");
        }
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
