package com.example.CargoAssign.Controller;

import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.Service.CargoAvailableService;
import com.example.CargoAssign.dto.PostLoadDTO;
import com.example.CargoAssign.repo.DriverVerificationRepo;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cargo-available")
public class CargoAvailableController {

    @Autowired
    private CargoAvailableService cargoService;

    @Autowired
    private DriverVerificationRepo driverVerificationRepo;

    // =============================
    // GET All Loads
    // =============================
    @GetMapping
    public ResponseEntity<List<PostLoadDTO>> getAllLoads() {

        List<PostLoadDTO> loads =
                cargoService.getAllAvailableLoads();

        if (loads.isEmpty())
            return ResponseEntity.noContent().build();

        return ResponseEntity.ok(loads);
    }

    // =============================
    // GET Load By Id
    // =============================
    @GetMapping("/{id}")
    public ResponseEntity<PostLoadDTO> getLoadById(@PathVariable String id) {

        return ResponseEntity.ok(
                cargoService.getLoadById(id)
        );
    }

    // =============================
    // Driver Request
    // =============================
    @PostMapping("/{loadId}/request")
    public ResponseEntity<?> driverRequest(
            @PathVariable String loadId,
            HttpSession session) {
        User driver =
                (User) session.getAttribute("LOGGED_USER");
        if (driver == null)
            return ResponseEntity.status(401).build();
        
        if (!"DRIVER".equalsIgnoreCase(driver.getRole())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only driver can confirm the load"));
        }
        if (!driverVerificationRepo.existsByUser_IdAndStatus(driver.getId(), "APPROVED")) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "kycRequired", true,
                            "redirectUrl", "/driver-verification?kycRequired=cargo-confirm",
                            "message", "Complete KYC to confirm cargo"
                    ));
        }

        cargoService.driverRequest(normalizeLoadId(loadId), driver);

        return ResponseEntity.ok().build();
    }

    // =============================
    // Shipper Approve
    // =============================
    @PostMapping("/{loadId}/approve")
    public ResponseEntity<?> shipperApprove(
            @PathVariable String loadId,
            HttpSession session) {

        User shipper =
                (User) session.getAttribute("LOGGED_USER");

        if (shipper == null)
            return ResponseEntity.status(401).build();
        
        if (!"SHIPPER".equalsIgnoreCase(shipper.getRole())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only shipper can approve cargo"));
        }

        try {
            cargoService.shipperApprove(normalizeLoadId(loadId), shipper);
        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }

        return ResponseEntity.ok().build();
    }
    
    // =============================
    // Shipper Cancel Approval/Request
    // =============================
    @PostMapping("/{loadId}/cancel")
    public ResponseEntity<?> shipperCancel(
            @PathVariable String loadId,
            HttpSession session) {

        User shipper =
                (User) session.getAttribute("LOGGED_USER");

        if (shipper == null)
            return ResponseEntity.status(401).build();

        if (!"SHIPPER".equalsIgnoreCase(shipper.getRole())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only shipper can cancel cargo approval"));
        }

        try {
            cargoService.shipperCancelApproval(normalizeLoadId(loadId), shipper);
        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }

        return ResponseEntity.ok().build();
    }

    private String normalizeLoadId(String loadId) {
        if (loadId == null) return null;
        String normalized = loadId.trim();
        if (normalized.isEmpty()) return normalized;
        if (normalized.toUpperCase().startsWith("LD-")) {
            return "LD-" + normalized.substring(3);
        }
        if (normalized.chars().allMatch(Character::isDigit)) {
            return "LD-" + normalized;
        }
        return normalized;
    }
}
