// PaymentController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.dto.CardDto;
import com.example.e_commerce.dto.PaymentRequest;
import com.example.e_commerce.entity.Payment;
import com.example.e_commerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/save")
    public Payment save(@RequestBody Payment p) {
        return paymentService.savePayment(p);
    }

    @GetMapping("/all")
    public List<Payment> all() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/{id}")
    public Payment byId(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        paymentService.deletePayment(id);
    }
    
    // New endpoints for Stripe integration
    
    @PostMapping("/stripe/customers/{userId}")
    public ResponseEntity<String> createStripeCustomer(@PathVariable Long userId) {
        String customerId = paymentService.createCustomerIfNotExists(userId);
        return ResponseEntity.ok(customerId);
    }
    
    @PostMapping("/stripe/customers/{userId}/cards")
    public ResponseEntity<String> addCard(@PathVariable Long userId, @RequestBody CardDto cardDto) {
        String paymentMethodId = paymentService.addCardToCustomer(userId, cardDto);
        return ResponseEntity.ok(paymentMethodId);
    }
    
    @GetMapping("/stripe/customers/{userId}/cards")
    public ResponseEntity<List<Map<String, Object>>> getCards(@PathVariable Long userId) {
        List<Map<String, Object>> cards = paymentService.getUserCards(userId);
        return ResponseEntity.ok(cards);
    }
    
    @PostMapping("/stripe/customers/{userId}/payment-methods")
    public ResponseEntity<String> addPaymentMethod(
            @PathVariable Long userId, 
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        
        // Log the incoming request
        System.out.println("Adding payment method for user: " + userId);
        System.out.println("Payment method ID: " + payload.get("paymentMethodId"));
        
        try {
            String result = paymentService.attachPaymentMethod(userId, payload.get("paymentMethodId"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error adding payment method: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding payment method: " + e.getMessage());
        }
    }
    
    @PostMapping("/process")
    public ResponseEntity<Payment> processPayment(@RequestBody PaymentRequest paymentRequest) {
        Payment payment = paymentService.processPayment(paymentRequest);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getUserPayments(@PathVariable Long userId) {
        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }
}
