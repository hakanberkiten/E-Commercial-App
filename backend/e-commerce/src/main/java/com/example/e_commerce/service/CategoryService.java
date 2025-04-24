package com.example.e_commerce.service;


import com.example.e_commerce.dto.CategoryDTO;
import java.util.List;

public interface CategoryService {
    CategoryDTO create(CategoryDTO dto);
    CategoryDTO getById(Integer id);
    List<CategoryDTO> getAll();
    CategoryDTO update(Integer id, CategoryDTO dto);
    void delete(Integer id);
}
