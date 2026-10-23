package com.example.auth.web;

import com.example.auth.dao.BookDao;
import com.example.auth.util.Auth;
import com.example.auth.util.Http;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;

/** POST /books/progress  (id, page): saves the reading position for the logged-in user. */
@WebServlet("/books/progress")
public class BookProgressServlet extends HttpServlet {
    private final BookDao dao = new BookDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long uid = Auth.currentUserId(req);
        if (uid == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (!Auth.validCsrf(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        long id = Http.parseId(req.getParameter("id"));
        int page;
        try {
            page = Integer.parseInt(req.getParameter("page"));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (id < 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            dao.updateProgress(id, uid, page);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            log("DB error", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
