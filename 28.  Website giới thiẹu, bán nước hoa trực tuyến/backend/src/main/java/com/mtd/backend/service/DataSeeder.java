package com.mtd.backend.service;

import com.mtd.backend.entity.Category;
import com.mtd.backend.entity.Product;
import com.mtd.backend.entity.User;
import com.mtd.backend.repository.CategoryRepository;
import com.mtd.backend.repository.ProductRepository;
import com.mtd.backend.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DataSeeder(CategoryRepository categoryRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        var cat = categoryRepository.findBySlug("nuoc-hoa").orElseGet(() -> {
            Category c = new Category(); c.setName("Nước hoa"); c.setSlug("nuoc-hoa"); return categoryRepository.save(c);
        });
        if (productRepository.count() == 0) {
            Product p1 = new Product(); p1.setName("Perfume Aurora"); p1.setSlug("perfume-aurora"); p1.setDescription("Hương thơm quyến rũ, lưu hương lâu"); p1.setPriceCents(8900000); p1.setImageUrl("/images/aurora.jpg"); p1.setStock(50); p1.setCategory(cat); productRepository.save(p1);
            Product p2 = new Product(); p2.setName("Perfume Noir"); p2.setSlug("perfume-noir"); p2.setDescription("Hương trầm ấm, bí ẩn"); p2.setPriceCents(6500000); p2.setImageUrl("/images/noir.jpg"); p2.setStock(30); p2.setCategory(cat); productRepository.save(p2);
            Product p3 = new Product(); p3.setName("Perfume Blossom"); p3.setSlug("perfume-blossom"); p3.setDescription("Hương hoa cỏ tươi mát"); p3.setPriceCents(4200000); p3.setImageUrl("/images/blossom.jpg"); p3.setStock(80); p3.setCategory(cat); productRepository.save(p3);
        }
        userRepository.findByEmail("admin@example.com").orElseGet(() -> {
            BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
            User u = new User(); u.setEmail("admin@example.com"); u.setFullName("Site Admin"); u.setRole("ADMIN"); u.setPasswordHash(enc.encode("admin123")); return userRepository.save(u);
        });
    }
}
