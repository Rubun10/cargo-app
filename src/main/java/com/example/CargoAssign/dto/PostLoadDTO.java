package com.example.CargoAssign.dto;

import com.example.CargoAssign.Model.LoadStatus;
import com.example.CargoAssign.Model.PostLoad;

import lombok.Getter;

import java.time.LocalDate;
@Getter
public class PostLoadDTO {
	
    private String loadId;
    private String loadType;
    private String shipperName;
    private String phone;
    private String pickupLocation;
    private Double pickupLat;
    private Double pickupLng;
    private String dropLocation;
    private Double weightKg;
    private String weightUnit;
    private Double price;
    private LoadStatus status;
    private String cargoImage;
    private LocalDate expectedDate;
    private String additionalDetails;

    public PostLoadDTO(PostLoad load) {
    	
        this.loadId = load.getLoadId();
        this.loadType = load.getLoadType();
        this.shipperName = load.getUser().getName();
        this.phone = load.getUser().getMobile();
        this.pickupLocation = load.getPickupLocation();
        this.pickupLat = load.getPickupLat();
        this.pickupLng = load.getPickupLng();
        this.dropLocation = load.getDropLocation();
        this.weightKg = load.getWeightKg();
        this.weightUnit = load.getWeightUnit();
        this.price = load.getPrice();
        this.status = load.getStatus();
        this.cargoImage = load.getCargoImage();
        this.expectedDate = load.getExpectedDate();
        this.additionalDetails = load.getAdditionalDetails();
        

    }

    // Getters
    
}
