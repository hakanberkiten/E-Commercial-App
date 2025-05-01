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
import java.util.stream.Collectors;

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
            System.out.println("==================================================");
            System.out.println("DELETE PRODUCT DEBUG:");
            System.out.println("Current user ID: " + currentUser.getUserId());
            System.out.println("Current user role name: " + currentUser.getRole().getRoleName());
            System.out.println("Product seller ID: " + (product.getSeller() != null ? product.getSeller().getUserId() : "null"));
            
            // Enhanced admin check
            String roleName = currentUser.getRole().getRoleName();
            boolean isAdmin = roleName != null && roleName.trim().toUpperCase().equals("ADMIN");
            boolean isSeller = product.getSeller() != null && 
                              product.getSeller().getUserId().equals(currentUser.getUserId());
            
            System.out.println("Is admin? " + isAdmin);
            System.out.println("Is seller? " + isSeller);
            System.out.println("Will allow delete? " + (isAdmin || isSeller));
            System.out.println("==================================================");
            
            // Check if current user is admin OR the seller of the product
            if (!isAdmin && !isSeller) {
                System.out.println("ACCESS DENIED: User is neither admin nor the seller of this product");
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
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Boolean inStock) {
        
        List<Product> products = productService.filterProducts(categoryId, minPrice, maxPrice, sortBy, inStock);
        
        // Filter out products from inactive sellers
        return products.stream()
                .filter(product -> product.getSeller() == null || product.getSeller().getActive())
                .collect(Collectors.toList());
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
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product, Principal principal) {
        try {
            User currentUser = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
            Product existingProduct = productService.getProductById(id);
            
            // Very detailed logging
            System.out.println("==================================================");
            System.out.println("UPDATE PRODUCT DEBUG - Request Details:");
            System.out.println("Current user ID: " + currentUser.getUserId());
            System.out.println("Current user email: " + currentUser.getEmail());
            System.out.println("Current user role name: " + currentUser.getRole().getRoleName());
            System.out.println("Current user role ID: " + currentUser.getRole().getRoleId());
            System.out.println("Product ID: " + id);
            System.out.println("Product seller ID: " + (existingProduct.getSeller() != null ? 
                                                   existingProduct.getSeller().getUserId() : "null"));
            
            // Use exact string comparison for role name
            String roleName = currentUser.getRole().getRoleName();
            System.out.println("Exact role name: '" + roleName + "'");
            
            // More direct approach to admin check
            boolean isAdmin = false;
            if (roleName != null) {
                // Check exact matches for common admin role names
                isAdmin = "ROLE_ADMIN".equals(roleName) || 
                          "ADMIN".equals(roleName);
                
                // Log the specific comparisons
                System.out.println("Equals ROLE_ADMIN? " + "ROLE_ADMIN".equals(roleName));
                System.out.println("Equals ADMIN? " + "ADMIN".equals(roleName));
            }
            
            // Also check by ID for backup (using equals to handle boxed types)
            if (!isAdmin && currentUser.getRole().getRoleId() != null) {
                isAdmin = Integer.valueOf(1).equals(currentUser.getRole().getRoleId());
                System.out.println("Role ID equals 1? " + isAdmin);
            }
            
            boolean isSeller = existingProduct.getSeller() != null && 
                              existingProduct.getSeller().getUserId().equals(currentUser.getUserId());
            
            System.out.println("Is admin? " + isAdmin);
            System.out.println("Is seller? " + isSeller);
            System.out.println("Will allow update? " + (isAdmin || isSeller));
            System.out.println("==================================================");
            
            if (!isAdmin && !isSeller) {
                System.out.println("ACCESS DENIED: User is neither admin nor the seller of this product");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(null);
            }
            
            // Preserve the original seller when updating
            product.setProductId(id);
            product.setSeller(existingProduct.getSeller());
            
            Product updatedProduct = productService.updateProduct(product);
            System.out.println("Product updated successfully");
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            System.err.println("ERROR in updateProduct: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
