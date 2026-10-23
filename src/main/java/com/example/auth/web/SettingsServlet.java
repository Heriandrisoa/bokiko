package com.example.auth.web;

import com.example.auth.util.Auth;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Displays application settings, including theme customizer. */
@WebServlet("/settings")
public class SettingsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (Auth.currentUserId(req) == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.setAttribute("currentPage", "settings");
        req.getRequestDispatcher("/WEB-INF/views/settings.jsp").forward(req, resp);
    }
}

