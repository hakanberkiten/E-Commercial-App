// ReviewRepository.java
package com.example.e_commerce.repository;
import java.util.List;

import com.example.e_commerce.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // ReviewRepository interface'ine ekleyin
List<Review> findByProductProductId(Long productId);
}
