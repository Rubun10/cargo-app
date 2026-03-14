package com.example.CargoAssign.dto;

import lombok.Data;

@Data
public class AdminResetPasswordRequest {
    private String email;
    private String newPassword;
}
