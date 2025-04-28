// CartItemRepository.java
package com.example.e_commerce.repository;

import com.example.e_commerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {}
