// PaymentRepository.java
package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {}
