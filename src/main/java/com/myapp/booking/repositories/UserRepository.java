package com.myapp.booking.repositories;

import com.myapp.booking.enums.RoleName;
import com.myapp.booking.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByFullNameContainingOrEmailContaining(
            String fullName,
            String email,
            Pageable pageable
    );

    // use enum RoleName from com.myapp.booking.enums
    Page<User> findByRole_RoleName(RoleName roleName, Pageable pageable);

    long countByRole_RoleName(RoleName roleName);

    long countByIsActiveTrue();

    long countByIsLockedTrue();

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByPhone(String phone);
}