package com.example.e_commerce.repository;

import com.example.e_commerce.entity.CartItem;

import jakarta.transaction.Transactional;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartCartId(Long cartId);
    
    // Find cart items by product ID
    List<CartItem> findByProductProductId(Long productId);
    
    // Add this explicit delete query
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.product.productId = :productId")
    void deleteCartItemsByProductId(@Param("productId") Long productId);
    
    // The existing method may not be working as expected
    void deleteByProductProductId(Long productId);
}
