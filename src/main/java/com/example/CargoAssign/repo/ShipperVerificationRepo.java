package com.example.CargoAssign.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CargoAssign.Model.ShipperVerification;
import com.example.CargoAssign.Model.User;

public interface ShipperVerificationRepo extends JpaRepository<ShipperVerification, Long> {

	boolean existsByAadharNumber(String aadharNumber);

	boolean existsByUser(User user);

boolean existsByUser_Id(Long userId);

boolean existsByUser_IdAndStatus(Long userId, String status);
	
Optional<ShipperVerification> findByUser_Id(Long userId);

	List<ShipperVerification> findByStatus(String status);

	Optional<ShipperVerification> findByUser_IdAndStatus(Long userId, String status);


}
