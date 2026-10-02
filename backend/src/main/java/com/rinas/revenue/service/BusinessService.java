package com.rinas.revenue.service;

import com.rinas.revenue.common.exception.ResourceNotFoundException;
import com.rinas.revenue.domain.Business;
import com.rinas.revenue.dto.business.BusinessResponse;
import com.rinas.revenue.repository.BusinessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reads and updates the business the caller is authenticated against. */
@Service
public class BusinessService {

    private final BusinessRepository businessRepository;

    public BusinessService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    @Transactional(readOnly = true)
    public Business requireBusiness(java.util.UUID businessId) {
        return businessRepository.findById(businessId)
            .orElseThrow(() -> ResourceNotFoundException.of("Business", businessId));
    }

    @Transactional(readOnly = true)
    public BusinessResponse get(java.util.UUID businessId) {
        return BusinessResponse.from(requireBusiness(businessId));
    }

    @Transactional
    public BusinessResponse rename(java.util.UUID businessId, String name) {
        Business business = requireBusiness(businessId);
        business.setName(name);
        return BusinessResponse.from(businessRepository.save(business));
    }
}
