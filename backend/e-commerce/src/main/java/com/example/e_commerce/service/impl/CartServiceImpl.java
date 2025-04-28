// CartServiceImpl.java
package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.Cart;
import com.example.e_commerce.entity.CartItem;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.CartItemRepository;
import com.example.e_commerce.repository.CartRepository;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.CartService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
@Service @RequiredArgsConstructor @Transactional
public class CartServiceImpl implements CartService {
  private final CartRepository cartRepo;
  private final CartItemRepository itemRepo;
  private final UserRepository userRepo;
  private final ProductRepository productRepo;

  public Cart getOrCreateCart(Long userId) {
    return cartRepo.findByUserUserId(userId)
      .orElseGet(() -> {
        User u = userRepo.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));
        Cart c = new Cart();
        c.setUser(u);
        c.setTotalPrice(BigDecimal.ZERO);
        return cartRepo.save(c);
      });
  }

  public CartItem addItem(Long userId, Long productId, Integer qty) {
    Cart cart = getOrCreateCart(userId);
    Product p = productRepo.findById(productId)
       .orElseThrow(() -> new RuntimeException("Product not found"));
    if (p.getQuantityInStock() < qty) throw new RuntimeException("Insufficient stock");
    p.setQuantityInStock(p.getQuantityInStock() - qty);
    productRepo.save(p);

    CartItem it = new CartItem();
    it.setCart(cart);
    it.setProduct(p);
    it.setQuantityInCart(qty);
    CartItem saved = itemRepo.save(it);

    // toplam güncelle
    BigDecimal total = itemRepo.findByCartCartId(cart.getCartId()).stream()
        .map(x -> BigDecimal.valueOf(x.getProduct().getPrice()).multiply(BigDecimal.valueOf(x.getQuantityInCart())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    cart.setTotalPrice(total);
    cartRepo.save(cart);

    return saved;
  }

  public List<CartItem> listItems(Long userId) {
    Cart cart = getOrCreateCart(userId);
    return itemRepo.findByCartCartId(cart.getCartId());
  }

  public void removeItem(Long userId, Long itemId) {
    CartItem it = itemRepo.findById(itemId)
      .orElseThrow(() -> new RuntimeException("Item not found"));
    if (!it.getCart().getUser().getUserId().equals(userId))
      throw new RuntimeException("Not your cart item");
    // stok iade edelim
    Product p = it.getProduct();
    p.setQuantityInStock(p.getQuantityInStock() + it.getQuantityInCart());
    productRepo.save(p);
    itemRepo.delete(it);

    // toplam güncelle
    Cart cart = it.getCart();
    BigDecimal total = itemRepo.findByCartCartId(cart.getCartId()).stream()
        .map(x -> BigDecimal.valueOf(x.getProduct().getPrice()).multiply(BigDecimal.valueOf(x.getQuantityInCart())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    cart.setTotalPrice(total);
    cartRepo.save(cart);
  }

  public CartItem updateItem(Long userId, Long itemId, Integer qty) {
    CartItem it = itemRepo.findById(itemId)
      .orElseThrow(() -> new RuntimeException("Item not found"));
    if (!it.getCart().getUser().getUserId().equals(userId))
      throw new RuntimeException("Not your cart item");

    // stok farkı yansıt
    int diff = qty - it.getQuantityInCart();
    Product p = it.getProduct();
    if (p.getQuantityInStock() < diff) throw new RuntimeException("Insufficient stock");
    p.setQuantityInStock(p.getQuantityInStock() - diff);
    productRepo.save(p);

    it.setQuantityInCart(qty);
    CartItem saved = itemRepo.save(it);

    // toplam güncelle
    Cart cart = it.getCart();
    BigDecimal total = itemRepo.findByCartCartId(cart.getCartId()).stream()
        .map(x -> BigDecimal.valueOf(x.getProduct().getPrice()).multiply(BigDecimal.valueOf(x.getQuantityInCart())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    cart.setTotalPrice(total);
    cartRepo.save(cart);

    return saved;
  }
}