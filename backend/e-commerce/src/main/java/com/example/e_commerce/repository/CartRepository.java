// CartRepository.java
package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {}
