package com.example.CargoAssign.repo;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CargoAssign.Model.Driver;
import com.example.CargoAssign.Model.User;

public interface DriverRepository  extends JpaRepository<Driver, Long>{

	boolean existsByUser(User sessionUser);

Optional<Driver> findByUser_Id(Long userId);

	Optional<Driver> findByUser(User user);
}
