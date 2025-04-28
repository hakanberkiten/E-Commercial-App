// PaymentController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.entity.Payment;
import com.example.e_commerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<Payment> all() { return paymentService.getAllPayments(); }

    @GetMapping("/{id}")
    public Payment byId(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        paymentService.deletePayment(id);
    }
}
