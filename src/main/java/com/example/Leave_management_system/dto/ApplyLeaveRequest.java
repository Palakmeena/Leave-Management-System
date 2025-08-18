package com.example.Leave_management_system.dto;


import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ApplyLeaveRequest {
    @NotNull private Long employeeId;
    @NotNull private LocalDate startDate;
    @NotNull private LocalDate endDate;
    @Size(max = 200) private String reason;
}
