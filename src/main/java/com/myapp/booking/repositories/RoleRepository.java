package com.myapp.booking.repositories;

import com.myapp.booking.models.Role;
import com.myapp.booking.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(RoleName roleName); // ✅ kiểu đúng
}
