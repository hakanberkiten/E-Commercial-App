package com.example.e_commerce.controller;

import com.example.e_commerce.entity.Category;
import com.example.e_commerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin("http://localhost:4200")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/save")
    public Category saveCategory(@RequestBody Category category) {
        return categoryService.saveCategory(category);
    }
    // @GetMapping("/all") 
    // public List<Category> all() { 
    //   return categoryService.getAll(); 
    // }
    @GetMapping("/all")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public Category getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }
}
