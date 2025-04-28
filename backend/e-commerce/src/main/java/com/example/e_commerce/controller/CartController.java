// CartController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.entity.CartItem;
import com.example.e_commerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/cart")
@CrossOrigin("http://localhost:4200")
@RequiredArgsConstructor
public class CartController {
  private final CartService cartSvc;

  // kimliği Spring Security üzerinden alıyoruz:
  private Long currentUser(Principal p) {
    return Long.valueOf(p.getName()); // BasicAuth userid olarak dönüyorsa; yoksa custom
  }

  @GetMapping
  public List<CartItem> list(Principal p) {
    return cartSvc.listItems(currentUser(p));
  }

  @PostMapping("/items")
  public CartItem add(
    Principal p,
    @RequestBody Map<String,Object> body
  ) {
    Long productId = Long.valueOf(body.get("productId").toString());
    Integer qty     = Integer.valueOf(body.get("quantity").toString());
    return cartSvc.addItem(currentUser(p), productId, qty);
  }

  @DeleteMapping("/items/{id}")
  public void remove(Principal p, @PathVariable Long id) {
    cartSvc.removeItem(currentUser(p), id);
  }

  @PutMapping("/items/{id}")
  public CartItem update(
    Principal p,
    @PathVariable Long id,
    @RequestParam Integer quantity
  ) {
    return cartSvc.updateItem(currentUser(p), id, quantity);
  }
}