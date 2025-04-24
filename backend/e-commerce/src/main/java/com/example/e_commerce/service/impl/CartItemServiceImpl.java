package com.example.e_commerce.service.impl;


import com.example.e_commerce.dto.CartItemDTO;
import com.example.e_commerce.entity.CartItem;
import com.example.e_commerce.entity.ProductItem;
import com.example.e_commerce.entity.ShoppingCart;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.CartItemRepository;
import com.example.e_commerce.repository.ProductItemRepository;
import com.example.e_commerce.repository.ShoppingCartRepository;
import com.example.e_commerce.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository repo;
    private final ShoppingCartRepository cartRepo;
    private final ProductItemRepository itemRepo;

    private CartItemDTO toDto(CartItem e) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(e.getId());
        dto.setCartId(e.getCart().getId());
        dto.setProductItemId(e.getProductItem().getId());
        dto.setQuantity(e.getQuantity());
        return dto;
    }

    private CartItem toEntity(CartItemDTO dto) {
        ShoppingCart c = cartRepo.findById(dto.getCartId())
            .orElseThrow(() -> new ResourceNotFoundException("ShoppingCart","id",dto.getCartId()));
        ProductItem pi = itemRepo.findById(dto.getProductItemId())
            .orElseThrow(() -> new ResourceNotFoundException("ProductItem","id",dto.getProductItemId()));
        CartItem e = new CartItem();
        e.setCart(c);
        e.setProductItem(pi);
        e.setQuantity(dto.getQuantity());
        return e;
    }

    @Override
    public CartItemDTO create(CartItemDTO dto) {
        CartItem saved = repo.save(toEntity(dto));
        return toDto(saved);
    }

    @Override
    public CartItemDTO getById(Integer id) {
        return repo.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("CartItem","id",id));
    }

    @Override
    public List<CartItemDTO> getAll() {
        return repo.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<CartItemDTO> getByCartId(Integer cartId) {
        return repo.findByCartId(cartId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public CartItemDTO update(Integer id, CartItemDTO dto) {
        CartItem existing = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("CartItem","id",id));
        existing.setQuantity(dto.getQuantity());
        CartItem updated = repo.save(existing);
        return toDto(updated);
    }

    @Override
    public void delete(Integer id) {
        CartItem e = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("CartItem","id",id));
        repo.delete(e);
    }
}

