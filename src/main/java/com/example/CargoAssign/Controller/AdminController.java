package com.example.CargoAssign.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.Service.AdminService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Check if user is admin
     */
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("LOGGED_USER");
        return user != null && "ADMIN".equals(user.getRole());
    }

    /**
     * Get pending KYC verifications
     */
    @GetMapping("/pending-verifications")
    public ResponseEntity<?> getPendingVerifications(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        try {
            List<Map<String, Object>> pending = adminService.getPendingVerifications();
            return ResponseEntity.ok(pending);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Get KYC statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        try {
            Map<String, Long> stats = adminService.getStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Approve a KYC submission
     */
    @PostMapping("/approve-kyc")
    public ResponseEntity<?> approveKYC(@RequestParam String type,
                                         @RequestParam Long id,
                                         HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        try {
            Map<String, Object> result = adminService.approveKYC(type, id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Reject and delete a KYC submission
     */
    @PostMapping("/reject-kyc")
    public ResponseEntity<?> rejectKYC(@RequestParam String type,
                                        @RequestParam Long id,
                                        HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        try {
            Map<String, Object> result = adminService.rejectKYC(type, id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
