package com.example.CargoAssign.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String role;
    private String name;
    private String mobile;
    private String email;
    private String password;
}
