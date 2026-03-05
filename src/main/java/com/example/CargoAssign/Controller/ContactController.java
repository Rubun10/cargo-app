package com.example.CargoAssign.Controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CargoAssign.Model.ContactMessage;
import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.repo.ContactMessageRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactMessageRepository contactMessageRepository;

    public ContactController(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @PostMapping
    public ResponseEntity<?> submitContact(@RequestBody Map<String, String> payload,
                                           HttpSession session) {
        User user = (User) session.getAttribute("LOGGED_USER");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Not logged in"));
        }

        String name = safeTrim(payload.get("name"));
        String email = safeTrim(payload.get("email"));
        String phone = safeTrim(payload.get("phone"));
        String message = safeTrim(payload.get("message"));

        if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Name, email and message are required"));
        }
        if (name.length() > 120) {
            return ResponseEntity.badRequest().body(Map.of("message", "Name is too long"));
        }
        if (email.length() > 180) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is too long"));
        }
        if (phone.length() > 30) {
            return ResponseEntity.badRequest().body(Map.of("message", "Phone number is too long"));
        }

        ContactMessage contactMessage = new ContactMessage();
        contactMessage.setUser(user);
        contactMessage.setName(name);
        contactMessage.setEmail(email);
        contactMessage.setPhone(phone.isEmpty() ? null : phone);
        contactMessage.setMessage(message);

        contactMessageRepository.save(contactMessage);

        return ResponseEntity.ok(Map.of("message", "Your request has been submitted"));
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
