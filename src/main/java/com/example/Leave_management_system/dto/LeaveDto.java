package com.example.Leave_management_system.dto;


import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LeaveDto {
    private Long id;

    private Long employeeId;
    private String employeeName;
    private String department;

    private String type;   // enum name as String
    private String status; // enum name as String

    private int days;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;

    private Long approverId;    // may be null if pending
    private String approverName; // may be null if pending
    private String decisionNote; // may be null

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}