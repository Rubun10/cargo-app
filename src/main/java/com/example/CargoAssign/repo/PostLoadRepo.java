package com.example.CargoAssign.repo;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CargoAssign.Model.LoadStatus;
import com.example.CargoAssign.Model.PostLoad;

public interface PostLoadRepo extends JpaRepository<PostLoad, String>{

	List<PostLoad> findByStatus(LoadStatus status);

	List<PostLoad> findByUserId(Long id);

	List<PostLoad> findByUserIdAndStatusIn(Long id, List<LoadStatus> allowedStatuses);
	
	List<PostLoad> findByDriverIDAndStatusIn(Long driverID, List<LoadStatus> allowedStatuses);
	
	List<PostLoad> findByDriverIDAndStatus(Long driverID, LoadStatus status);
	
	List<PostLoad> findByDriverID(Long driverID);

	List<PostLoad> findByUserIdAndStatus(Long id, LoadStatus completed);


}
