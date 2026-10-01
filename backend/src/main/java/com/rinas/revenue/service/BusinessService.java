package com.rinas.revenue.service;

import com.rinas.revenue.domain.Business;
import com.rinas.revenue.repository.BusinessRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;

    public BusinessService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    @Transactional
    public Business create(String name, UUID businessId) {
        Business business = new Business(name, businessId);
        return businessRepository.save(business);
    }

    @Transactional
    public Business update(UUID id, String name, UUID businessId) {
        Optional<Business> optional = businessRepository.findById(id);
        if (optional.isPresent()) {
            Business business = optional.get();
            business.setName(name);
            business.setBusinessId(businessId);
            business.setUpdatedAt(java.time.Instant.now());
            return businessRepository.save(business);
        }
        return null;
    }

    public Optional<Business> findById(UUID id) {
        return businessRepository.findById(id);
    }

    public Optional<Business> findByBusinessId(UUID businessId) {
        return businessRepository.findByBusinessId(businessId);
    }

    public Optional<Business> findByName(String name) {
        return businessRepository.findByName(name);
    }

    public List<Business> listAll() {
        return businessRepository.findAll();
    }

    @Transactional
    public void delete(UUID id) {
        businessRepository.deleteById(id);
    }
}