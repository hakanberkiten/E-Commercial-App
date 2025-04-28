package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository; // Assuming you have a UserRepository to fetch User details


    @Override
// ProductServiceImpl.java
public Product saveProduct(Product product) {
    // 1️⃣ JSON’dan gelen “seller” objesi sadece userId içeriyor, role bilgisi null
    // Bu yüzden önce gerçek User’ı veritabanından çekiyoruz:
    User seller = userRepository.findById(product.getSeller().getUserId())
        .orElseThrow(() -> new RuntimeException("Seller not found"));

    // 2️⃣ Şimdi seller’ın rolünü güvenle kontrol edebiliriz:
    if (seller.getRole().getRoleId() != 2) {
        throw new IllegalArgumentException("Only SELLER can add products");
    }

    // 3️⃣ Product içine mutlaka DB’den çektiğimiz seller objesini set etmeliyiz
    product.setSeller(seller);

    return productRepository.save(product);
}


    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> getByCategory(Long categoryId) {
        return productRepository.findByCategoryCategoryId(categoryId);
      }
}
