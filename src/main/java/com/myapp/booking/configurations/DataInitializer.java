package com.myapp.booking.configurations;

import com.myapp.booking.enums.RoleName;
import com.myapp.booking.models.Role;
import com.myapp.booking.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        initializeRoles();
    }

    private void initializeRoles() {
        try {
            // Lặp qua tất cả RoleName trong enum
            for (RoleName roleName : RoleName.values()) {
                if (!roleRepository.existsByRoleName(roleName)) {
                    Role role = Role.builder()
                            .roleName(roleName)
                            .description(getDescriptionForRole(roleName))
                            .permissions(getPermissionsForRole(roleName))
                            .build();
                    roleRepository.save(role);
                    log.info("✅ Created role: {}", roleName);
                } else {
                    log.info("ℹ️ Role already exists: {}", roleName);
                }
            }
            log.info("✅ Role initialization completed!");
        } catch (Exception e) {
            log.error("❌ Error initializing roles: {}", e.getMessage(), e);
        }
    }

    private String getDescriptionForRole(RoleName roleName) {
        return switch (roleName) {
            case USER -> "Regular user who can browse and book services";
            case VENDOR -> "Service provider who can manage their services";
            case ADMIN -> "Administrator with full system access";
        };
    }

    private String getPermissionsForRole(RoleName roleName) {
        return switch (roleName) {
            case USER -> "[\"BROWSE_SERVICES\", \"MAKE_BOOKINGS\", \"WRITE_REVIEWS\", \"CHAT\", \"VIEW_OWN_BOOKINGS\"]";
            case VENDOR -> "[\"MANAGE_OWN_SERVICES\", \"VIEW_OWN_BOOKINGS\", \"MANAGE_BOOKINGS\", \"CHAT\", \"VIEW_REPORTS\"]";
            case ADMIN -> "[\"MANAGE_USERS\", \"MANAGE_VENDORS\", \"VIEW_REPORTS\", \"MANAGE_SERVICES\", \"MANAGE_BOOKINGS\", \"MANAGE_SYSTEM\"]";
        };
    }
}
