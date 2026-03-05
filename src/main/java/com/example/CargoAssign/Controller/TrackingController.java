package com.example.CargoAssign.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CargoAssign.Model.LoadStatus;
import com.example.CargoAssign.Model.PostLoad;
import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.Model.DriverStatus;
import com.example.CargoAssign.Service.NotificationService;
import com.example.CargoAssign.repo.DriverRepository;
import com.example.CargoAssign.repo.PostLoadRepo;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    private final PostLoadRepo postLoadRepo;
    private final NotificationService notificationService;
    private final DriverRepository driverRepository;

    public TrackingController(PostLoadRepo postLoadRepo,
                              NotificationService notificationService,
                              DriverRepository driverRepository) {
        this.postLoadRepo = postLoadRepo;
        this.notificationService = notificationService;
        this.driverRepository = driverRepository;
    }

    @GetMapping("/cargos")
    public ResponseEntity<List<PostLoad>> getTrackingCargos(HttpSession session) {
        User user = (User) session.getAttribute("LOGGED_USER");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String role = user.getRole() != null ? user.getRole().toUpperCase() : "";
        if ("DRIVER".equals(role)) {
            List<PostLoad> loads = postLoadRepo.findByDriverIDAndStatusIn(
                    user.getId(),
                    List.of(
                            LoadStatus.DRIVER_REQUESTED,
                            LoadStatus.SHIPPER_CONFIRMED,
                            LoadStatus.ACTIVE,
                            LoadStatus.COMPLETED
                    )
            );
            return ResponseEntity.ok(loads);
        }

        if ("SHIPPER".equals(role)) {
            List<PostLoad> loads = postLoadRepo.findByUserIdAndStatusIn(
                    user.getId(),
                    List.of(
                            LoadStatus.AVAILABLE,
                            LoadStatus.DRIVER_REQUESTED,
                            LoadStatus.SHIPPER_CONFIRMED,
                            LoadStatus.ACTIVE,
                            LoadStatus.COMPLETED
                    )
            );
            return ResponseEntity.ok(loads);
        }

        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/cargos/{loadId}/status")
    public ResponseEntity<?> updateDriverCargoStatus(
            @PathVariable String loadId,
            @RequestBody Map<String, String> payload,
            HttpSession session) {

        User user = (User) session.getAttribute("LOGGED_USER");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!"DRIVER".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only driver can update cargo status"));
        }

        String statusRaw = payload != null ? payload.get("status") : null;
        if (statusRaw == null || statusRaw.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Status is required"));
        }

        LoadStatus targetStatus;
        try {
            targetStatus = LoadStatus.valueOf(statusRaw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid status"));
        }

        if (targetStatus != LoadStatus.ACTIVE && targetStatus != LoadStatus.COMPLETED) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Allowed statuses are ACTIVE and COMPLETED"));
        }

        String normalizedLoadId = normalizeLoadId(loadId);
        PostLoad load = postLoadRepo.findById(normalizedLoadId)
                .orElseThrow(() -> new RuntimeException("Load not found"));

        if (load.getDriverID() == null || !load.getDriverID().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You are not assigned to this cargo"));
        }

        LoadStatus currentStatus = load.getStatus();
        if (currentStatus == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Current status is invalid"));
        }

        if (targetStatus == LoadStatus.ACTIVE
                && currentStatus != LoadStatus.SHIPPER_CONFIRMED
                && currentStatus != LoadStatus.DRIVER_REQUESTED
                && currentStatus != LoadStatus.ACTIVE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Cargo can be set ACTIVE only after shipper confirmation"));
        }

        if (targetStatus == LoadStatus.COMPLETED
                && currentStatus != LoadStatus.ACTIVE
                && currentStatus != LoadStatus.COMPLETED) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Cargo can be marked COMPLETED only from ACTIVE"));
        }

        if (currentStatus != targetStatus) {
            load.setStatus(targetStatus);
            postLoadRepo.save(load);

            // Keep driver working state in sync with trip lifecycle.
            driverRepository.findByUser_Id(user.getId()).ifPresent(driver -> {
                if (targetStatus == LoadStatus.ACTIVE) {
                    driver.setDriverStatus(DriverStatus.WORKING);
                    driverRepository.save(driver);
                } else if (targetStatus == LoadStatus.COMPLETED) {
                    driver.setDriverStatus(DriverStatus.FREE);
                    driverRepository.save(driver);
                }
            });

            Long loadEntityId = extractLoadEntityId(normalizedLoadId);
            if (targetStatus == LoadStatus.COMPLETED) {
                notificationService.createNotification(
                        load.getUser(),
                        user,
                        "LOAD",
                        loadEntityId,
                        "REVIEW_REQUIRED",
                        "Delivery completed for cargo " + normalizedLoadId
                                + ". Please review the driver."
                );
            } else {
                notificationService.createNotification(
                        load.getUser(),
                        user,
                        "LOAD",
                        loadEntityId,
                        "STATUS_UPDATE",
                        "Driver " + user.getName() + " (ID: " + user.getId() + ") updated cargo "
                                + normalizedLoadId + " status to " + targetStatus + "."
                );
            }
        }

        return ResponseEntity.ok(Map.of("message", "Status updated"));
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

    private Long extractLoadEntityId(String loadId) {
        if (loadId == null || loadId.isBlank()) {
            throw new RuntimeException("Invalid load id");
        }
        String numeric = loadId.replace("LD-", "").trim();
        try {
            return Long.parseLong(numeric);
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Invalid load id format");
        }
    }
}
