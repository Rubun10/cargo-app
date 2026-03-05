package com.example.CargoAssign.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaymentInvoiceItemDTO {

    private final String invoiceId;
    private final String tripId;
    private final LocalDate date;
    private final Double amount;
    private final String status;
    private final String cargoStatus;
    private final String loadType;
    private final String pickupLocation;
    private final String dropLocation;
    private final Double weightKg;
    private final String driverName;
    private final Long driverId;
    private final String shipperName;
    private final String additionalDetails;
    private final String cargoImage;
    private final Boolean paymentDone;
    private final LocalDateTime paymentDate;
    private final Integer driverReviewRating;
    private final String driverReviewComment;
    private final LocalDateTime driverReviewedAt;
    private final Boolean canReview;

    public PaymentInvoiceItemDTO(String invoiceId,
                                 String tripId,
                                 LocalDate date,
                                 Double amount,
                                 String status,
                                 String cargoStatus,
                                 String loadType,
                                 String pickupLocation,
                                 String dropLocation,
                                 Double weightKg,
                                 String driverName,
                                 Long driverId,
                                 String shipperName,
                                 String additionalDetails,
                                 String cargoImage,
                                 Boolean paymentDone,
                                 LocalDateTime paymentDate,
                                 Integer driverReviewRating,
                                 String driverReviewComment,
                                 LocalDateTime driverReviewedAt,
                                 Boolean canReview) {
        this.invoiceId = invoiceId;
        this.tripId = tripId;
        this.date = date;
        this.amount = amount;
        this.status = status;
        this.cargoStatus = cargoStatus;
        this.loadType = loadType;
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.weightKg = weightKg;
        this.driverName = driverName;
        this.driverId = driverId;
        this.shipperName = shipperName;
        this.additionalDetails = additionalDetails;
        this.cargoImage = cargoImage;
        this.paymentDone = paymentDone;
        this.paymentDate = paymentDate;
        this.driverReviewRating = driverReviewRating;
        this.driverReviewComment = driverReviewComment;
        this.driverReviewedAt = driverReviewedAt;
        this.canReview = canReview;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getTripId() {
        return tripId;
    }

    public LocalDate getDate() {
        return date;
    }

    public Double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getCargoStatus() {
        return cargoStatus;
    }

    public String getLoadType() {
        return loadType;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public String getDropLocation() {
        return dropLocation;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public String getDriverName() {
        return driverName;
    }

    public Long getDriverId() {
        return driverId;
    }

    public String getShipperName() {
        return shipperName;
    }

    public String getAdditionalDetails() {
        return additionalDetails;
    }

    public String getCargoImage() {
        return cargoImage;
    }

    public Boolean getPaymentDone() {
        return paymentDone;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public Integer getDriverReviewRating() {
        return driverReviewRating;
    }

    public String getDriverReviewComment() {
        return driverReviewComment;
    }

    public LocalDateTime getDriverReviewedAt() {
        return driverReviewedAt;
    }

    public Boolean getCanReview() {
        return canReview;
    }
}
