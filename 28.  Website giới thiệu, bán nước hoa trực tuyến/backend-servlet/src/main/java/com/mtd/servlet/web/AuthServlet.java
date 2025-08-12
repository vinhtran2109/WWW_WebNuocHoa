package com.mtd.servlet.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtd.servlet.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

@WebServlet(name = "AuthServlet", urlPatterns = {"/api/auth/register", "/api/auth/login", "/api/auth/me"})
public class AuthServlet extends HttpServlet {
    private final ObjectMapper om = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/api/auth/register".equals(path)) handleRegister(req, resp);
        else if ("/api/auth/login".equals(path)) handleLogin(req, resp);
        else resp.sendError(404);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/api/auth/me".equals(req.getServletPath())) handleMe(req, resp); else resp.sendError(404);
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var json = om.readTree(req.getInputStream());
        String email = json.get("email").asText();
        String fullName = json.get("fullName").asText();
        String password = json.get("password").asText();
        try {
            Connection conn = (Connection) getServletContext().getAttribute("DB_CONN");
            try (PreparedStatement chk = conn.prepareStatement("SELECT 1 FROM users WHERE email=?")) {
                chk.setString(1, email);
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next()) { resp.setStatus(400); resp.getWriter().write("Email exists"); return; }
                }
            }
            String hash = BCrypt.hashpw(password, BCrypt.gensalt());
            try (PreparedStatement ins = conn.prepareStatement("INSERT INTO users(email, password_hash, full_name, role) VALUES (?,?,?, 'CUSTOMER')")) {
                ins.setString(1, email); ins.setString(2, hash); ins.setString(3, fullName); ins.executeUpdate();
            }
            resp.getWriter().write("OK");
        } catch (Exception e) { resp.sendError(500, e.getMessage()); }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var json = om.readTree(req.getInputStream());
        String email = json.get("email").asText();
        String password = json.get("password").asText();
        try {
            Connection conn = (Connection) getServletContext().getAttribute("DB_CONN");
            try (PreparedStatement ps = conn.prepareStatement("SELECT id, password_hash, full_name, role FROM users WHERE email=?")) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { resp.setStatus(400); resp.getWriter().write("Invalid"); return; }
                    long id = rs.getLong(1); String hash = rs.getString(2); String fullName = rs.getString(3); String role = rs.getString(4);
                    if (!BCrypt.checkpw(password, hash)) { resp.setStatus(400); resp.getWriter().write("Invalid"); return; }
                    String token = JwtUtil.generateToken(String.valueOf(id), Map.of("email", email, "fullName", fullName, "role", role), 7*24*3600);
                    resp.setContentType("application/json");
                    om.writeValue(resp.getOutputStream(), Map.of("token", token, "userId", id, "email", email, "fullName", fullName, "role", role));
                }
            }
        } catch (Exception e) { resp.sendError(500, e.getMessage()); }
    }

    private void handleMe(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) { resp.setStatus(401); return; }
        String token = auth.substring(7);
        var claims = JwtUtil.parseToken(token);
        resp.setContentType("application/json");
        om.writeValue(resp.getOutputStream(), claims);
    }
}
