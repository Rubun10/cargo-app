package com.example.CargoAssign.Service;

import com.example.CargoAssign.Model.LoadStatus;
import com.example.CargoAssign.Model.PostLoad;
import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.Model.DriverStatus;
import com.example.CargoAssign.dto.PostLoadDTO;
import com.example.CargoAssign.repo.DriverRepository;
import com.example.CargoAssign.repo.PostLoadRepo;
import com.example.CargoAssign.repo.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CargoAvailableService {

    @Autowired
    private PostLoadRepo postLoadRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private DriverRepository driverRepository;

    // =============================
    // Get All Available Loads
    // =============================
    public List<PostLoadDTO> getAllAvailableLoads() {

        List<PostLoad> loads =
                postLoadRepo.findByStatus(LoadStatus.AVAILABLE);

        return loads.stream()
                .map(PostLoadDTO::new)
                .collect(Collectors.toList());
    }

    // =============================
    // Get Load By Id
    // =============================
    public PostLoadDTO getLoadById(String id) {

        PostLoad load = postLoadRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Load not found"));

        return new PostLoadDTO(load);
    }

    // =============================
    // Driver Request Load
    // =============================
    public void driverRequest(String loadId, User driver) {

        PostLoad load = postLoadRepo.findById(loadId)
                .orElseThrow(() -> new RuntimeException("Load not found"));

        if (!load.getStatus().equals(LoadStatus.AVAILABLE)) {
            throw new RuntimeException("Load not available");
        }

        load.setDriverID(driver.getId());
        load.setDriverName(driver.getName());
        load.setStatus(LoadStatus.DRIVER_REQUESTED);

        postLoadRepo.save(load);

        // Notify shipper
        notificationService.createNotification(
                load.getUser(),
                driver,
                "LOAD",
                extractLoadEntityId(loadId),
                "DRIVER_REQUEST",
                "Driver requested your load."
        );
    }

    // =============================
    // Shipper Approve Load
    // =============================
    @Transactional
    public void shipperApprove(String loadId, User shipper) {

        PostLoad load = postLoadRepo.findById(loadId)
                .orElseThrow(() -> new RuntimeException("Load not found"));

        // Only shipper can approve
        if (!load.getUser().getId().equals(shipper.getId())) {
            throw new RuntimeException("Only shipper can approve");
        }

        if (load.getStatus() == null) {
            throw new RuntimeException("Invalid load status in database");
        }

        if (load.getStatus().equals(LoadStatus.SHIPPER_CONFIRMED)) {
            return;
        }

        if (!load.getStatus().equals(LoadStatus.DRIVER_REQUESTED)) {
            throw new RuntimeException("Load is not in driver requested status");
        }

        load.setStatus(LoadStatus.SHIPPER_CONFIRMED);
        postLoadRepo.save(load);

        // Notify driver
        User driver = userRepository.findById(load.getDriverID())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        Long loadEntityId = extractLoadEntityId(loadId);

        // Remove old pending approve notification for shipper
        notificationService.deleteDriverRequestNotification(shipper, loadEntityId);

        notificationService.createNotification(
                driver,
                shipper,
                "LOAD",
                loadEntityId,
                "LOAD_APPROVED",
                "Shipper accepted your request. You can pick the cargo."
        );

        // Notify shipper with confirmation details
        notificationService.createNotification(
                shipper,
                shipper,
                "LOAD",
                loadEntityId,
                "APPROVED_CONFIRMATION",
                "You have approved this cargo to driver " +
                        (driver.getName() != null ? driver.getName() : "-") +
                        " (ID: " + driver.getId() + ")."
        );

        // Mark approved driver as WORKING.
        driverRepository.findByUser_Id(driver.getId()).ifPresent(d -> {
            d.setDriverStatus(DriverStatus.WORKING);
            driverRepository.save(d);
        });
    }
    
    // =============================
    // Shipper Cancel Approval/Request
    // =============================
    @Transactional
    public void shipperCancelApproval(String loadId, User shipper) {

        PostLoad load = postLoadRepo.findById(loadId)
                .orElseThrow(() -> new RuntimeException("Load not found"));

        if (!load.getUser().getId().equals(shipper.getId())) {
            throw new RuntimeException("Only shipper can cancel");
        }

        if (load.getStatus() == null) {
            throw new RuntimeException("Invalid load status in database");
        }

        if (!load.getStatus().equals(LoadStatus.DRIVER_REQUESTED)
                && !load.getStatus().equals(LoadStatus.SHIPPER_CONFIRMED)) {
            throw new RuntimeException("Load cannot be cancelled from current status");
        }

        if (load.getDriverID() == null) {
            throw new RuntimeException("Driver not assigned");
        }

        User driver = userRepository.findById(load.getDriverID())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        load.setStatus(LoadStatus.AVAILABLE);
        load.setDriverID(null);
        load.setDriverName(null);
        postLoadRepo.save(load);

        driverRepository.findByUser_Id(driver.getId()).ifPresent(d -> {
            d.setDriverStatus(DriverStatus.FREE);
            driverRepository.save(d);
        });

        Long loadEntityId = extractLoadEntityId(loadId);
        notificationService.deleteDriverRequestNotification(shipper, loadEntityId);

        notificationService.createNotification(
                driver,
                shipper,
                "LOAD",
                loadEntityId,
                "APPROVAL_CANCELLED",
                "Your cargo approval request was cancelled by the shipper."
        );
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
