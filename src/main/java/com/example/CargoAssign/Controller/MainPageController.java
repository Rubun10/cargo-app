package com.example.CargoAssign.Controller;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.repo.DriverVerificationRepo;
import com.example.CargoAssign.repo.ShipperVerificationRepo;


import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class MainPageController {
	
	@Autowired 
	private DriverVerificationRepo driverVerificationRepo;
	
	
	@Autowired
	private ShipperVerificationRepo shipperVerificationRepo;
	
	
	@PostMapping("/kyc/status")
	public ResponseEntity<?> kycStatus(HttpSession session) {

	    User sessionUser = (User) session.getAttribute("LOGGED_USER");

	    if (sessionUser == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }

	    Long userId = sessionUser.getId();
	    boolean completed = false;

	    if ("DRIVER".equals(sessionUser.getRole())) {
	        completed = driverVerificationRepo.existsByUser_IdAndStatus(userId, "APPROVED");
	    } else if ("SHIPPER".equals(sessionUser.getRole())) {
	        completed = shipperVerificationRepo.existsByUser_IdAndStatus(userId, "APPROVED");
	    }

	    return ResponseEntity.ok(Map.of("kycCompleted", completed));
	}
	@GetMapping("/kyc/status")
	public ResponseEntity<?> checkKyc(HttpSession session) {

	    User sessionUser = (User) session.getAttribute("LOGGED_USER");

	    if (sessionUser == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }

	    boolean completed = false;

	    if ("DRIVER".equals(sessionUser.getRole())) {
	        completed = driverVerificationRepo.existsByUser_IdAndStatus(sessionUser.getId(), "APPROVED");
	    }
	    else if ("SHIPPER".equals(sessionUser.getRole())) {
	        completed = shipperVerificationRepo.existsByUser_IdAndStatus(sessionUser.getId(), "APPROVED");
	    }

	    return ResponseEntity.ok(
	        Map.of("kycCompleted", completed)
	    );
	}	
	
	@GetMapping("/user/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        User sessionUser = (User) session.getAttribute("LOGGED_USER");

        if (sessionUser == null) {
            // return empty object or 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of());
        }

        String photoUrl = sessionUser.getProfile_photo();
        if (photoUrl == null) photoUrl = "";

        Map<String, Object> response = new HashMap<>();
        response.put("userId", sessionUser.getId());
        response.put("role", sessionUser.getRole() != null ? sessionUser.getRole() : "UNKNOWN");
        response.put("name", sessionUser.getName());
        response.put("profilePhoto", photoUrl);

        return ResponseEntity.ok(response);
    }
}
