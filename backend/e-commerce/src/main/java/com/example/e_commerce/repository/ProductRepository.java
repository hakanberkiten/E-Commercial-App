package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryCategoryId(Long categoryId);
}
