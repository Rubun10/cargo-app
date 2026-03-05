package com.example.CargoAssign.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.dto.UpdateRequest;
import com.example.CargoAssign.repo.UserRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;
     
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @GetMapping("/profile")
    public String profilePage() {
		return "profilePage";	
    }
    
    @GetMapping("/user/profile")
    public ResponseEntity<Map<String, Object>> getProfile(HttpSession session) {

        User sessionUser = (User) session.getAttribute("LOGGED_USER");

        if (sessionUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("mobile", user.getMobile());
        response.put("location", user.getLocation());
       
        String photo = user.getProfile_photo();

        if (photo != null) {
            photo = photo.trim().replaceAll("\\s+", "");
        }

        response.put("profilePhoto", photo);


        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/update")
    public ResponseEntity<String> updateProfile(
            @RequestBody UpdateRequest req,
            HttpSession session) {

        User sessionUser = (User) session.getAttribute("LOGGED_USER");

        if (sessionUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
                );

        switch (req.getField()) {

            case "email":
                if (userRepository.existsByEmailAndIdNot(req.getValue(), user.getId())) {
                    return ResponseEntity.badRequest().body("Email already exists");
                }
                user.setEmail(req.getValue());
                break;

            case "mobile":
                if (userRepository.existsByMobileAndIdNot(req.getValue(), user.getId())) {
                    return ResponseEntity.badRequest().body("Mobile number already exists");
                }
                user.setMobile(req.getValue());
                break;

            case "location":
                user.setLocation(req.getValue());
                break;

            case "password":
                user.setPassword(passwordEncoder.encode(req.getValue()));
                break;

            default:
                return ResponseEntity.badRequest().body("Invalid field");
        }

        userRepository.save(user);

        return ResponseEntity.ok("Updated successfully");
    }
    
}
