package com.example.auth.web;

import com.example.auth.dao.BookDao;
import com.example.auth.util.Auth;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;

/** /home is the library: the grid of the user's books. */
@WebServlet("/home")
public class LibraryServlet extends HttpServlet {
    private final BookDao dao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long uid = Auth.currentUserId(req);
        if (uid == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            req.setAttribute("books", dao.findByUser(uid));
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.setAttribute("currentPage", "home");
        req.getRequestDispatcher("/WEB-INF/views/library.jsp").forward(req, resp);
    }
}
