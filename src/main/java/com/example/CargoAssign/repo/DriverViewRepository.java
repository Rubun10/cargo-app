package com.example.CargoAssign.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CargoAssign.Model.DriverView;

import java.util.List;

public interface DriverViewRepository extends JpaRepository<DriverView, Long> {

    List<DriverView> findByStatus(String status);


}
