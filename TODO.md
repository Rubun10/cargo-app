# Forgot Password Implementation Plan

## Overview
Implement OTP-based forgot password: email input → generate/display OTP → verify OTP + new password.

## Steps (to be checked off as completed)

### 1. [x] Create DTOs
   - ForgotPasswordRequest.java
   - ResetPasswordRequest.java

### 2. [ ] Update AuthService.java
   - Add in-memory OTP storage with expiry
   - Add generateOtp(), verifyOtp(), resetPassword()

### 3. [ ] Update AuthController.java
   - Add POST /api/auth/forgot-password
   - Add POST /api/auth/reset-password

### 4. [ ] Update forgotPassword.html
   - Add email form + JS
   - Add OTP display + reset form

### 5. [ ] Test
   - mvn compile
   - mvn spring-boot:run
   - Test flow with existing user email

### 6. [ ] Complete
