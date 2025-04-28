// CartServiceImpl.java
package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.Cart;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.CartRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepo;
    private final UserRepository userRepo;

    @Override
    public Cart saveCart(Cart cart) {
        User user = userRepo.findById(cart.getUser().getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        cart.setUser(user);
        return cartRepo.save(cart);
    }

    @Override
    public List<Cart> getAllCarts() { return cartRepo.findAll(); }

    @Override
    public Cart getCartById(Long id) {
        return cartRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    @Override
    public void deleteCart(Long id) {
        cartRepo.deleteById(id);
    }
}
