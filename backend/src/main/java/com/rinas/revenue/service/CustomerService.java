package com.rinas.revenue.service;

import com.rinas.revenue.common.exception.ConflictException;
import com.rinas.revenue.common.exception.ResourceNotFoundException;
import com.rinas.revenue.common.web.PageResponse;
import com.rinas.revenue.domain.Customer;
import com.rinas.revenue.dto.customer.CustomerRequest;
import com.rinas.revenue.dto.customer.CustomerResponse;
import com.rinas.revenue.repository.CustomerRepository;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Customers are always addressed through {@code findByIdAndBusinessId}. */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BusinessService businessService;

    public CustomerService(CustomerRepository customerRepository, BusinessService businessService) {
        this.customerRepository = customerRepository;
        this.businessService = businessService;
    }

    @Transactional
    public CustomerResponse create(UUID businessId, CustomerRequest request) {
        if (request.email() != null && !request.email().isBlank()
                && customerRepository.existsByBusinessIdAndEmailIgnoreCase(businessId, request.email())) {
            throw ConflictException.duplicate("Customer email '" + request.email() + "'");
        }
        Customer customer = new Customer(request.name(), businessService.requireBusiness(businessId));
        customer.setEmail(blankToNull(request.email()));
        customer.setPhone(request.phone());
        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> list(UUID businessId, Pageable pageable) {
        return PageResponse.from(customerRepository.findAllByBusinessId(businessId, pageable), CustomerResponse::from);
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(UUID businessId, UUID customerId) {
        return CustomerResponse.from(requireCustomer(businessId, customerId));
    }

    @Transactional
    public CustomerResponse update(UUID businessId, UUID customerId, CustomerRequest request) {
        Customer customer = requireCustomer(businessId, customerId);
        if (request.email() != null && !request.email().isBlank()
                && !request.email().equalsIgnoreCase(customer.getEmail())
                && customerRepository.existsByBusinessIdAndEmailIgnoreCase(businessId, request.email())) {
            throw ConflictException.duplicate("Customer email '" + request.email() + "'");
        }
        customer.setName(request.name());
        customer.setEmail(blankToNull(request.email()));
        customer.setPhone(request.phone());
        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional
    public void deactivate(UUID businessId, UUID customerId) {
        Customer customer = requireCustomer(businessId, customerId);
        customer.setActive(false);
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer requireCustomer(UUID businessId, UUID customerId) {
        return customerRepository.findByIdAndBusinessId(customerId, businessId)
            .orElseThrow(() -> ResourceNotFoundException.of("Customer", customerId));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
