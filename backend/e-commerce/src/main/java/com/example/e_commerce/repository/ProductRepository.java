package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryCategoryId(Long categoryId);
    List<Product> findBySellerUserId(Long id);
    // Ürün ismine göre arama yapan metot
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchProducts(@Param("query") String query);

    // Price sorting queries
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price ASC")
    List<Product> findByPriceRangeOrderByPriceAsc(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price DESC")
    List<Product> findByPriceRangeOrderByPriceDesc(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    // Rating sorting queries
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.productRate ASC")
    List<Product> findByPriceRangeOrderByRatingAsc(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.productRate DESC")
    List<Product> findByPriceRangeOrderByRatingDesc(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    // Category and price with sorting
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price ASC")
    List<Product> findByCategoryAndPriceRangeOrderByPriceAsc(
        @Param("categoryId") Long categoryId, 
        @Param("minPrice") Double minPrice, 
        @Param("maxPrice") Double maxPrice);

    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price DESC")
    List<Product> findByCategoryAndPriceRangeOrderByPriceDesc(
        @Param("categoryId") Long categoryId, 
        @Param("minPrice") Double minPrice, 
        @Param("maxPrice") Double maxPrice);

    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.productRate ASC")
    List<Product> findByCategoryAndPriceRangeOrderByRatingAsc(
        @Param("categoryId") Long categoryId, 
        @Param("minPrice") Double minPrice, 
        @Param("maxPrice") Double maxPrice);
        List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
        List<Product> findByCategoryCategoryIdAndPriceBetween(Long categoryId, Double minPrice, Double maxPrice);

    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.productRate DESC")
    List<Product> findByCategoryAndPriceRangeOrderByRatingDesc(
        @Param("categoryId") Long categoryId, 
        @Param("minPrice") Double minPrice, 
        @Param("maxPrice") Double maxPrice);
}
