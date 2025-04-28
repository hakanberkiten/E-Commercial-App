// CartItemServiceImpl.java
package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.Cart;
import com.example.e_commerce.entity.CartItem;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.repository.CartItemRepository;
import com.example.e_commerce.repository.CartRepository;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {
    private final CartItemRepository itemRepo;
    private final CartRepository cartRepo;
    private final ProductRepository productRepo;

    @Override
    public CartItem saveCartItem(CartItem item) {
        Cart cart = cartRepo.findById(item.getCart().getCartId())
            .orElseThrow(() -> new RuntimeException("Cart not found"));
        Product prod = productRepo.findById(item.getProduct().getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));
        item.setCart(cart);
        item.setProduct(prod);
        return itemRepo.save(item);
    }

    @Override
    public List<CartItem> getAllCartItems() { return itemRepo.findAll(); }

    @Override
    public CartItem getCartItemById(Long id) {
        return itemRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("CartItem not found"));
    }

    @Override
    public void deleteCartItem(Long id) {
        itemRepo.deleteById(id);
    }
}
