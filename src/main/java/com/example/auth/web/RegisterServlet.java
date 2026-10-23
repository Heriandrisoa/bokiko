package com.example.auth.web;

import com.example.auth.dao.UserDao;
import com.example.auth.util.Auth;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.-]{3,30}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private final UserDao dao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (Auth.isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }
        show(req, resp, new ArrayList<>());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (Auth.isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }
        List<String> errors = new ArrayList<>();
        String username = trim(req.getParameter("username"));
        String email = trim(req.getParameter("email"));
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String confirm = req.getParameter("confirm") == null ? "" : req.getParameter("confirm");

        req.setAttribute("username", username);
        req.setAttribute("email", email);

        if (!Auth.validCsrf(req)) {
            errors.add("error.csrf");
            show(req, resp, errors);
            return;
        }
        if (!USERNAME.matcher(username).matches()) errors.add("register.error.username");
        if (email.length() > 255 || !EMAIL.matcher(email).matches()) errors.add("register.error.email");
        // BCrypt only uses the first 72 bytes
        int pwBytes = password.getBytes(StandardCharsets.UTF_8).length;
        if (password.length() < 8 || pwBytes > 72) errors.add("register.error.password");
        if (!password.equals(confirm)) errors.add("register.error.confirm");

        try {
            if (errors.isEmpty()) {
                if (dao.usernameExists(username)) errors.add("register.error.usernameTaken");
                if (dao.emailExists(email)) errors.add("register.error.emailTaken");
            }
            if (errors.isEmpty()) {
                try {
                    dao.create(username, email, BCrypt.hashpw(password, BCrypt.gensalt(12)));
                    resp.sendRedirect(req.getContextPath() + "/login?registered=1");
                    return;
                } catch (SQLException e) {
                    if ("23505".equals(e.getSQLState())) {      // unique violation (race condition)
                        errors.add("register.error.usernameTaken");
                    } else {
                        throw e;
                    }
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        show(req, resp, errors);
    }

    private void show(HttpServletRequest req, HttpServletResponse resp, List<String> errors)
            throws ServletException, IOException {
        Auth.csrfToken(req);
        req.setAttribute("errors", errors);
        req.setAttribute("currentPage", "register");
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }
}
