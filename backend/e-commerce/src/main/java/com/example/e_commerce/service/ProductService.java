package com.example.e_commerce.service;

import com.example.e_commerce.entity.Product;
import java.util.List;

public interface ProductService {
    Product saveProduct(Product product);
    List<Product> getAllProducts();
    Product getProductById(Long id);
    void deleteProduct(Long id);
    List<Product> getByCategory(Long categoryId);
    List<Product> searchProducts(String query);
    
    // Yeni eklenen metodlar
    List<Product> getByPriceRange(Double minPrice, Double maxPrice);
    List<Product> getByCategoryAndPriceRange(Long categoryId, Double minPrice, Double maxPrice);
    List<Product> getProductsBySellerId(Long sellerId);
    Product createProduct(Product product);
    Product updateProduct(Product product);
    List<Product> getFilteredAndSortedProducts(Long categoryId, Double minPrice, Double maxPrice, String sortBy);
    List<Product> filterProducts(Long categoryId, Double minPrice, Double maxPrice, String sortBy, Boolean inStock);
}
