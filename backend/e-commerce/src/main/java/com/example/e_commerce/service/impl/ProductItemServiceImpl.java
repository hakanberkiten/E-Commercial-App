package com.example.e_commerce.service.impl;

import com.example.e_commerce.dto.ProductItemDTO;
import com.example.e_commerce.entity.*;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.*;
import com.example.e_commerce.service.ProductItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductItemServiceImpl implements ProductItemService {

    private final ProductItemRepository repo;
    private final ProductRepository productRepo;
    private final ColorRepository colorRepo;
    private final SizeRepository sizeRepo;

    private ProductItemDTO toDto(ProductItem e) {
        ProductItemDTO dto = new ProductItemDTO();
        dto.setId(e.getId());
        dto.setProductId(e.getProduct().getId());
        dto.setColorId(e.getColor()   != null ? e.getColor().getId()   : null);
        dto.setSizeId(e.getSize()     != null ? e.getSize().getId()    : null);
        dto.setDiscountedPrice(e.getDiscountedPrice());
        dto.setQuantityInStock(e.getQuantityInStock());
        return dto;
    }

    private ProductItem toEntity(ProductItemDTO dto) {
        ProductItem e = new ProductItem();
        // Ürün
        Product p = productRepo.findById(dto.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product","id",dto.getProductId()));
        e.setProduct(p);
        // Renk
        if (dto.getColorId() != null) {
            Color c = colorRepo.findById(dto.getColorId())
                .orElseThrow(() -> new ResourceNotFoundException("Color","id",dto.getColorId()));
            e.setColor(c);
        }
        // Beden
        if (dto.getSizeId() != null) {
            Size s = sizeRepo.findById(dto.getSizeId())
                .orElseThrow(() -> new ResourceNotFoundException("Size","id",dto.getSizeId()));
            e.setSize(s);
        }
        e.setDiscountedPrice(dto.getDiscountedPrice());
        e.setQuantityInStock(dto.getQuantityInStock());
        return e;
    }

    @Override
    public ProductItemDTO create(ProductItemDTO dto) {
        ProductItem saved = repo.save(toEntity(dto));
        return toDto(saved);
    }

    @Override
    public ProductItemDTO getById(Integer id) {
        return repo.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("ProductItem","id",id));
    }

    @Override
    public List<ProductItemDTO> getAll() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ProductItemDTO> getByProductId(Integer productId) {
        return repo.findByProductId(productId)
            .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ProductItemDTO update(Integer id, ProductItemDTO dto) {
        ProductItem existing = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ProductItem","id",id));
        // Sadece gelen alanları güncelleyelim
        if (dto.getColorId() != null) {
            Color c = colorRepo.findById(dto.getColorId())
                .orElseThrow(() -> new ResourceNotFoundException("Color","id",dto.getColorId()));
            existing.setColor(c);
        }
        if (dto.getSizeId() != null) {
            Size s = sizeRepo.findById(dto.getSizeId())
                .orElseThrow(() -> new ResourceNotFoundException("Size","id",dto.getSizeId()));
            existing.setSize(s);
        }
        existing.setDiscountedPrice(dto.getDiscountedPrice());
        existing.setQuantityInStock(dto.getQuantityInStock());
        ProductItem updated = repo.save(existing);
        return toDto(updated);
    }

    @Override
    public void delete(Integer id) {
        ProductItem e = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ProductItem","id",id));
        repo.delete(e);
    }
}
