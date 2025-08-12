package com.mtd.servlet.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ProductServlet", urlPatterns = {"/api/products", "/api/products/*"})
public class ProductServlet extends HttpServlet {
    private final ObjectMapper om = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path != null && path.length() > 1) {
            String slug = path.substring(1);
            detail(slug, req, resp);
        } else list(req, resp);
    }

    private void list(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String q = req.getParameter("q");
        try {
            Connection conn = (Connection) getServletContext().getAttribute("DB_CONN");
            String sql = "SELECT id, name, slug, price_cents, image_url FROM products" + (q != null && !q.isBlank() ? " WHERE LOWER(name) LIKE ?" : "");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (q != null && !q.isBlank()) ps.setString(1, "%" + q.toLowerCase() + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    List<Map<String,Object>> list = new ArrayList<>();
                    while (rs.next()) {
                        Map<String,Object> m = new HashMap<>();
                        m.put("id", rs.getLong(1));
                        m.put("name", rs.getString(2));
                        m.put("slug", rs.getString(3));
                        m.put("priceCents", rs.getInt(4));
                        m.put("imageUrl", rs.getString(5));
                        list.add(m);
                    }
                    resp.setContentType("application/json"); om.writeValue(resp.getOutputStream(), list);
                }
            }
        } catch (Exception e) { resp.sendError(500, e.getMessage()); }
    }

    private void detail(String slug, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Connection conn = (Connection) getServletContext().getAttribute("DB_CONN");
            try (PreparedStatement ps = conn.prepareStatement("SELECT id, name, slug, description, price_cents, image_url FROM products WHERE slug=?")) {
                ps.setString(1, slug);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { resp.sendError(404); return; }
                    Map<String,Object> m = new HashMap<>();
                    m.put("id", rs.getLong(1));
                    m.put("name", rs.getString(2));
                    m.put("slug", rs.getString(3));
                    m.put("description", rs.getString(4));
                    m.put("priceCents", rs.getInt(5));
                    m.put("imageUrl", rs.getString(6));
                    resp.setContentType("application/json"); om.writeValue(resp.getOutputStream(), m);
                }
            }
        } catch (Exception e) { resp.sendError(500, e.getMessage()); }
    }
}
