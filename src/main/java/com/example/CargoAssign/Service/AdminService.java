package com.example.CargoAssign.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.CargoAssign.Model.Driver;
import com.example.CargoAssign.Model.DriverVerification;
import com.example.CargoAssign.Model.Notification;
import com.example.CargoAssign.Model.ShipperVerification;
import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.repo.DriverRepository;
import com.example.CargoAssign.repo.DriverVerificationRepo;
import com.example.CargoAssign.repo.NotificationRepository;
import com.example.CargoAssign.repo.ShipperVerificationRepo;
import com.example.CargoAssign.repo.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AdminService {

    @Autowired
    private DriverVerificationRepo driverVerificationRepo;

    @Autowired
    private ShipperVerificationRepo shipperVerificationRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Get all pending KYC submissions (both drivers and shippers)
     */
    public List<Map<String, Object>> getPendingVerifications() {
        List<Map<String, Object>> result = new ArrayList<>();

        // Get pending driver verifications
        List<DriverVerification> pendingDrivers = driverVerificationRepo.findByStatus("PENDING");
        for (DriverVerification dv : pendingDrivers) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", dv.getId());
            item.put("type", "DRIVER");
            item.put("userId", dv.getUser().getId());
            item.put("name", dv.getUser().getName());
            item.put("documentType", "Driving License");
            item.put("submittedAt", dv.getCreatedAt());
            item.put("aadharName", dv.getAadharName());
            item.put("aadharNumber", dv.getAadharNumber());
            item.put("licenseNumber", dv.getLicenseNumber());
            item.put("licenseValidFrom", dv.getLicenseValidFrom());
            item.put("licenseValidTo", dv.getLicenseValidTo());
            item.put("licenseFrontUrl", dv.getLicenseFrontUrl());
            item.put("licenseBackUrl", dv.getLicenseBackUrl());
            item.put("truckNumber", dv.getTruckNumber());
            item.put("rcNumber", dv.getRcNumber());
            item.put("rcFrontUrl", dv.getRcFrontUrl());
            item.put("rcBackUrl", dv.getRcBackUrl());
            item.put("vehicleType", dv.getVehicleType());
            item.put("driverPhotoUrl", dv.getUserPhoto());
            item.put("insuranceFrom", dv.getInsuranceFrom());
            item.put("insuranceTo", dv.getInsuranceTo());
            item.put("location", dv.getLocation());
            item.put("state", dv.getState());
            item.put("district", dv.getDistrict());
            item.put("city", dv.getCity());
            result.add(item);
        }

        // Get pending shipper verifications
        List<ShipperVerification> pendingShippers = shipperVerificationRepo.findByStatus("PENDING");
        for (ShipperVerification sv : pendingShippers) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", sv.getId());
            item.put("type", "SHIPPER");
            item.put("userId", sv.getUser().getId());
            item.put("name", sv.getUser().getName());
            item.put("documentType", "Business Documents");
            item.put("submittedAt", sv.getCreatedAt());
            item.put("aadharName", sv.getAadharName());
            item.put("aadharNumber", sv.getAadharNumber());
            item.put("companyName", sv.getCompanyName());
            item.put("gstin", sv.getGstin());
            item.put("businessEmail", sv.getBusinessEmail());
            item.put("businessMobile", sv.getBusinessMobile());
            item.put("aadharImage", sv.getAadharImage());
            item.put("userPhoto", sv.getUserPhoto());
            item.put("address", sv.getAddress());
            item.put("city", sv.getCity());
            item.put("state", sv.getState());
            item.put("pincode", sv.getPincode());
            result.add(item);
        }

        return result;
    }

    /**
     * Get KYC statistics
     */
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();

        long pendingDrivers = driverVerificationRepo.findByStatus("PENDING").size();
        long approvedDrivers = driverVerificationRepo.findByStatus("APPROVED").size();
        long rejectedDrivers = driverVerificationRepo.findByStatus("REJECTED").size();

        long pendingShippers = shipperVerificationRepo.findByStatus("PENDING").size();
        long approvedShippers = shipperVerificationRepo.findByStatus("APPROVED").size();
        long rejectedShippers = shipperVerificationRepo.findByStatus("REJECTED").size();

        stats.put("pendingDrivers", pendingDrivers);
        stats.put("approvedDrivers", approvedDrivers);
        stats.put("rejectedDrivers", rejectedDrivers);
        stats.put("pendingShippers", pendingShippers);
        stats.put("approvedShippers", approvedShippers);
        stats.put("rejectedShippers", rejectedShippers);
        stats.put("totalPending", pendingDrivers + pendingShippers);
        stats.put("totalVerified", approvedDrivers + approvedShippers);
        stats.put("totalRejected", rejectedDrivers + rejectedShippers);

        return stats;
    }

    /**
     * Create a notification for user
     */
    private void createNotification(User receiver, String type, String message) {
        // Create a system notification 
        Notification notification = new Notification();
        notification.setReceiver(receiver);
        // Note: sender is optional - will be null in database (allowed)
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notificationRepository.save(notification);
    }

    /**
     * Approve a KYC submission
     */
    @Transactional
    public Map<String, Object> approveKYC(String type, Long id) {
        Map<String, Object> result = new HashMap<>();

        if ("DRIVER".equals(type)) {
            DriverVerification dv = driverVerificationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Driver verification not found"));

            dv.setStatus("APPROVED");
            driverVerificationRepo.save(dv);

            // Update user status
            User user = dv.getUser();
            user.setStatus("ACTIVE");
            userRepository.save(user);

            // Send notification
            createNotification(user, "KYC_APPROVED", "Your driver KYC has been approved. You can now start accepting loads.");

            result.put("success", true);
            result.put("message", "Driver KYC approved successfully");
            result.put("userName", user.getName());
        } else if ("SHIPPER".equals(type)) {
            ShipperVerification sv = shipperVerificationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Shipper verification not found"));

            sv.setStatus("APPROVED");
            shipperVerificationRepo.save(sv);

            // Update user status
            User user = sv.getUser();
            user.setStatus("ACTIVE");
            userRepository.save(user);

            // Send notification
            createNotification(user, "KYC_APPROVED", "Your shipper KYC has been approved. You can now start posting loads.");

            result.put("success", true);
            result.put("message", "Shipper KYC approved successfully");
            result.put("userName", user.getName());
        } else {
            throw new RuntimeException("Invalid verification type");
        }

        return result;
    }

    /**
     * Reject and delete a KYC submission
     */
    @Transactional
    public Map<String, Object> rejectKYC(String type, Long id) {
        Map<String, Object> result = new HashMap<>();

        if ("DRIVER".equals(type)) {
            DriverVerification dv = driverVerificationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Driver verification not found"));

            User user = dv.getUser();

            // Send notification before deleting
            createNotification(user, "KYC_REJECTED", "Your driver KYC has been rejected. Please submit correct documents for verification.");

            // Delete driver record if exists
            if (driverRepository.existsByUser(user)) {
                driverRepository.findByUser_Id(user.getId()).ifPresent(driver -> driverRepository.delete(driver));
            }

            // Delete verification record
            driverVerificationRepo.delete(dv);

            result.put("success", true);
            result.put("message", "Driver KYC rejected and data deleted");
            result.put("userName", user.getName());
        } else if ("SHIPPER".equals(type)) {
            ShipperVerification sv = shipperVerificationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Shipper verification not found"));

            User user = sv.getUser();

            // Send notification before deleting
            createNotification(user, "KYC_REJECTED", "Your shipper KYC has been rejected. Please submit correct documents for verification.");

            // Delete verification record
            shipperVerificationRepo.delete(sv);

            result.put("success", true);
            result.put("message", "Shipper KYC rejected and data deleted");
            result.put("userName", user.getName());
        } else {
            throw new RuntimeException("Invalid verification type");
        }

        return result;
    }
}
