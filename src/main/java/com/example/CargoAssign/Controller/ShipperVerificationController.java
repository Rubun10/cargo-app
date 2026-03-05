package com.example.CargoAssign.Controller;

import java.io.IOException;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.CargoAssign.Model.ShipperVerification;
import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.Service.ImageKitService;
import com.example.CargoAssign.repo.ShipperVerificationRepo;
import com.example.CargoAssign.repo.UserRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class ShipperVerificationController {

	
	@Autowired 
	private ShipperVerificationRepo shipperVerificationrepo;
	
	 @Autowired
	 private UserRepository userRepository;
	 
	 @Autowired
	 private ImageKitService imageKitService;
	 
	 
	 
	@PostMapping(
		value = "/shipper-verification",
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	public ResponseEntity<?> submitVerification(
			@RequestParam String aadharName,
			@RequestParam String aadharNumber,
			@RequestParam MultipartFile aadharImage,
			@RequestParam MultipartFile userPhoto,
			
			//business
			@RequestParam String companyName,
			@RequestParam String gstin,
			@RequestParam String businessEmail,
			@RequestParam String businessMobile,
			
			//address
			@RequestParam String address,
			@RequestParam String city,
			@RequestParam String state,			
			@RequestParam int pincode,
			
			HttpSession session
			
			) {
		User sessionUser = (User) session.getAttribute("LOGGED_USER");

		if (sessionUser == null) {
		    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		User user = userRepository.findById(sessionUser.getId()).orElse(null);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("success", false, "message", "User not found. Please login again."));
		}

		if (shipperVerificationrepo.existsByUser_Id(user.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                    		"success", false,
                    		"message", "Verification already submitted"));
        }


		if (shipperVerificationrepo.existsByAadharNumber(aadharNumber)) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                    		"success",false,
                    		"message","Aadhar already submitted"));
        }
        
		if (!"SHIPPER".equalsIgnoreCase(user.getRole())) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
	            Map.of("success", false, "message", "Only shippers can verify")
	        );
	    }
		
        try {
            ShipperVerification sv = new ShipperVerification();

            sv.setUser(user);
            sv.setAadharName(aadharName);
            sv.setAadharNumber(aadharNumber);
            sv.setAadharImage(imageKitService.uploadImage(aadharImage));

            sv.setCompanyName(companyName);
            sv.setGstin(gstin);
            sv.setBusinessEmail(businessEmail);
            sv.setBusinessMobile(businessMobile);

            sv.setAddress(address);
            sv.setCity(city);
            sv.setState(state);
            sv.setPincode(pincode);

            String photoUrl = imageKitService.uploadImage(userPhoto);
            sv.setUserPhoto(photoUrl);

            user.setLocation(address + " , "+city + " , " + state + " , " + pincode );
            user.setProfile_photo(photoUrl);
            userRepository.save(user);
            session.setAttribute("LOGGED_USER", user);

            shipperVerificationrepo.save(sv);
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("success", false, "message", ex.getMessage()));
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Duplicate or invalid verification data"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Verification submission failed"));
        }
        
        return ResponseEntity.ok(
        	    Map.of(
        	        "success", true,
        	        "message", "Verification submitted successfully"
        	    )
        	);
	}

}
