package com.example.CargoAssign.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.dto.LoginRequest;
import com.example.CargoAssign.dto.LoginResponse;
import com.example.CargoAssign.repo.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {

        if (request.getEmail() == null || request.getPassword() == null) {
            throw new RuntimeException("Email and password required");
        }

        Optional<User> optionalUser =
                userRepository.findByEmail(request.getEmail().trim());

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new RuntimeException("Account inactive");
        }

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getRole()
        );
    }
}
