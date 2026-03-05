package com.example.CargoAssign.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CargoAssign.dto.NotificationDTO;
import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.Service.NotificationService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            HttpSession session) {

        User loggedUser =
                (User) session.getAttribute("LOGGED_USER");

        if (loggedUser == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(
                notificationService.getUserNotifications(loggedUser)
        );
    }


    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            HttpSession session) {

        User loggedUser =
                (User) session.getAttribute("LOGGED_USER");

        if (loggedUser == null) {
            return ResponseEntity.status(401).build();
        }

        notificationService.markAsRead(id, loggedUser);

        return ResponseEntity.ok().build();
    }
}
