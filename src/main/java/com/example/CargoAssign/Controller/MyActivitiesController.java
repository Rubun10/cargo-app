package com.example.CargoAssign.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CargoAssign.Model.LoadStatus;
import com.example.CargoAssign.Model.PostLoad;
import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.repo.PostLoadRepo;


import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class MyActivitiesController {

    private final PostLoadRepo postLoadRepo;

    public MyActivitiesController(PostLoadRepo postLoadRepo) {
        this.postLoadRepo = postLoadRepo;
    }

    @GetMapping("/my-activities")
    public ResponseEntity<List<PostLoad>> getMyActivities(HttpSession session) {

        User sessionUser = (User) session.getAttribute("LOGGED_USER");

        if (sessionUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<LoadStatus> allowedStatuses = List.of(
                LoadStatus.AVAILABLE,
                LoadStatus.PENDING,
                LoadStatus.CONFIRM,
                LoadStatus.ACTIVE,
                LoadStatus.DRIVER_REQUESTED
        );

        List<PostLoad> loads =
                postLoadRepo.findByUserIdAndStatusIn(
                        sessionUser.getId(),
                        allowedStatuses
                );

        return ResponseEntity.ok(loads);
    }
    
    @GetMapping("/active-trips")
    public ResponseEntity<List<PostLoad>> getMyActiveTrips(HttpSession session){
    	User sessionUser = (User) session.getAttribute("LOGGED_USER");
    	
    	
    	if(sessionUser == null)
    	{
    		return ResponseEntity.status(401).build();
    	}
    	
    	
    	List<LoadStatus> allowedStatuses =List.of( 
    			LoadStatus.ACTIVE,
    			LoadStatus.SHIPPER_CONFIRMED);
    	
    	
    	List<PostLoad> loads =
                postLoadRepo.findByDriverIDAndStatusIn(
                        sessionUser.getId(),
                        allowedStatuses
                );
    	
		return ResponseEntity.ok(loads);
    }
    @GetMapping("/my-trips")
    public ResponseEntity<List<PostLoad>> getMyTrips(HttpSession session){
    	User sessionUser = (User) session.getAttribute("LOGGED_USER");
    	
    	
    	if(sessionUser == null)
    	{
    		return ResponseEntity.status(401).build();
    	}
    	
    	
    	List<LoadStatus> allowedStatuses =List.of( 
    			LoadStatus.PENDING,
    			LoadStatus.CONFIRM,
    			LoadStatus.DRIVER_REQUESTED,
    			LoadStatus.COMPLETED
    			);

    	List<PostLoad> loads =
    			postLoadRepo.findByDriverIDAndStatusIn(
    					sessionUser.getId(),
    					allowedStatuses
    					);
    	return ResponseEntity.ok(loads);
    }

}
