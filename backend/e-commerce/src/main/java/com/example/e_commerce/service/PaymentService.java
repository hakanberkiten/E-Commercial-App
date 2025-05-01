// PaymentService.java
package com.example.e_commerce.service;

import com.example.e_commerce.dto.CardDto;
import com.example.e_commerce.dto.PaymentRequest;
import com.example.e_commerce.entity.Payment;

import java.util.List;
import java.util.Map;

public interface PaymentService {
    Payment savePayment(Payment payment);
    List<Payment> getAllPayments();
    Payment getPaymentById(Long id);
    void deletePayment(Long id);
    String createCustomerIfNotExists(Long userId);
    String addCardToCustomer(Long userId, CardDto cardDto);
    List<Map<String, Object>> getUserCards(Long userId);
    Payment processPayment(PaymentRequest paymentRequest);
    List<Payment> getPaymentsByUserId(Long userId);
}
