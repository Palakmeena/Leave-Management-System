package com.example.Leave_management_system.controller;


import com.example.Leave_management_system.dto.AuthRequest;
import com.example.Leave_management_system.dto.AuthResponse;
import com.example.Leave_management_system.model.Employee;
import com.example.Leave_management_system.model.Role;
import com.example.Leave_management_system.Repository.EmployeeRepository;
import com.example.Leave_management_system.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public String register(@RequestBody Employee emp){
        if(employeeRepository.existsByEmail(emp.getEmail())){
            return "Email already exists!";
        }
        emp.setRole(emp.getRole() != null ? emp.getRole() : Role.EMPLOYEE);

        // Hash password before saving
        emp.setPassword(passwordEncoder.encode(emp.getPassword()));

        employeeRepository.save(emp);
        return "Registered successfully!";
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        Employee emp = employeeRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.getPassword(), emp.getPassword()))
            throw new RuntimeException("Invalid credentials");

        // ✅ Pass Employee (UserDetails) instead of String email
        String token = jwtUtil.generateToken(emp);

    return ResponseEntity.ok(new AuthResponse(token, emp.isHr() ? "HR" : "EMPLOYEE"));
    }
}
