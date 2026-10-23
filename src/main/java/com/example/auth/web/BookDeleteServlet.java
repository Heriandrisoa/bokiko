package com.example.auth.web;

import com.example.auth.dao.BookDao;
import com.example.auth.model.Book;
import com.example.auth.service.BookStorage;
import com.example.auth.util.Auth;
import com.example.auth.util.Http;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/books/delete")
public class BookDeleteServlet extends HttpServlet {
    private final BookDao dao = new BookDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long uid = Auth.currentUserId(req);
        if (uid == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!Auth.validCsrf(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        long id = Http.parseId(req.getParameter("id"));
        try {
            Optional<Book> book = id > 0 ? dao.findByIdAndUser(id, uid) : Optional.empty();
            if (book.isPresent() && dao.delete(id, uid)) {
                BookStorage.deleteQuietly(book.get().getFilePath());
                BookStorage.deleteQuietly(book.get().getCoverPath());
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        resp.sendRedirect(req.getContextPath() + "/home");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/home");
    }
}
