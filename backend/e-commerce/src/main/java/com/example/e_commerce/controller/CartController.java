// CartController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.entity.CartItem;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.security.Principal;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/cart")
@CrossOrigin("http://localhost:4200")
@RequiredArgsConstructor
public class CartController {
  private final CartService cartSvc;
  private final UserRepository userRepository;

  // kimliği Spring Security üzerinden alıyoruz:
 private Long currentUser(Principal p) {
    // If the principal is the UserDetails itself
    if (p instanceof UsernamePasswordAuthenticationToken) {
      UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) p;
      Object principal = token.getPrincipal();
      
      if (principal instanceof UserDetails) {
        String username = ((UserDetails) principal).getUsername();
        User user = userRepository.findByEmail(username)
          .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getUserId();
      }
    }
    
    // Fallback - try to get from principal name (email)
    User user = userRepository.findByEmail(p.getName())
      .orElseThrow(() -> new RuntimeException("User not found"));
    return user.getUserId();
  }

  @GetMapping
  public List<CartItem> list(Principal p) {
    return cartSvc.listItems(currentUser(p));
  }

  @PostMapping("/items")
  public CartItem add(Principal p, @RequestBody Map<String, Object> body) {
      System.out.println("Add to cart request received. Principal: " + (p != null ? p.getName() : "null"));
      
      if (p == null) {
          throw new RuntimeException("Not authenticated");
      }
      
      Long productId = Long.valueOf(body.get("productId").toString());
      Integer qty = Integer.valueOf(body.get("quantity").toString());
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