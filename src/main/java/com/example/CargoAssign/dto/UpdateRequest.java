package com.example.CargoAssign.dto;

import lombok.Data;

@Data
public class UpdateRequest {
	
	private Long userId;
    private String field;
    private String value;
    
    private String mobile;
    private String email;
	
}
