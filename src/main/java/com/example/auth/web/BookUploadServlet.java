package com.example.auth.web;

import com.example.auth.service.BookImporter;
import com.example.auth.service.BookImporter.ImportException;
import com.example.auth.util.Auth;
import com.example.auth.util.Http;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;

/** POST /books/upload (one PDF per request, field name "file"). Answers JSON. */
@WebServlet("/books/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024,
                 maxFileSize = 100L * 1024 * 1024,
                 maxRequestSize = 105L * 1024 * 1024)
public class BookUploadServlet extends HttpServlet {
    private final BookImporter importer = new BookImporter();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long uid = Auth.currentUserId(req);
        if (uid == null) {
            Http.json(resp, 401, false, Http.msg(req, "error.unauthorized"));
            return;
        }
        if (!Auth.validCsrf(req)) {
            Http.json(resp, 403, false, Http.msg(req, "error.csrf"));
            return;
        }

        Part part;
        try {
            part = req.getPart("file");
        } catch (IllegalStateException e) {                 // over the size limit
            Http.json(resp, 413, false, Http.msg(req, "books.error.tooLarge"));
            return;
        } catch (ServletException e) {
            Http.json(resp, 400, false, Http.msg(req, "books.error.invalid"));
            return;
        }
        if (part == null) {
            Http.json(resp, 400, false, Http.msg(req, "books.error.invalid"));
            return;
        }

        try {
            importer.importPdf(uid, part);
            Http.json(resp, 201, true, "OK");
        } catch (ImportException e) {
            int status = "books.error.duplicate".equals(e.getKey()) ? 409 : 400;
            Http.json(resp, status, false, Http.msg(req, e.getKey()));
        } catch (SQLException | IOException | RuntimeException e) {
            log("Upload failed", e);
            Http.json(resp, 500, false, Http.msg(req, "books.error.generic"));
        } finally {
            try { part.delete(); } catch (IOException ignored) { }
        }
    }
}
