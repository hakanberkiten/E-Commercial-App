package com.example.e_commerce.repository;


import com.example.e_commerce.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Integer> {

    /**
     * Kullanıcının sepetini getirir (tek sepet varsayımıyla)
     */
    Optional<ShoppingCart> findByUserId(Integer userId);
}
