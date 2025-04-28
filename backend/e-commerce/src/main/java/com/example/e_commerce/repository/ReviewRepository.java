// ReviewRepository.java
package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {}
