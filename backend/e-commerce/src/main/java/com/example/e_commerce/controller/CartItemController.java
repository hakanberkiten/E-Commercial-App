// CartItemController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.entity.CartItem;
import com.example.e_commerce.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart-items")
@RequiredArgsConstructor
public class CartItemController {
    private final CartItemService itemService;

    @PostMapping("/save")
    public CartItem save(@RequestBody CartItem item) {
        return itemService.saveCartItem(item);
    }

    @GetMapping("/all")
    public List<CartItem> all() { return itemService.getAllCartItems(); }

    @GetMapping("/{id}")
    public CartItem byId(@PathVariable Long id) {
        return itemService.getCartItemById(id);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        itemService.deleteCartItem(id);
    }
}
