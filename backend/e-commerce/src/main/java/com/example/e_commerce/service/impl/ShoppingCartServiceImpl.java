package com.example.e_commerce.service.impl;

import com.example.e_commerce.dto.ShoppingCartDTO;
import com.example.e_commerce.entity.ShoppingCart;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.ShoppingCartRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository cartRepo;
    private final UserRepository userRepo;

    private ShoppingCartDTO toDto(ShoppingCart e) {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setId(e.getId());
        dto.setUserId(e.getUser().getId());
        return dto;
    }

    private ShoppingCart toEntity(ShoppingCartDTO dto) {
        User u = userRepo.findById(dto.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User","id",dto.getUserId()));
        ShoppingCart e = new ShoppingCart();
        e.setUser(u);
        return e;
    }

    @Override
    public ShoppingCartDTO create(ShoppingCartDTO dto) {
        ShoppingCart saved = cartRepo.save(toEntity(dto));
        return toDto(saved);
    }

    @Override
    public ShoppingCartDTO getById(Integer id) {
        return cartRepo.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("ShoppingCart","id",id));
    }

    @Override
    public Optional<ShoppingCartDTO> getByUserId(Integer userId) {
        return cartRepo.findByUserId(userId)
            .map(this::toDto);
    }

    @Override
    public void delete(Integer id) {
        ShoppingCart e = cartRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ShoppingCart","id",id));
        cartRepo.delete(e);
    }
}
