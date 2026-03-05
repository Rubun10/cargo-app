package com.example.CargoAssign.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CargoAssign.Model.PostLoad;
import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.Service.ReportsService;

import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/api")
public class ReportsController {
	
	@Autowired 
	private ReportsService reportsService;
	
	@GetMapping("/reports")
	public ResponseEntity<?> getAllLoads(HttpSession session) {
		User sessionUser = (User) session.getAttribute("LOGGED_USER");

		if (sessionUser == null) {
			return ResponseEntity.status(401).build();
		}

	    List<PostLoad> loads = reportsService.getMyReportLoads(sessionUser);

	    return ResponseEntity.ok(loads);
	}

}
