// ReviewService.java
package com.example.e_commerce.service;

import com.example.e_commerce.entity.Review;

import java.util.List;

public interface ReviewService {
    Review saveReview(Review review);
    List<Review> getAllReviews();
    Review getReviewById(Long id);
    void deleteReview(Long id);
List<Review> getReviewsByProduct(Long productId);
}
