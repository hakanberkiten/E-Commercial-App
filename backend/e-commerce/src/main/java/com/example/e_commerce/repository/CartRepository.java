// CartRepository.java
package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Cart;
import com.example.e_commerce.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserUserId(Long userId);

    void deleteByUser(User user);

}
