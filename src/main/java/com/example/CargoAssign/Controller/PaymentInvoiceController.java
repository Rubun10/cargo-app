package com.example.CargoAssign.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.Service.PaymentInvoiceService;
import com.example.CargoAssign.dto.DriverReviewRequest;
import com.example.CargoAssign.dto.PaymentInvoiceResponseDTO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaymentInvoiceController {

    private final PaymentInvoiceService paymentInvoiceService;

    public PaymentInvoiceController(PaymentInvoiceService paymentInvoiceService) {
        this.paymentInvoiceService = paymentInvoiceService;
    }

    @GetMapping("/payment-invoices")
    public ResponseEntity<?> getMyPaymentInvoices(HttpSession session) {
        User sessionUser = (User) session.getAttribute("LOGGED_USER");

        if (sessionUser == null) {
            return ResponseEntity.status(401).build();
        }

        PaymentInvoiceResponseDTO response = paymentInvoiceService.getMyPaymentInvoices(sessionUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payment-invoices/{tripId}/review-driver")
    public ResponseEntity<?> reviewDriver(@PathVariable String tripId,
                                          @Valid @RequestBody DriverReviewRequest request,
                                          HttpSession session) {
        User sessionUser = (User) session.getAttribute("LOGGED_USER");
        if (sessionUser == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            paymentInvoiceService.submitDriverReview(sessionUser, tripId, request);
            return ResponseEntity.ok(Map.of("message", "Driver review submitted"));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError err : ex.getBindingResult().getFieldErrors()) {
            errors.put(err.getField(), err.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(Map.of(
                "message", "Validation failed",
                "errors", errors
        ));
    }
}
