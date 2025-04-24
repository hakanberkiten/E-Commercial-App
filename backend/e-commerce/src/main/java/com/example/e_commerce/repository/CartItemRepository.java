package com.example.e_commerce.repository;


import com.example.e_commerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    /**
     * Bir sepete ait kalemleri getirir
     */
    List<CartItem> findByCartId(Integer cartId);
}
