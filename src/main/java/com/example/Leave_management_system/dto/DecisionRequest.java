package com.example.Leave_management_system.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DecisionRequest {
    @NotNull private Long approverId;   // approver must be HR
    @NotBlank private String note;
}
