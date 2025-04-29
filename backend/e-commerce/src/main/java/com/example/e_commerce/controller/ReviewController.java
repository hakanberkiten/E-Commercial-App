// ReviewController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.entity.Review;
import com.example.e_commerce.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/save")
    public Review save(@RequestBody Review r) {
        return reviewService.saveReview(r);
    }

    @GetMapping("/all")
    public List<Review> all() { return reviewService.getAllReviews(); }

    @GetMapping("/{id}")
    public Review byId(@PathVariable Long id) {
        return reviewService.getReviewById(id);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }

    @GetMapping("/product/{productId}")
public List<Review> getReviewsByProduct(@PathVariable Long productId) {
    return reviewService.getReviewsByProduct(productId);
}
}
