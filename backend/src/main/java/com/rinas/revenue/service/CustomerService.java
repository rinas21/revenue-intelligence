package com.rinas.revenue.service;

import com.rinas.revenue.domain.Customer;
import com.rinas.revenue.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer create(String name, UUID businessId) {
        Customer customer = new Customer(name, businessId);
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(UUID id, String name, UUID businessId) {
        Optional<Customer> optional = customerRepository.findById(id);
        if (optional.isPresent()) {
            Customer customer = optional.get();
            customer.setName(name);
            customer.setBusinessId(businessId);
            customer.setUpdatedAt(java.time.Instant.now());
            return customerRepository.save(customer);
        }
        return null;
    }

    public Optional<Customer> findById(UUID id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> findByBusinessIdAndEmail(UUID businessId, String email) {
        return customerRepository.findByBusinessIdAndEmail(businessId, email);
    }

    public Optional<Customer> findByBusinessId(UUID businessId) {
        return customerRepository.findByBusinessId(businessId);
    }

    public List<Customer> listAllByBusinessId(UUID businessId) {
        return customerRepository.findAll().stream()
                .filter(c -> c.getBusinessId().equals(businessId))
                .toList();
    }

    @Transactional
    public void delete(UUID id) {
        customerRepository.deleteById(id);
    }
}