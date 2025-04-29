package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryCategoryId(Long categoryId);
    
    // Ürün ismine göre arama yapan metot
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchProducts(@Param("query") String query);

      // Fiyat aralığına göre filtreleme
      @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
      List<Product> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
      
      // Hem kategori hem de fiyat aralığına göre filtreleme
      @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND p.price BETWEEN :minPrice AND :maxPrice")
      List<Product> findByCategoryAndPriceRange(
          @Param("categoryId") Long categoryId, 
          @Param("minPrice") Double minPrice, 
          @Param("maxPrice") Double maxPrice
      );
}
