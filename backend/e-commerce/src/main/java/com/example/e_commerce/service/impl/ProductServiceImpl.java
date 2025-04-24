package com.example.e_commerce.service.impl;


import com.example.e_commerce.dto.ProductDTO;
import com.example.e_commerce.entity.Category;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.CategoryRepository;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    private ProductDTO toDto(Product e) {
        ProductDTO dto = new ProductDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setDescription(e.getDescription());
        dto.setPrice(e.getPrice());
        dto.setCategoryId(e.getCategory() != null ? e.getCategory().getId() : null);
        dto.setGender(e.getGender());
        dto.setDiscount(e.getDiscount());
        dto.setProductImage(e.getProductImage());
        return dto;
    }

    private Product toEntity(ProductDTO dto) {
        Product e = new Product();
        e.setName(dto.getName());
        e.setDescription(dto.getDescription());
        e.setPrice(dto.getPrice());
        if (dto.getCategoryId() != null) {
            Category cat = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category","id",dto.getCategoryId()));
            e.setCategory(cat);
        }
        e.setGender(dto.getGender());
        e.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO);
        e.setProductImage(dto.getProductImage());
        return e;
    }

    @Override
    public ProductDTO create(ProductDTO dto) {
        Product saved = productRepo.save(toEntity(dto));
        return toDto(saved);
    }

    @Override
    public ProductDTO getById(Integer id) {
        return productRepo.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Product","id",id));
    }

    @Override
    public List<ProductDTO> getAll() {
        return productRepo.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public ProductDTO update(Integer id, ProductDTO dto) {
        Product existing = productRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product","id",id));
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        if (dto.getCategoryId() != null) {
            Category cat = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category","id",dto.getCategoryId()));
            existing.setCategory(cat);
        }
        existing.setGender(dto.getGender());
        existing.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : existing.getDiscount());
        existing.setProductImage(dto.getProductImage());
        Product updated = productRepo.save(existing);
        return toDto(updated);
    }

    @Override
    public void delete(Integer id) {
        Product e = productRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product","id",id));
        productRepo.delete(e);
    }
}
