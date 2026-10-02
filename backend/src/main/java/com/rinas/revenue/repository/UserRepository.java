package com.rinas.revenue.repository;

import com.rinas.revenue.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /** Tenant-scoped: never look a user up by id alone on a business-owned path. */
    Optional<User> findByIdAndBusinessId(UUID id, UUID businessId);

    List<User> findAllByBusinessId(UUID businessId);
}
