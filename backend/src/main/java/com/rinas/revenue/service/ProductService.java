package com.rinas.revenue.service;

import com.rinas.revenue.common.exception.ConflictException;
import com.rinas.revenue.common.exception.ResourceNotFoundException;
import com.rinas.revenue.common.web.PageResponse;
import com.rinas.revenue.domain.Product;
import com.rinas.revenue.domain.ProductStatus;
import com.rinas.revenue.dto.product.ProductRequest;
import com.rinas.revenue.dto.product.ProductResponse;
import com.rinas.revenue.repository.ProductRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Products are always addressed through {@code findByIdAndBusinessId}. There is
 * no method here that fetches a product by id alone.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final BusinessService businessService;

    public ProductService(ProductRepository productRepository, BusinessService businessService) {
        this.productRepository = productRepository;
        this.businessService = businessService;
    }

    @Transactional
    public ProductResponse create(UUID businessId, ProductRequest request) {
        if (productRepository.existsByBusinessIdAndNameIgnoreCase(businessId, request.name())) {
            throw ConflictException.duplicate("Product '" + request.name() + "'");
        }
        Product product = new Product(request.name(), request.price(), businessService.requireBusiness(businessId));
        product.setDescription(request.description());
        if (request.status() != null) {
            product.setStatus(request.status());
        }
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> list(UUID businessId, ProductStatus status, Pageable pageable) {
        Page<Product> page = status == null
            ? productRepository.findAllByBusinessId(businessId, pageable)
            : productRepository.findAllByBusinessIdAndStatus(businessId, status, pageable);
        return PageResponse.from(page, ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(UUID businessId, UUID productId) {
        return ProductResponse.from(requireProduct(businessId, productId));
    }

    @Transactional
    public ProductResponse update(UUID businessId, UUID productId, ProductRequest request) {
        Product product = requireProduct(businessId, productId);
        if (!product.getName().equalsIgnoreCase(request.name())
                && productRepository.existsByBusinessIdAndNameIgnoreCase(businessId, request.name())) {
            throw ConflictException.duplicate("Product '" + request.name() + "'");
        }
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        if (request.status() != null) {
            product.setStatus(request.status());
        }
        return ProductResponse.from(productRepository.save(product));
    }

    /**
     * Soft-deactivates a product. It disappears from active listings but remains
     * valid on historical orders. Destructive deletion is deliberately not
     * offered for a product that may be referenced by a sale.
     */
    @Transactional
    public void deactivate(UUID businessId, UUID productId) {
        Product product = requireProduct(businessId, productId);
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product requireProduct(UUID businessId, UUID productId) {
        return productRepository.findByIdAndBusinessId(productId, businessId)
            .orElseThrow(() -> ResourceNotFoundException.of("Product", productId));
    }
}
