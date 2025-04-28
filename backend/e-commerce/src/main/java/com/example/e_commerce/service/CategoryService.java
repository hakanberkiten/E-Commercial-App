package com.example.e_commerce.service;

import com.example.e_commerce.entity.Category;

import java.util.List;

public interface CategoryService {
    Category saveCategory(Category category);
    List<Category> getAllCategories();
    Category getCategoryById(Long id);
    void deleteCategory(Long id);
}
