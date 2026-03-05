package com.example.CargoAssign.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String email;
    private String password;

    // REQUIRED: default constructor
    public LoginRequest() {
    }
}
