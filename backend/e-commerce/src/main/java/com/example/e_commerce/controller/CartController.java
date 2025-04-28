// CartController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.entity.Cart;
import com.example.e_commerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/save")
    public Cart save(@RequestBody Cart cart) {
        return cartService.saveCart(cart);
    }

    @GetMapping("/all")
    public List<Cart> all() { return cartService.getAllCarts(); }

    @GetMapping("/{id}")
    public Cart byId(@PathVariable Long id) {
        return cartService.getCartById(id);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        cartService.deleteCart(id);
    }
}
