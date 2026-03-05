package com.example.CargoAssign.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.CargoAssign.Model.Driver;
import com.example.CargoAssign.Model.DriverVerification;
import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.Service.ImageKitService;
import com.example.CargoAssign.repo.DriverRepository;
import com.example.CargoAssign.repo.DriverVerificationRepo;
import com.example.CargoAssign.repo.UserRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class DriverVerificationController {

    @Autowired
    private DriverVerificationRepo repository;

    @Autowired
    private ImageKitService imageKitService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    @PostMapping(
            value = "/driver-verification",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> submitVerification(

            @RequestParam String aadharName,
            @RequestParam String aadharNumber,

            @RequestParam String state,
            @RequestParam String district,
            @RequestParam String city,

            @RequestParam String licenseNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate licenseValidFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate licenseValidTo,

            @RequestParam String vehicleType,
            @RequestParam int experienceYears,
            @RequestParam String truckNumber,
            @RequestParam String rcNumber,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate insuranceFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate insuranceTo,

            @RequestParam MultipartFile licenseFront,
            @RequestParam MultipartFile licenseBack,
            @RequestParam MultipartFile rcFront,
            @RequestParam MultipartFile rcBack,
            @RequestParam MultipartFile userPhoto,

            HttpSession session

    ) {

       
        //  LOGIN CHECK
      
        User sessionUser = (User) session.getAttribute("LOGGED_USER");

        if (sessionUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Login required"));
        }
        
        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User not found. Please login again."));
        }

       
        //  ROLE CHECK
       
        if (!"DRIVER".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Only drivers can verify"));
        }

        
        //  DUPLICATE CHECKS
   
        if (repository.existsByUser_Id(user.getId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Verification already submitted"));
        }

        if (driverRepository.findByUser_Id(user.getId()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Driver profile already exists"));
        }

        if (repository.existsByAadharNumber(aadharNumber)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Aadhar number already exists"));
        }
       
        //  UPLOAD IMAGES (ONLY AFTER VALIDATION)
        
        String licenseFrontUrl;
        String licenseBackUrl;
        String rcFrontUrl;
        String rcBackUrl;
        String photoUrl;
        try {
            licenseFrontUrl = imageKitService.uploadImage(licenseFront);
            licenseBackUrl = imageKitService.uploadImage(licenseBack);
            rcFrontUrl = imageKitService.uploadImage(rcFront);
            rcBackUrl = imageKitService.uploadImage(rcBack);
            photoUrl = imageKitService.uploadImage(userPhoto);
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }

        
        //  SAVE DRIVER VERIFICATION
       
        DriverVerification dv = new DriverVerification();
        dv.setUser(user);
        dv.setAadharName(aadharName);
        dv.setAadharNumber(aadharNumber);
        dv.setState(state);
        dv.setDistrict(district);
        dv.setCity(city);
        dv.setLocation(state + ", " + district + ", " + city);
        dv.setLicenseNumber(licenseNumber);
        dv.setLicenseValidFrom(licenseValidFrom);
        dv.setLicenseValidTo(licenseValidTo);
        dv.setVehicleType(vehicleType);
        dv.setTruckNumber(truckNumber);
        dv.setRcNumber(rcNumber);
        dv.setInsuranceFrom(insuranceFrom);
        dv.setInsuranceTo(insuranceTo);
        dv.setLicenseFrontUrl(licenseFrontUrl);
        dv.setLicenseBackUrl(licenseBackUrl);
        dv.setRcFrontUrl(rcFrontUrl);
        dv.setRcBackUrl(rcBackUrl);
        dv.setUserPhoto(photoUrl);

        try {
            repository.save(dv);

            //  UPDATE USER
            user.setProfile_photo(photoUrl);
            user.setLocation(state + ", " + district + ", " + city);
            userRepository.save(user);

       
            // CREATE DRIVER ENTRY
        
            Driver driver = new Driver();
            driver.setUser(user);
            driver.setExperienceYears(experienceYears);
            driver.setLocation(state + ", " + district + ", " + city);
            driver.setVehicleType(vehicleType);
        
            driverRepository.save(driver);
            session.setAttribute("LOGGED_USER", user);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Duplicate or invalid verification data"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Verification submission failed"));
        }
   
        // 8 SUCCESS RESPONSE
        return ResponseEntity.ok(
                Map.of("success", true, "message", "Verification submitted successfully")
        );
    }
}
