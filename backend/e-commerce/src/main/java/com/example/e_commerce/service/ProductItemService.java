package com.example.e_commerce.service;

import com.example.e_commerce.dto.ProductItemDTO;
import java.util.List;

public interface ProductItemService {
    ProductItemDTO create(ProductItemDTO dto);
    ProductItemDTO getById(Integer id);
    List<ProductItemDTO> getAll();
    List<ProductItemDTO> getByProductId(Integer productId);
    ProductItemDTO update(Integer id, ProductItemDTO dto);
    void delete(Integer id);
}
