package com.example.CargoAssign.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "post_load")
@Data
public class PostLoad {
	
	
    @Id
    private String loadId;   

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private String loadType;

    
    private String cargoImage;

    private Double weightKg;

    private String weightUnit;
    
    private Double price;
    
    private String pickupLocation;

    private Double pickupLat;

    private Double pickupLng;


    private String dropLocation;

  
    private Double dropLat;

    private Double dropLng;

    private LocalDate expectedDate;
    
    private String driverName;
    
    private Long driverID;

    @Convert(converter = LoadStatusConverter.class)
    private LoadStatus status;


    @Column(name = "additional_details", columnDefinition = "TEXT")
    private String additionalDetails;

    @Column(name = "payment_done")
    private Boolean paymentDone = false;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "driver_review_rating")
    private Integer driverReviewRating;

    @Column(name = "driver_review_comment", columnDefinition = "TEXT")
    private String driverReviewComment;

    @Column(name = "driver_reviewed_at")
    private LocalDateTime driverReviewedAt;
    
    

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* ---------------- Lifecycle hooks ---------------- */

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @JsonProperty("shipperName")
    public String getShipperName() {
        return user != null ? user.getName() : null;
    }

}


