// OrdersRepository.java
package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Orders;
import com.example.e_commerce.entity.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    // Add this method to your OrdersRepository interface

    @Query("SELECT DISTINCT o FROM Orders o JOIN o.items i WHERE i.product.seller.userId = :sellerId")
    List<Orders> findBySellerId(@Param("sellerId") Long sellerId);

    void deleteAllByUser(User user);

    List<Orders> findByUserOrderByOrderDateDesc(User user);

    @Query("SELECT o FROM Orders o LEFT JOIN FETCH o.items WHERE o.orderId = :id")
    Optional<Orders> findWithItemsById(@Param("id") Long id);
}
