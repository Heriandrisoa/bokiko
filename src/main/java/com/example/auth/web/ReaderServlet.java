package com.example.auth.web;

import com.example.auth.dao.BookDao;
import com.example.auth.model.Book;
import com.example.auth.util.Auth;
import com.example.auth.util.Http;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/read")
public class ReaderServlet extends HttpServlet {
    private final BookDao dao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long uid = Auth.currentUserId(req);
        if (uid == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        long id = Http.parseId(req.getParameter("id"));
        try {
            Optional<Book> book = id > 0 ? dao.findByIdAndUser(id, uid) : Optional.empty();
            if (book.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }
            dao.touch(id, uid);
            req.setAttribute("book", book.get());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.setAttribute("currentPage", "read");
        req.getRequestDispatcher("/WEB-INF/views/reader.jsp").forward(req, resp);
    }
}
