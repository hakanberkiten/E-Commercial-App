// ReviewServiceImpl.java
package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.Review;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.repository.ReviewRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;

    @Override
    public Review saveReview(Review review) {
        User u = userRepo.findById(review.getUser().getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        Product p = productRepo.findById(review.getProduct().getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));
        review.setUser(u);
        review.setProduct(p);
        return reviewRepo.save(review);
    }

    @Override
    public List<Review> getAllReviews() { return reviewRepo.findAll(); }

    @Override
    public Review getReviewById(Long id) {
        return reviewRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Review not found"));
    }

    @Override
    public void deleteReview(Long id) {
        reviewRepo.deleteById(id);
    }
}
