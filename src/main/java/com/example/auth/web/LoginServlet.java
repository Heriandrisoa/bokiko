package com.example.auth.web;

import com.example.auth.dao.UserDao;
import com.example.auth.model.User;
import com.example.auth.util.Auth;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    // Compared against when the user doesn't exist, so response time doesn't reveal it
    private static final String DUMMY_HASH = BCrypt.hashpw("dummy-password", BCrypt.gensalt(12));
    private final UserDao dao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (Auth.isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }
        show(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (Auth.isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }
        if (!Auth.validCsrf(req)) {
            req.setAttribute("error", "error.csrf");
            show(req, resp);
            return;
        }

        String identifier = trim(req.getParameter("identifier"));
        String password = req.getParameter("password");
        if (password == null) password = "";

        try {
            Optional<User> user = (identifier.isEmpty() || identifier.length() > 255)
                    ? Optional.empty() : dao.findByIdentifier(identifier);
            String hash = user.map(User::getPasswordHash).orElse(DUMMY_HASH);
            boolean ok = BCrypt.checkpw(password, hash) && user.isPresent();

            if (ok) {
                Auth.login(req, user.get());   // new session id
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        req.setAttribute("error", "login.error.invalid");   // generic message
        req.setAttribute("identifier", identifier);
        show(req, resp);
    }

    private void show(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Auth.csrfToken(req);
        req.setAttribute("currentPage", "login");
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }
}
