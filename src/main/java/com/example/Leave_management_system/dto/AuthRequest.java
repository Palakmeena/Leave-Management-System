package com.example.Leave_management_system.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
