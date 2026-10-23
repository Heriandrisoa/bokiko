package com.example.auth.web;

import com.example.auth.dao.BookDao;
import com.example.auth.model.Book;
import com.example.auth.service.BookStorage;
import com.example.auth.util.Auth;
import com.example.auth.util.Http;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/books/cover")
public class BookCoverServlet extends HttpServlet {
    private final BookDao dao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long uid = Auth.currentUserId(req);
        if (uid == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        long id = Http.parseId(req.getParameter("id"));
        try {
            Optional<Book> book = id > 0 ? dao.findByIdAndUser(id, uid) : Optional.empty();
            if (book.isEmpty() || !book.get().isHasCover()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Path file = BookStorage.resolve(book.get().getCoverPath());
            if (!Files.isRegularFile(file)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            resp.setContentType("image/jpeg");
            resp.setContentLengthLong(Files.size(file));
            resp.setHeader("Cache-Control", "private, max-age=604800");   // covers never change
            Files.copy(file, resp.getOutputStream());
        } catch (SQLException e) {
            log("DB error", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
