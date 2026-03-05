package com.example.CargoAssign.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.Service.AuthService;
import com.example.CargoAssign.dto.LoginRequest;
import com.example.CargoAssign.dto.LoginResponse;
import com.example.CargoAssign.dto.RegisterRequest;
import com.example.CargoAssign.repo.UserRepository;
import com.example.CargoAssign.security.JwtService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        String name = request.getName() == null ? null : request.getName().trim();
        String email = request.getEmail() == null ? null : request.getEmail().trim();
        String mobile = request.getMobile() == null ? null : request.getMobile().trim();
        String role = request.getRole() == null ? null : request.getRole().trim();
        String password = request.getPassword();

        if (name == null || name.isEmpty()
                || email == null || email.isEmpty()
                || mobile == null || mobile.isEmpty()
                || role == null || role.isEmpty()
                || password == null || password.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "All fields are required"));
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email already exists"));
        }

        if (userRepository.findByMobile(mobile).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Mobile already exists"));
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setRole(role);
        user.setStatus("ACTIVE");
        user.setPassword(passwordEncoder.encode(password));

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email or mobile already exists"));
        }

        return ResponseEntity.ok(
                Map.of("message", "Registration successful")
        );
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpSession session) {

        LoginResponse response;
        try {
            response = authService.login(request);
        } catch (RuntimeException ex) {
            String message = ex.getMessage() != null ? ex.getMessage() : "Login failed";
            if (message.toLowerCase().contains("required")) {
                return ResponseEntity.badRequest().body(Map.of("message", message));
            }
            if (message.toLowerCase().contains("inactive")) {
                return ResponseEntity.status(403).body(Map.of("message", message));
            }
            return ResponseEntity.status(401).body(Map.of("message", message));
        }

        // Get full user from DB
        User user = userRepository.findById(response.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Store in session
        session.setAttribute("LOGGED_USER", user);

        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(
                Map.of(
                        "id", response.getId(),
                        "name", response.getName(),
                        "role", response.getRole(),
                        "token", token
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {

        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(
                Map.of("success", true, "message", "Logged out successfully")
        );
    }
}

