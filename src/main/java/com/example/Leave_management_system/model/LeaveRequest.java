package com.example.Leave_management_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

import java.time.LocalDateTime;


@Entity
@Table(name = "leave_requests",
        indexes = {
                @Index(name="idx_lr_emp", columnList = "employee_id"),
                @Index(name="idx_lr_status", columnList = "status"),
                @Index(name="idx_lr_dates", columnList = "startDate,endDate")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false,
            foreignKey = @ForeignKey(name="fk_lr_employee"))
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LeaveType type = LeaveType.ANNUAL;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    /** persisted at apply-time to keep history stable */
    @Positive
    private Integer days;

    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id",
            foreignKey = @ForeignKey(name="fk_lr_approver"))
    private Employee approver;

    private String decisionNote;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void touch() { this.updatedAt = LocalDateTime.now(); }
}