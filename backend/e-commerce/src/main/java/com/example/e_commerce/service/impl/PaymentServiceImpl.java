// PaymentServiceImpl.java
package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.Payment;
import com.example.e_commerce.repository.PaymentRepository;
import com.example.e_commerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository payRepo;

    @Override
    public Payment savePayment(Payment payment) {
        return payRepo.save(payment);
    }

    @Override
    public List<Payment> getAllPayments() { return payRepo.findAll(); }

    @Override
    public Payment getPaymentById(Long id) {
        return payRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Override
    public void deletePayment(Long id) {
        payRepo.deleteById(id);
    }
}
