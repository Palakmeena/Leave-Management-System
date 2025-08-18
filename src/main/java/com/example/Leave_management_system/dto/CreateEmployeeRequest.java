package com.example.Leave_management_system.dto;


import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateEmployeeRequest {
    @NotBlank private String name;
    @Email @NotBlank private String email;
    @NotBlank private String department;
    @NotNull private LocalDate joiningDate;
    private Boolean hr; // optional, default false
    @Positive @NotNull private Integer annualAllocationDays;
}
