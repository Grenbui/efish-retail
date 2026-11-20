package com.retail.storeback.category.repository;

import com.retail.storeback.category.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
    List<Product> findByCategoryId(UUID categoryId);
    List<Product> findByActiveTrue();
}
