// OrdersRepository.java
package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, Long> {}
