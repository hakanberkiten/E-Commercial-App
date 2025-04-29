package com.example.e_commerce.controller;

import com.example.e_commerce.entity.Product;
import com.example.e_commerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/save")
    public Product saveProduct(@RequestBody Product product) {
        return productService.saveProduct(product);
    }

    @GetMapping("/all")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }
    @GetMapping
    public List<Product> byCategory(@RequestParam Long categoryId) {
      return productService.getByCategory(categoryId);
    }
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String query) {
        return productService.searchProducts(query);
    }
    @GetMapping("/filter")
    public List<Product> filterProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "0.0") Double minPrice, 
            @RequestParam(required = false, defaultValue = "999999.99") Double maxPrice) {
        
        if (categoryId != null && categoryId > 0) {
            return productService.getByCategoryAndPriceRange(categoryId, minPrice, maxPrice);
        } else {
            return productService.getByPriceRange(minPrice, maxPrice);
        }
    }
}
