package com.example.Leave_management_system.service;


import com.example.Leave_management_system.Repository.EmployeeRepository;
import com.example.Leave_management_system.dto.CreateEmployeeRequest;

import com.example.Leave_management_system.exception.ConflictException;
import com.example.Leave_management_system.exception.NotFoundException;
import com.example.Leave_management_system.model.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Employee create(CreateEmployeeRequest req){
        if (employeeRepository.existsByEmail(req.getEmail()))
            throw new ConflictException("email already exists");

        var emp = Employee.builder()
                .name(req.getName())
                .email(req.getEmail())
                .department(req.getDepartment())
                .joiningDate(req.getJoiningDate())
                .hr(Boolean.TRUE.equals(req.getHr()))
                .annualAllocationDays(req.getAnnualAllocationDays())
                .build();
        return employeeRepository.save(emp);
    }

    public Employee getOrThrow(Long id){
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("employee not found: " + id));
    }
}
