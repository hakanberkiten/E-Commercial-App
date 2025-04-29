// ReviewController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.entity.Review;
import com.example.e_commerce.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/save")
    public ResponseEntity<?> saveReview(@RequestBody Review review) {
        try {
            Review savedReview = reviewService.saveReview(review);
            return ResponseEntity.ok(savedReview);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/product/{productId}")
    public List<Review> getReviewsByProduct(@PathVariable Long productId) {
        return reviewService.getReviewsByProduct(productId);
    }

    @GetMapping("/product/{productId}/rating")
    public Map<String, Object> getProductRating(@PathVariable Long productId) {
        Double averageRating = reviewService.calculateAverageRating(productId);
        Integer reviewCount = reviewService.countReviewsByProduct(productId);
        
        return Map.of(
            "averageRating", averageRating,
            "reviewCount", reviewCount
        );
    }
}
