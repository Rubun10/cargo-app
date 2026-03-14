package com.example.CargoAssign.Service;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.dto.ForgotPasswordRequest;
import com.example.CargoAssign.dto.LoginRequest;
import com.example.CargoAssign.dto.LoginResponse;
import com.example.CargoAssign.dto.ResetPasswordRequest;
import com.example.CargoAssign.repo.UserRepository;

@Service
public class AuthService {

    private static class OtpInfo {
        String otp;
        long expiry;

        OtpInfo(String otp, long expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }

    private static final Map<String, OtpInfo> otpStorage = new ConcurrentHashMap<>();
    private static final long OTP_EXPIRY_MS = 5 * 60 * 1000; // 5 minutes
    private static final Random random = new Random();

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

    public String generateOtp(ForgotPasswordRequest request) {
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        email = email.trim().toLowerCase();

        if (userRepository.findByEmail(email).isEmpty()) {
            throw new RuntimeException("Email not found");
        }

        String otp = String.format("%06d", random.nextInt(900000) + 100000);
        long expiry = System.currentTimeMillis() + OTP_EXPIRY_MS;
        otpStorage.put(email, new OtpInfo(otp, expiry));

        return otp;
    }

    public void verifyAndReset(ResetPasswordRequest request) {
        String email = request.getEmail();
        String otp = request.getOtp();
        String newPassword = request.getNewPassword();

        if (email == null || email.trim().isEmpty() || otp == null || newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("Email, OTP, and new password required");
        }
        email = email.trim().toLowerCase();

        OtpInfo info = otpStorage.get(email);
        if (info == null || System.currentTimeMillis() > info.expiry || !info.otp.equals(otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Clear OTP
        otpStorage.remove(email);

        // Update password
        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = optUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
