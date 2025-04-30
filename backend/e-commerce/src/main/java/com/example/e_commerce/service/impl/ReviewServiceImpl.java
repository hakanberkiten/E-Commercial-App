// ReviewServiceImpl.java
package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.Review;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.repository.ReviewRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Review saveReview(Review review) {
        // Kullanıcı var mı kontrol et
        User user = userRepository.findById(review.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        review.setUser(user);
        
        // Ürün var mı kontrol et
        Product product = productRepository.findById(review.getProduct().getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        review.setProduct(product);
        
        // Kullanıcı bu ürünü daha önce değerlendirmiş mi kontrol et
        if (reviewRepository.existsByUserUserIdAndProductProductId(user.getUserId(), product.getProductId())) {
            throw new IllegalStateException("You have already reviewed this product");
        }
        
        // Review puanı 1-5 arasında mı kontrol et
        if (review.getReviewPoint() < 1 || review.getReviewPoint() > 5) {
            throw new IllegalArgumentException("Review point must be between 1 and 5");
        }
        
        // İncelemeyi kaydet
        Review savedReview = reviewRepository.save(review);
        
        // Ürün puanını güncelle
        updateProductRating(product.getProductId());
        
        return savedReview;
    }

    @Override
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Override
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        Long productId = review.getProduct().getProductId();
        
        // İncelemeyi sil
        reviewRepository.deleteById(id);
        
        // Ürün puanını güncelle
        updateProductRating(productId);
    }

    @Override
    public List<Review> getReviewsByProduct(Long productId) {
        List<Review> reviews = reviewRepository.findByProductProductId(productId);
        
        // Filter out reviews from inactive users
        return reviews.stream()
                .filter(review -> review.getUser() == null || review.getUser().getActive())
                .collect(Collectors.toList());
    }

    @Override
    public Double calculateAverageRating(Long productId) {
        // Get reviews only from active users
        List<Review> activeReviews = reviewRepository.findByProductProductId(productId).stream()
                .filter(review -> review.getUser() == null || review.getUser().getActive())
                .collect(Collectors.toList());
        
        if (activeReviews.isEmpty()) {
            return 0.0;
        }
        
        double sum = activeReviews.stream()
                .mapToDouble(Review::getReviewPoint)
                .sum();
        
        return sum / activeReviews.size();
    }

    @Override
    public Integer countReviewsByProduct(Long productId) {
        List<Review> activeReviews = reviewRepository.findByProductProductId(productId).stream()
                .filter(review -> review.getUser() == null || review.getUser().getActive())
                .collect(Collectors.toList());
        
        return activeReviews.size();
    }

    @Override
    @Transactional
    public void updateProductRating(Long productId) {
        Double averageRating = calculateAverageRating(productId);
        Integer reviewCount = countReviewsByProduct(productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Ürünün ortalama puanı ve değerlendirme sayısını güncelle
        product.setProductRate(averageRating);
        product.setReviewCount(reviewCount);
        
        productRepository.save(product);
    }
}
