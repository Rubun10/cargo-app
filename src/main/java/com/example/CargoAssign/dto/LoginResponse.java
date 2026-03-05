package com.example.CargoAssign.dto;



public class LoginResponse {

    private Long id;
    private String name;
    private String role;

    public LoginResponse(Long id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}
