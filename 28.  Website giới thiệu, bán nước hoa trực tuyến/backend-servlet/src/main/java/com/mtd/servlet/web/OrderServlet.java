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
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@WebServlet(name = "OrderServlet", urlPatterns = "/api/orders/checkout")
public class OrderServlet extends HttpServlet {
    private final ObjectMapper om = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) { resp.setStatus(401); return; }
        var json = om.readTree(req.getInputStream());
        var items = json.get("items");
        if (items == null || !items.isArray() || items.size()==0) { resp.setStatus(400); resp.getWriter().write("Cart empty"); return; }
        try {
            Connection conn = (Connection) getServletContext().getAttribute("DB_CONN");
            int total = 0;
            for (var it : items) {
                long productId = it.get("productId").asLong();
                int quantity = it.get("quantity").asInt();
                try (PreparedStatement ps = conn.prepareStatement("SELECT price_cents FROM products WHERE id=?")) {
                    ps.setLong(1, productId);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) total += rs.getInt(1) * quantity; else { resp.setStatus(400); return; } }
                }
            }
            long orderId;
            try (PreparedStatement ins = conn.prepareStatement("INSERT INTO orders(user_id, total_cents, status) VALUES (1, ?, 'PAID')", Statement.RETURN_GENERATED_KEYS)) { // demo: user_id=1
                ins.setInt(1, total);
                ins.executeUpdate();
                try (ResultSet keys = ins.getGeneratedKeys()) { keys.next(); orderId = keys.getLong(1); }
            }
            for (var it : items) {
                long productId = it.get("productId").asLong();
                int quantity = it.get("quantity").asInt();
                try (PreparedStatement ps = conn.prepareStatement("SELECT price_cents FROM products WHERE id=?")) {
                    ps.setLong(1, productId);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); int unitPrice = rs.getInt(1);
                        try (PreparedStatement ins = conn.prepareStatement("INSERT INTO order_items(order_id, product_id, quantity, unit_price) VALUES (?,?,?,?)")) {
                            ins.setLong(1, orderId); ins.setLong(2, productId); ins.setInt(3, quantity); ins.setInt(4, unitPrice); ins.executeUpdate();
                        }
                    }
                }
            }
            resp.setContentType("application/json"); om.writeValue(resp.getOutputStream(), Map.of("orderId", orderId, "totalCents", total));
        } catch (Exception e) { resp.sendError(500, e.getMessage()); }
    }
}
