package com.rinas.revenue.service;

import com.rinas.revenue.domain.User;
import com.rinas.revenue.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User create(String username, String email, String passwordHash, UUID businessId, String role) {
        User user = new User(username, email, passwordHash, businessId, role);
        return userRepository.save(user);
    }

    @Transactional
    public User updatePassword(UUID id, String newPasswordHash) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isPresent()) {
            User user = optional.get();
            user.setPasswordHash(newPasswordHash);
            user.setUpdatedAt(java.time.Instant.now());
            return userRepository.save(user);
        }
        return null;
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByBusinessIdAndRole(UUID businessId, String role) {
        return userRepository.findByBusinessIdAndRole(businessId, role);
    }

    public List<User> listAllByBusinessId(UUID businessId) {
        // JpaRepository doesn't have this directly; use a query or fetch all and filter
        // For now, fetch all and filter in Java
        return userRepository.findAll().stream()
                .filter(u -> u.getBusinessId().equals(businessId))
                .toList();
    }
}