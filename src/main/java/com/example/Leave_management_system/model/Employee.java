package com.example.Leave_management_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(name="uq_employee_email", columnNames = "email"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Email @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String department;

    @NotNull
    private LocalDate joiningDate;

    /** HR/Approver flag */
    @Builder.Default
    private boolean hr = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.EMPLOYEE;  // default role

    /** Per-year allocation (simple MVP policy) */
    @Builder.Default
    private Integer annualAllocationDays = 18;

    // ---------------- UserDetails methods ----------------
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security expects authorities as a list
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return this.email;  // email as username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // always active in MVP
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // always unlocked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // credentials never expire for now
    }

    @Override
    public boolean isEnabled() {
        return true; // always enabled
    }
}