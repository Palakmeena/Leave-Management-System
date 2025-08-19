package com.example.Leave_management_system.dto;


import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import com.example.Leave_management_system.model.LeaveType;

@Data
public class ApplyLeaveRequest {
    @NotNull private Long employeeId;
    @NotNull private LocalDate startDate;
    @NotNull private LocalDate endDate;
    @Size(max = 200) private String reason;
    // optional; default to ANNUAL when not provided
    private LeaveType type = LeaveType.ANNUAL;
}
