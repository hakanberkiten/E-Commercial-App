// ReviewRepository.java
package com.example.e_commerce.repository;

import java.util.List;

import com.example.e_commerce.entity.Review;
import com.example.e_commerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductProductId(Long productId);
    void deleteByProductProductId(Long productId);

    // Ürün için ortalama puanı hesaplama
    @Query("SELECT AVG(r.reviewPoint) FROM Review r WHERE r.product.productId = :productId")
    Double calculateAverageRating(@Param("productId") Long productId);
    
    // Ürün için toplam değerlendirme sayısını hesaplama
    @Query("SELECT COUNT(r.reviewId) FROM Review r WHERE r.product.productId = :productId")
    Integer countReviewsByProduct(@Param("productId") Long productId);
    
    boolean existsByUserUserIdAndProductProductId(Long userId, Long productId);

    void deleteAllByUser(User user);
}
