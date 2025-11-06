package com.myapp.booking.models;

import jakarta.persistence.*;
import lombok.*;
import com.myapp.booking.enums.RoleName;

@Entity
@Table(name = "roles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", unique = true, nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RoleName roleName;

    @Column(columnDefinition = "TEXT")
    private String description;


}
