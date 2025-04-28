package com.example.e_commerce.service;

import com.example.e_commerce.entity.Product;

import java.util.List;

public interface ProductService {
    Product saveProduct(Product product);
    List<Product> getAllProducts();
    Product getProductById(Long id);
    void deleteProduct(Long id);
    List<Product> getByCategory(Long categoryId);

}
