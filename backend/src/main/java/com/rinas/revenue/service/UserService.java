package com.rinas.revenue.service;

import com.rinas.revenue.common.exception.ConflictException;
import com.rinas.revenue.common.exception.ForbiddenOperationException;
import com.rinas.revenue.common.exception.ResourceNotFoundException;
import com.rinas.revenue.domain.Role;
import com.rinas.revenue.domain.User;
import com.rinas.revenue.dto.user.UserResponse;
import com.rinas.revenue.repository.UserRepository;
import com.rinas.revenue.security.SecurityUtils;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** User management within a single business. */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BusinessService businessService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BusinessService businessService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.businessService = businessService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list(UUID businessId) {
        return userRepository.findAllByBusinessId(businessId).stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse create(UUID businessId, UserResponse.CreateRequest request) {
        // Only an OWNER may create another OWNER. An ADMIN can staff the
        // business but cannot mint a peer that could later remove them.
        if (request.role() == Role.OWNER && !SecurityUtils.requirePrincipal().hasRole(Role.OWNER)) {
            throw new ForbiddenOperationException("Only an OWNER can create another OWNER");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw ConflictException.duplicate("Username '" + request.username() + "'");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw ConflictException.duplicate("Email '" + request.email() + "'");
        }
        User user = new User(
            request.username(),
            request.email(),
            passwordEncoder.encode(request.password()),
            businessService.requireBusiness(businessId),
            request.role());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void remove(UUID businessId, UUID userId) {
        User user = userRepository.findByIdAndBusinessId(userId, businessId)
            .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        if (user.getId().equals(SecurityUtils.requirePrincipal().getUserId())) {
            throw new ForbiddenOperationException("You cannot remove your own account");
        }
        userRepository.delete(user);
    }
}
