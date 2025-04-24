package com.example.e_commerce.repository;


import com.example.e_commerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    /**
     * Bir siparişe ait kalemleri getirir
     */
    List<OrderItem> findByOrderId(Integer orderId);
}
