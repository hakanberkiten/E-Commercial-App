// PaymentService.java
package com.example.e_commerce.service;

import com.example.e_commerce.entity.Payment;

import java.util.List;

public interface PaymentService {
    Payment savePayment(Payment payment);
    List<Payment> getAllPayments();
    Payment getPaymentById(Long id);
    void deletePayment(Long id);
}
