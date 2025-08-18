package com.example.Leave_management_system.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(name="uq_employee_email", columnNames = "email"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Email @NotBlank
    private String email;

    @NotBlank
    private String department;

    @NotNull
    private LocalDate joiningDate;

    /** HR/Approver flag */
    @Builder.Default
    private boolean hr = false;

    /** Per-year allocation (simple MVP policy) */
    @Builder.Default
    private Integer annualAllocationDays = 18;
}
