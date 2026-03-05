package com.example.CargoAssign.Model;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Immutable 
@Table(name = "driver_list_view")
public class DriverView {

    @Id
    @Column(name = "user_id")
    private Long userId;

    private String name;
    private String phone;
    private String photo;
    private String location;

    @Column(name = "vehicle_type")
    private String vehicleType;

    private String status;
}
