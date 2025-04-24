package com.example.e_commerce.service;


import com.example.e_commerce.dto.ProductDTO;
import java.util.List;

public interface ProductService {
    ProductDTO create(ProductDTO dto);
    ProductDTO getById(Integer id);
    List<ProductDTO> getAll();
    ProductDTO update(Integer id, ProductDTO dto);
    void delete(Integer id);
}
