package com.example.CargoAssign.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.CargoAssign.Model.LoadStatus;
import com.example.CargoAssign.Model.PostLoad;
import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.Service.ImageKitService;
import com.example.CargoAssign.repo.PostLoadRepo;
import com.example.CargoAssign.repo.ShipperVerificationRepo;


import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/api/loads-posting")
public class PostLoadController {

    @Autowired
    private PostLoadRepo postLoadRepo;

    @Autowired
    private ImageKitService imageKitService;

    @Autowired
    private ShipperVerificationRepo shipperVerificationRepo;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createLoad(

            HttpSession session,   // ✅ inject session

            @RequestParam String loadType,
            @RequestParam Double weight,
            @RequestParam String weightUnit,
            @RequestParam Double price,

            @RequestParam String pickupAddress,
            @RequestParam Double pickupLat,
            @RequestParam Double pickupLng,

            @RequestParam String dropAddress,
            @RequestParam Double dropLat,
            @RequestParam Double dropLng,

            @RequestParam LocalDate deliveryDate,
            @RequestParam(required = false) String details,

            @RequestPart(required = false) MultipartFile images

    ) throws IOException {

        // ✅ AUTH CHECK
        User user = (User) session.getAttribute("LOGGED_USER");
        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Login required"));
        }
        if (!"SHIPPER".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Only shipper can post load"));
        }
        if (!shipperVerificationRepo.existsByUser_IdAndStatus(user.getId(), "APPROVED")) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "success", false,
                            "kycRequired", true,
                            "redirectUrl", "/shipper-verification?kycRequired=post-load",
                            "message", "Complete KYC to post load"
                    ));
        }

        PostLoad post = new PostLoad();

        post.setLoadId("LD-" + System.currentTimeMillis());
        post.setUser(user); // ✅ session user only
        
        post.setLoadType(loadType);
        post.setWeightKg(weight);
        post.setWeightUnit(weightUnit);
        post.setPrice(price);

        post.setPickupLocation(pickupAddress);
        post.setPickupLat(pickupLat);
        post.setPickupLng(pickupLng);

        post.setDropLocation(dropAddress);
        post.setDropLat(dropLat);
        post.setDropLng(dropLng);
        
        post.setStatus(LoadStatus.AVAILABLE);

        post.setExpectedDate(deliveryDate);
        post.setAdditionalDetails(details);

        if (images != null && !images.isEmpty()) {
            post.setCargoImage(imageKitService.uploadImage(images));
        }

        postLoadRepo.save(post);
        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "referenceId", post.getLoadId()
                )
        );
    }
}
