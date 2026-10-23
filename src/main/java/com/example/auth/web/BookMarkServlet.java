package com.example.auth.web;

import com.example.auth.dao.BookMarkDao;
import com.example.auth.dao.BookDao;
import com.example.auth.model.BookMark;
import com.example.auth.util.Auth;
import com.example.auth.util.Http;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** JSON API for a user's bookmarks and notes on a book. */
@WebServlet("/books/marks")
public class BookMarkServlet extends HttpServlet {
    private final BookMarkDao dao = new BookMarkDao();
    private final BookDao bookDao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long userId = Auth.currentUserId(req);
        long bookId = Http.parseId(req.getParameter("bookId"));
        if (userId == null) { resp.sendError(HttpServletResponse.SC_UNAUTHORIZED); return; }
        if (bookId <= 0) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST); return; }
        try {
            if (!ownsBook(bookId, userId)) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
            List<BookMark> marks = dao.findByBook(bookId, userId);
            resp.setContentType("application/json;charset=UTF-8");
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < marks.size(); i++) {
                BookMark mark = marks.get(i);
                if (i > 0) json.append(',');
                json.append("{\"id\":").append(mark.id()).append(",\"page\":").append(mark.page())
                        .append(",\"note\":\"").append(escape(mark.note())).append("\"}");
            }
            resp.getWriter().write(json.append(']').toString());
        } catch (SQLException e) {
            log("Unable to load marks", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long userId = Auth.currentUserId(req);
        if (userId == null) { resp.sendError(HttpServletResponse.SC_UNAUTHORIZED); return; }
        if (!Auth.validCsrf(req)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        long bookId = Http.parseId(req.getParameter("bookId"));
        int page = parsePage(req.getParameter("page"));
        String note = req.getParameter("note");
        if (bookId <= 0 || page <= 0) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST); return; }
        if (note == null) note = "";
        if (note.length() > 500) note = note.substring(0, 500);
        try {
            if (!ownsBook(bookId, userId)) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
            BookMark mark = dao.insert(bookId, userId, page, note.trim());
            resp.setContentType("application/json;charset=UTF-8");
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("{\"id\":" + mark.id() + ",\"page\":" + mark.page()
                    + ",\"note\":\"" + escape(mark.note()) + "\"}");
        } catch (SQLException e) {
            log("Unable to create mark", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long userId = Auth.currentUserId(req);
        if (userId == null) { resp.sendError(HttpServletResponse.SC_UNAUTHORIZED); return; }
        if (!Auth.validCsrf(req)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        long bookId = Http.parseId(req.getParameter("bookId"));
        long markId = Http.parseId(req.getParameter("id"));
        if (bookId <= 0 || markId <= 0) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST); return; }
        try {
            if (!ownsBook(bookId, userId)) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
            resp.setStatus(dao.delete(markId, bookId, userId) ? HttpServletResponse.SC_NO_CONTENT : HttpServletResponse.SC_NOT_FOUND);
        } catch (SQLException e) {
            log("Unable to delete mark", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static int parsePage(String value) {
        try { return Integer.parseInt(value); } catch (NumberFormatException e) { return 0; }
    }
    private boolean ownsBook(long bookId, long userId) throws SQLException {
        return bookDao.findByIdAndUser(bookId, userId).isPresent();
    }
    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
