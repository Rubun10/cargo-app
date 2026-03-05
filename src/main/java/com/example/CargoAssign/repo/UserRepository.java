package com.example.CargoAssign.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CargoAssign.Model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);
	
	Optional<User> findByMobile(String mobile);
	
	boolean existsByMobileAndIdNot(String value, Long id);

	boolean existsByEmailAndIdNot(String value, Long id);

	Optional<User> findByEmailIgnoreCase(String email);

	boolean existsByMobile(String mobile);

}

