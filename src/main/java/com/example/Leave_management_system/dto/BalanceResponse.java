package com.example.Leave_management_system.dto;

import lombok.*;
@Data @AllArgsConstructor
public class BalanceResponse {
    private Long employeeId;
    private int allocation;
    private int approvedDaysThisYear;
    private int remaining;
}
