package com.example.CargoAssign.Controller;


import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CargoAssign.Model.Driver;
import com.example.CargoAssign.Model.DriverStatus;
import com.example.CargoAssign.Model.DriverView;
import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.repo.DriverRepository;
import com.example.CargoAssign.repo.DriverViewRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverViewRepository repository;
    private final DriverRepository driverRepository;
    

    public DriverController(DriverViewRepository repository,DriverRepository driverRepository) {
        this.repository = repository;
        this.driverRepository = driverRepository;
       
    }
    
    @GetMapping
    public List<Map<String, Object>> getAllDrivers() {
        return driverRepository.findAll().stream()
                .map(this::toDriverItem)
                .collect(Collectors.toList());
    }

    @GetMapping("/status/{status}")
    public List<Map<String, Object>> getByStatus(@PathVariable String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        return driverRepository.findAll().stream()
                .filter(d -> {
                    DriverStatus ds = d.getDriverStatus();
                    String current = ds != null ? ds.name() : "OFFLINE";
                    return current.equals(normalized);
                })
                .map(this::toDriverItem)
                .collect(Collectors.toList());
    }

    @GetMapping("/user/status")
    public ResponseEntity<?> getCurrentUserStatus(HttpSession session) {
        User sessionUser = (User) session.getAttribute("LOGGED_USER");
        if (sessionUser == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }

        Driver driver = driverRepository.findByUser_Id(sessionUser.getId()).orElse(null);
        if (driver == null) {
            return ResponseEntity.status(404).body("Driver profile not found");
        }

        DriverStatus status = driver.getDriverStatus();
        return ResponseEntity.ok(Map.of(
                "status", status != null ? status.name() : "OFFLINE"
        ));
    }
    
    @PostMapping("/user/status")
    public ResponseEntity<?> updateStatus(
            @RequestBody Map<String, String> body,
            HttpSession session) {
    	
    	User sessionUser = (User) session.getAttribute("LOGGED_USER");
    	if (sessionUser == null) {
    	    return ResponseEntity.status(401).body("Not logged in");
    	}
    	Long userId = sessionUser.getId();

        String status = body.get("status");

        if (status == null) {
            return ResponseEntity.badRequest().body("Status required");
        }

        Driver driver = driverRepository.findByUser_Id(userId).orElse(null);
        if (driver == null) {
            return ResponseEntity.status(404).body("Driver profile not found");
        }

        try {
            driver.setDriverStatus(DriverStatus.valueOf(status.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid status");
        }

        driverRepository.save(driver);

        return ResponseEntity.ok("Status Updated");
    }

    private Map<String, Object> toDriverItem(Driver driver) {
        User user = driver.getUser();
        DriverStatus ds = driver.getDriverStatus();
        String status = ds != null ? ds.name() : "OFFLINE";
        Map<String, Object> item = new HashMap<>();
        item.put("userId", user != null ? user.getId() : null);
        item.put("name", user != null && user.getName() != null ? user.getName() : "");
        item.put("phone", user != null && user.getMobile() != null ? user.getMobile() : "");
        item.put("photo", user != null && user.getProfile_photo() != null ? user.getProfile_photo() : "");
        item.put("location", driver.getLocation() != null ? driver.getLocation() : "");
        item.put("vehicleType", driver.getVehicleType() != null ? driver.getVehicleType() : "");
        item.put("experienceYears", driver.getExperienceYears());
        item.put("status", status);
        return item;
    }
}
