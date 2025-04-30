package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.CartItem;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.CartItemRepository;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.repository.ReviewRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.ProductService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    
    @Override
    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.searchProducts(query);
    }
    
    @Override
    public Product saveProduct(Product product) {
        // Get the actual seller from database since JSON only contains userId
        User seller = userRepository.findById(product.getSeller().getUserId())
            .orElseThrow(() -> new RuntimeException("Seller not found"));

        // Verify the user has seller role
        if (seller.getRole().getRoleId() != 2) {
            throw new IllegalArgumentException("Only users with SELLER role can add products");
        }

        // Set the full seller object to ensure proper relationship
        product.setSeller(seller);

        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }
    
    @Override
    public List<Product> getProductsBySellerId(Long sellerId) {
        return productRepository.findBySellerUserId(sellerId);
    }

    @Override
    public Product createProduct(Product product) {
        return saveProduct(product);
    }

    @Override
    public Product updateProduct(Product product) {
        // Check if product exists
        productRepository.findById(product.getProductId())
            .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + product.getProductId()));
        
        return saveProduct(product);
    }
    
    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        
        // Delete related reviews first
        reviewRepository.deleteByProductProductId(id);
        
        // Delete cart items containing this product
        cartItemRepository.deleteCartItemsByProductId(id);
        
        // For additional safety, manually find and delete any remaining cart items
        List<CartItem> remainingCartItems = cartItemRepository.findByProductProductId(id);
        if (!remainingCartItems.isEmpty()) {
            System.out.println("Found " + remainingCartItems.size() + " remaining cart items. Deleting manually...");
            cartItemRepository.deleteAll(remainingCartItems);
        }
        
        // Finally delete the product
        productRepository.delete(product);
        
        System.out.println("Product successfully deleted with ID: " + id);
    }

    @Override
    public List<Product> getByCategory(Long categoryId) {
        return productRepository.findByCategoryCategoryId(categoryId);
    }

    @Override
    public List<Product> getByPriceRange(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }
    
    @Override
    public List<Product> getByCategoryAndPriceRange(Long categoryId, Double minPrice, Double maxPrice) {
        return productRepository.findByCategoryCategoryIdAndPriceBetween(categoryId, minPrice, maxPrice);
    }
    
    @Override
    public List<Product> getFilteredAndSortedProducts(Long categoryId, Double minPrice, Double maxPrice, String sortBy) {
        if (categoryId == null || categoryId <= 0) {
            // No category filter, only price range and sorting
            switch (sortBy) {
                case "priceAsc":
                    return productRepository.findByPriceRangeOrderByPriceAsc(minPrice, maxPrice);
                case "priceDesc":
                    return productRepository.findByPriceRangeOrderByPriceDesc(minPrice, maxPrice);
                case "ratingAsc":
                    return productRepository.findByPriceRangeOrderByRatingAsc(minPrice, maxPrice);
                case "ratingDesc":
                    return productRepository.findByPriceRangeOrderByRatingDesc(minPrice, maxPrice);
                default:
                    return getByPriceRange(minPrice, maxPrice);
            }
        } else {
            // With category filter, price range, and sorting
            switch (sortBy) {
                case "priceAsc":
                    return productRepository.findByCategoryAndPriceRangeOrderByPriceAsc(categoryId, minPrice, maxPrice);
                case "priceDesc":
                    return productRepository.findByCategoryAndPriceRangeOrderByPriceDesc(categoryId, minPrice, maxPrice);
                case "ratingAsc":
                    return productRepository.findByCategoryAndPriceRangeOrderByRatingAsc(categoryId, minPrice, maxPrice);
                case "ratingDesc":
                    return productRepository.findByCategoryAndPriceRangeOrderByRatingDesc(categoryId, minPrice, maxPrice);
                default:
                    return getByCategoryAndPriceRange(categoryId, minPrice, maxPrice);
            }
        }
    }
}
