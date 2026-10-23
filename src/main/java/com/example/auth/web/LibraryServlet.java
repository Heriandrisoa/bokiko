package com.example.auth.web;

import com.example.auth.dao.BookDao;
import com.example.auth.util.Auth;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

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
            List<com.example.auth.model.Book> books = dao.findByUser(uid);
            String query = req.getParameter("q");
            if (query == null) query = "";
            query = query.trim();
            if (!query.isEmpty()) {
                String needle = query.toLowerCase(java.util.Locale.ROOT);
                books.removeIf(book -> !book.getTitle().toLowerCase(java.util.Locale.ROOT).contains(needle));
            }
            String sort = req.getParameter("sort");
            if ("title".equals(sort)) {
                books.sort(Comparator.comparing(com.example.auth.model.Book::getTitle, String.CASE_INSENSITIVE_ORDER));
            } else if ("added".equals(sort)) {
                books.sort(Comparator.comparing(com.example.auth.model.Book::getCreatedAt).reversed());
            } else {
                sort = "recent";
            }
            req.setAttribute("books", books);
            req.setAttribute("query", query);
            req.setAttribute("sort", sort);
            books.stream().filter(book -> book.getLastReadAt() != null).findFirst()
                    .ifPresent(book -> req.setAttribute("continueBook", book));
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.setAttribute("currentPage", "home");
        req.getRequestDispatcher("/WEB-INF/views/library.jsp").forward(req, resp);
    }
}
