package com.mtd.servlet.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Class.forName("org.h2.Driver");
            String url = "jdbc:h2:~/perfume-servlet-db;AUTO_SERVER=TRUE";
            Connection conn = DriverManager.getConnection(url, "sa", "");
            sce.getServletContext().setAttribute("DB_CONN", conn);
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS users (id IDENTITY PRIMARY KEY, email VARCHAR(255) UNIQUE, password_hash VARCHAR(255), full_name VARCHAR(255), role VARCHAR(20));");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS categories (id IDENTITY PRIMARY KEY, name VARCHAR(255) UNIQUE, slug VARCHAR(255) UNIQUE);");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS products (id IDENTITY PRIMARY KEY, name VARCHAR(255), slug VARCHAR(255) UNIQUE, description CLOB, price_cents INT, image_url VARCHAR(512), stock INT, category_id BIGINT);");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS orders (id IDENTITY PRIMARY KEY, user_id BIGINT, total_cents INT, status VARCHAR(20));");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS order_items (id IDENTITY PRIMARY KEY, order_id BIGINT, product_id BIGINT, quantity INT, unit_price INT);");
            }
            // seed minimal
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("MERGE INTO categories (id, name, slug) KEY(slug) VALUES (NULL, 'Nước hoa', 'nuoc-hoa')");
                st.executeUpdate("MERGE INTO users (id, email, password_hash, full_name, role) KEY(email) VALUES (NULL, 'admin@example.com', '$2a$10$gL4KZqv0l0dDk2bqDxA9vO1nPSb0g0oYwR2H2pQh8YyS/7s6cF0tO', 'Site Admin', 'ADMIN')");
            }
            // seed sample products if empty
            try (Statement st = conn.createStatement()) {
                try (var rs = st.executeQuery("SELECT COUNT(*) FROM products")) {
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        long catId = 1;
                        try (var rs2 = st.executeQuery("SELECT id FROM categories WHERE slug='nuoc-hoa'")) { if (rs2.next()) catId = rs2.getLong(1); }
                        st.executeUpdate("MERGE INTO products (id, name, slug, description, price_cents, image_url, stock, category_id) KEY(slug) VALUES (NULL,'Perfume Aurora','perfume-aurora','Hương thơm quyến rũ, lưu hương lâu',8900000,'/images/placeholder.svg',50,"+catId+")");
                        st.executeUpdate("MERGE INTO products (id, name, slug, description, price_cents, image_url, stock, category_id) KEY(slug) VALUES (NULL,'Perfume Noir','perfume-noir','Hương trầm ấm, bí ẩn',6500000,'/images/placeholder.svg',30,"+catId+")");
                        st.executeUpdate("MERGE INTO products (id, name, slug, description, price_cents, image_url, stock, category_id) KEY(slug) VALUES (NULL,'Perfume Blossom','perfume-blossom','Hương hoa cỏ tươi mát',4200000,'/images/placeholder.svg',80,"+catId+")");
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            Connection conn = (Connection) sce.getServletContext().getAttribute("DB_CONN");
            if (conn != null) conn.close();
        } catch (Exception ignored) { }
    }
}
