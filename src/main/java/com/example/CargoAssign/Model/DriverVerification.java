package com.example.CargoAssign.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "driver_verification")
public class DriverVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ================= USER RELATION ================= */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;   // maps to users(id)

    /* ================= AADHAR ================= */

    @Column(name = "aadhar_name", nullable = false)
    private String aadharName;

    @Column(name = "aadhar_number", nullable = false, unique = true, length = 12)
    private String aadharNumber;

    /* ================= LOCATION ================= */

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String city;

    @Column(length = 300)
    private String location;

    /* ================= LICENSE ================= */

    @Column(name = "license_number", nullable = false, length = 50)
    private String licenseNumber;

    @Column(name = "license_valid_from", nullable = false)
    private LocalDate licenseValidFrom;

    @Column(name = "license_valid_to", nullable = false)
    private LocalDate licenseValidTo;

    // URL TEXT (S3 / GCP / Local path)
    @Column(name = "license_front_url", length = 500)
    private String licenseFrontUrl;

    @Column(name = "license_back_url", length = 500)
    private String licenseBackUrl;

    /* ================= VEHICLE ================= */
    @Column(name ="vehicle_type", nullable = false,length=20)
    private String vehicleType;
    @Column(name = "truck_number", nullable = false, length = 20)
    private String truckNumber;

    @Column(name = "rc_number", nullable = false, length = 50)
    private String rcNumber;

    @Column(name = "rc_front_url", length = 500)
    private String rcFrontUrl;

    @Column(name = "rc_back_url", length = 500)
    private String rcBackUrl;

    @Column(name = "insurance_from")
    private LocalDate insuranceFrom;

    @Column(name = "insurance_to")
    private LocalDate insuranceTo;

    /* ================= DRIVER PHOTO ================= */

    @Column(name = "driver_photo_url", nullable = false, length = 500)
    private String UserPhoto;

    /* ================= STATUS ================= */

    @Column(length = 20)
    private String status = "PENDING"; 
    // PENDING | APPROVED | REJECTED

    /* ================= TIMESTAMPS ================= */

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
