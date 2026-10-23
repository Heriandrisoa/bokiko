package com.example.auth.web;

import com.example.auth.dao.BookDao;
import com.example.auth.model.Book;
import com.example.auth.service.BookStorage;
import com.example.auth.util.Auth;
import com.example.auth.util.Http;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Optional;

/**
 * GET /books/file?id=  streams the PDF, only to its owner.
 * Supports HTTP Range so PDF.js can load big files page by page.
 */
@WebServlet("/books/file")
public class BookFileServlet extends HttpServlet {
    private final BookDao dao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long uid = Auth.currentUserId(req);
        if (uid == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        long id = Http.parseId(req.getParameter("id"));
        Path file;
        try {
            Optional<Book> book = id > 0 ? dao.findByIdAndUser(id, uid) : Optional.empty();
            if (book.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            file = BookStorage.resolve(book.get().getFilePath());
        } catch (SQLException e) {
            log("DB error", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        if (!Files.isRegularFile(file)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long length = Files.size(file);
        long start = 0, end = length - 1;
        String range = req.getHeader("Range");
        boolean partial = false;
        if (range != null && range.startsWith("bytes=") && !range.contains(",")) {
            long[] r = parseRange(range.substring(6).trim(), length);
            if (r == null) {
                resp.setHeader("Content-Range", "bytes */" + length);
                resp.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }
            start = r[0];
            end = r[1];
            partial = true;
        }

        resp.setContentType("application/pdf");
        resp.setHeader("Accept-Ranges", "bytes");
        resp.setHeader("Content-Disposition", "inline");
        resp.setHeader("Cache-Control", "private, max-age=3600");
        if (partial) {
            resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            resp.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + length);
        }
        long remaining = end - start + 1;
        resp.setContentLengthLong(remaining);

        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r");
             OutputStream out = resp.getOutputStream()) {
            raf.seek(start);
            byte[] buf = new byte[64 * 1024];
            while (remaining > 0) {
                int n = raf.read(buf, 0, (int) Math.min(buf.length, remaining));
                if (n < 0) break;
                out.write(buf, 0, n);
                remaining -= n;
            }
        }
    }

    /** "a-b", "a-" or "-n". Returns {start,end} or null when unsatisfiable. */
    private static long[] parseRange(String spec, long length) {
        try {
            int dash = spec.indexOf('-');
            if (dash < 0 || length == 0) return null;
            String a = spec.substring(0, dash), b = spec.substring(dash + 1);
            long start, end;
            if (a.isEmpty()) {                       // last n bytes
                long n = Long.parseLong(b);
                if (n <= 0) return null;
                start = Math.max(0, length - n);
                end = length - 1;
            } else {
                start = Long.parseLong(a);
                end = b.isEmpty() ? length - 1 : Math.min(Long.parseLong(b), length - 1);
            }
            if (start < 0 || start > end || start >= length) return null;
            return new long[] { start, end };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
