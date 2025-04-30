package com.example.e_commerce.controller;

import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.service.ProductService;
import com.example.e_commerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final UserService userService; // Assuming you have a UserService to fetch user details

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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, Principal principal) {
        try {
            // Get current user's ID
            User currentUser = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + principal.getName()));
            
            // Get the product
            Product product = productService.getProductById(id);
            
            // Log IDs to help with debugging
            System.out.println("Current user ID: " + currentUser.getUserId());
            System.out.println("Product seller ID: " + (product.getSeller() != null ? product.getSeller().getUserId() : "null"));
            
            // Check if current user is the seller of the product
            if (product.getSeller() == null || !product.getSeller().getUserId().equals(currentUser.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only delete your own products"));
            }
            
            // Delete the product
            productService.deleteProduct(id);
            
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Error deleting product: " + e.getMessage()));
        }
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

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Product>> getProductsBySellerId(@PathVariable Long sellerId) {
        List<Product> products = productService.getProductsBySellerId(sellerId);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.created(URI.create("/api/products/" + createdProduct.getProductId()))
                .body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setProductId(id);
        Product updatedProduct = productService.updateProduct(product);
        return ResponseEntity.ok(updatedProduct);
    }
}
