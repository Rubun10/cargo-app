	package com.example.CargoAssign.repo;

import com.example.CargoAssign.Model.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverVerificationRepo extends JpaRepository<DriverVerification,Long> {

	boolean existsByAadharNumber(String aadharNumber);
	
	boolean existsByUser(User user);

boolean existsByUser_Id(Long userId);

boolean existsByUser_IdAndStatus(Long userId, String status);

Optional<DriverVerification> findByUser_Id(Long userId);

	List<DriverVerification> findByStatus(String status);

	Optional<DriverVerification> findByUser_IdAndStatus(Long userId, String status);
	
}
