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
public class LoadHistoryController {

    private final PostLoadRepo postLoadRepo;

    public LoadHistoryController(PostLoadRepo postLoadRepo) {
        this.postLoadRepo = postLoadRepo;
    }

    /**
     * Get completed load history for the logged-in user.
     * - SHIPPER: completed loads posted by the shipper
     * - DRIVER: completed loads assigned to the driver
     */
    @GetMapping("/load-history")
    public ResponseEntity<List<PostLoad>> getLoadHistory(HttpSession session) {
        User sessionUser = (User) session.getAttribute("LOGGED_USER");

        if (sessionUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<PostLoad> loads;
        if ("DRIVER".equalsIgnoreCase(sessionUser.getRole())) {
            loads = postLoadRepo.findByDriverIDAndStatus(
                    sessionUser.getId(),
                    LoadStatus.COMPLETED
            );
        } else {
            loads = postLoadRepo.findByUserIdAndStatus(
                    sessionUser.getId(),
                    LoadStatus.COMPLETED
            );
        }

        return ResponseEntity.ok(loads);
    }
}

