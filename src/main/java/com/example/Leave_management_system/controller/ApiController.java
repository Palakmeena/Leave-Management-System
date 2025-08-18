package com.example.Leave_management_system.controller;


import com.example.Leave_management_system.dto.ApplyLeaveRequest;
import com.example.Leave_management_system.dto.BalanceResponse;

import com.example.Leave_management_system.dto.CreateEmployeeRequest;
import com.example.Leave_management_system.dto.DecisionRequest;
import com.example.Leave_management_system.model.Employee;
import com.example.Leave_management_system.service.EmployeeService;
import com.example.Leave_management_system.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;

    // 1) Add employee
    @PostMapping("/employees")
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody CreateEmployeeRequest req){
        return ResponseEntity.ok(employeeService.create(req));
    }

    // 2) Apply for leave
    @PostMapping("/leaves")
    public ResponseEntity<?> apply(@Valid @RequestBody ApplyLeaveRequest req){
        Long id = leaveService.apply(req);
        return ResponseEntity.ok(java.util.Map.of("leaveId", id, "status", "PENDING"));
    }

    // 3) Approve leave
    @PutMapping("/leaves/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, @Valid @RequestBody DecisionRequest req){
        leaveService.approve(id, req);
        return ResponseEntity.ok(java.util.Map.of("leaveId", id, "status", "APPROVED"));
    }

    // 4) Reject leave
    @PutMapping("/leaves/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @Valid @RequestBody DecisionRequest req){
        leaveService.reject(id, req);
        return ResponseEntity.ok(java.util.Map.of("leaveId", id, "status", "REJECTED"));
    }

    // 5) Fetch leave balance
    @GetMapping("/employees/{id}/balance")
    public ResponseEntity<BalanceResponse> balance(@PathVariable Long id){
        return ResponseEntity.ok(leaveService.balance(id));
    }

    // 6) Get leave by ID
    @GetMapping("/leaves/{id}")
    public ResponseEntity<?> getLeave(@PathVariable Long id) {
        var leave = leaveService.getById(id);
        return ResponseEntity.ok(leave);
    }
}
