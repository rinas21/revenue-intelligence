package com.rinas.revenue.service;

import com.rinas.revenue.domain.Product;
import com.rinas.revenue.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product create(String name, BigDecimal price, UUID businessId) {
        Product product = new Product(name, price, businessId);
        return productRepository.save(product);
    }

    @Transactional
    public Product update(UUID id, String name, BigDecimal price, String status, UUID businessId) {
        Optional<Product> optional = productRepository.findById(id);
        if (optional.isPresent()) {
            Product product = optional.get();
            product.setName(name);
            product.setPrice(price);
            product.setStatus(status);
            product.setBusinessId(businessId);
            product.setUpdatedAt(java.time.Instant.now());
            return productRepository.save(product);
        }
        return null;
    }

    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id);
    }

    public Optional<Product> findByBusinessIdAndName(UUID businessId, String name) {
        return productRepository.findByBusinessIdAndName(businessId, name);
    }

    public Optional<Product> findByBusinessId(UUID businessId) {
        return productRepository.findByBusinessId(businessId);
    }

    public List<Product> listAllByBusinessId(UUID businessId) {
        return productRepository.findAll().stream()
                .filter(p -> p.getBusinessId().equals(businessId))
                .toList();
    }

    @Transactional
    public void delete(UUID id) {
        productRepository.deleteById(id);
    }
}